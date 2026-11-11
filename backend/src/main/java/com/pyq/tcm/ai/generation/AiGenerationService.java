package com.pyq.tcm.ai.generation;

import com.fasterxml.jackson.databind.JsonNode;
import com.pyq.tcm.ai.client.AiChatResult;
import com.pyq.tcm.ai.client.AiException;
import com.pyq.tcm.ai.client.ChatMessage;
import com.pyq.tcm.ai.client.OpenAiChatClient;
import com.pyq.tcm.ai.config.AiProperties;
import com.pyq.tcm.ai.dto.GenerationRequest;
import com.pyq.tcm.ai.entity.AiGeneration;
import com.pyq.tcm.ai.entity.AiGenerationItem;
import com.pyq.tcm.ai.model.AiCallContext;
import com.pyq.tcm.ai.prompt.AiJsonExtractor;
import com.pyq.tcm.ai.prompt.AiPromptService;
import com.pyq.tcm.ai.repository.AiGenerationItemRepository;
import com.pyq.tcm.ai.repository.AiGenerationRepository;
import com.pyq.tcm.common.BizException;
import com.pyq.tcm.common.PageResult;
import com.pyq.tcm.entity.CaseStep;
import com.pyq.tcm.entity.Project;
import com.pyq.tcm.entity.Module;
import com.pyq.tcm.repository.ModuleRepository;
import com.pyq.tcm.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiGenerationService {

    private static final Logger log = LoggerFactory.getLogger(AiGenerationService.class);
    private static final List<String> PRIORITIES = List.of("P0", "P1", "P2", "P3");
    private static final List<String> TYPES = List.of("FUNCTIONAL", "API", "UI", "PERFORMANCE", "SECURITY");

    private final AiGenerationRepository generationRepository;
    private final AiGenerationItemRepository itemRepository;
    private final ProjectRepository projectRepository;
    private final ModuleRepository moduleRepository;
    private final AiPromptService promptService;
    private final AiJsonExtractor jsonExtractor;
    private final OpenAiChatClient chatClient;
    private final AiProperties aiProperties;

    public AiGenerationService(AiGenerationRepository generationRepository,
                               AiGenerationItemRepository itemRepository,
                               ProjectRepository projectRepository,
                               ModuleRepository moduleRepository,
                               AiPromptService promptService,
                               AiJsonExtractor jsonExtractor,
                               OpenAiChatClient chatClient,
                               AiProperties aiProperties) {
        this.generationRepository = generationRepository;
        this.itemRepository = itemRepository;
        this.projectRepository = projectRepository;
        this.moduleRepository = moduleRepository;
        this.promptService = promptService;
        this.jsonExtractor = jsonExtractor;
        this.chatClient = chatClient;
        this.aiProperties = aiProperties;
    }

    @Transactional
    public AiGeneration createTask(GenerationRequest request, String username) {
        if (request.getProjectId() == null || !projectRepository.existsById(request.getProjectId())) {
            throw new BizException("项目不存在");
        }
        if (request.getRequirement() == null || request.getRequirement().isBlank()) {
            throw new BizException("需求描述不能为空");
        }
        if (request.getRequirement().length() > aiProperties.getMaxRequirementLength()) {
            throw new BizException("需求描述过长，最多 " + aiProperties.getMaxRequirementLength() + " 个字符");
        }
        if (request.getModuleId() != null
                && !moduleRepository.existsById(request.getModuleId())) {
            throw new BizException("所选模块不存在");
        }
        AiGeneration generation = new AiGeneration();
        generation.setProjectId(request.getProjectId());
        generation.setModuleId(request.getModuleId());
        generation.setScene(request.getScene() == null || request.getScene().isBlank()
                ? "GENERATE_CASE" : request.getScene());
        generation.setRequirement(request.getRequirement());
        generation.setFileRefs(request.getFileRefs());
        generation.setModelId(request.getModelId());
        generation.setCreatedBy(username);
        generation.setStatus("PENDING");
        return generationRepository.save(generation);
    }

    @Async("aiTaskExecutor")
    public void runTask(Long generationId) {
        AiGeneration generation = generationRepository.findById(generationId).orElse(null);
        if (generation == null) {
            return;
        }
        generation.setStatus("RUNNING");
        generationRepository.save(generation);
        try {
            List<AiGenerationItem> items = generateItems(generation);
            itemRepository.saveAll(items);
            generation.setTotalCount(items.size());
            generation.setStatus("SUCCEEDED");
            generation.setFinishedAt(LocalDateTime.now());
            generationRepository.save(generation);
        } catch (AiException e) {
            log.warn("AI 用例生成失败 generationId={}: {}", generationId, e.getMessage());
            generation.setStatus("FAILED");
            generation.setErrorMsg(truncate(e.getErrorCode() + ": " + e.getMessage()));
            generation.setFinishedAt(LocalDateTime.now());
            generationRepository.save(generation);
        } catch (Exception e) {
            log.error("AI 用例生成异常 generationId=" + generationId, e);
            generation.setStatus("FAILED");
            generation.setErrorMsg(truncate(e.getMessage()));
            generation.setFinishedAt(LocalDateTime.now());
            generationRepository.save(generation);
        }
    }

    private List<AiGenerationItem> generateItems(AiGeneration generation) {
        Project project = projectRepository.findById(generation.getProjectId())
                .orElseThrow(() -> new BizException("项目不存在"));
        String moduleName = "";
        if (generation.getModuleId() != null) {
            moduleName = moduleRepository.findById(generation.getModuleId())
                    .map(Module::getName).orElse("");
        }
        Map<String, String> variables = new HashMap<>();
        variables.put("projectName", project.getName());
        variables.put("moduleName", moduleName);
        variables.put("requirement", generation.getRequirement());
        AiPromptService.RenderedPrompt prompt = promptService.render("GENERATE_CASE", variables);
        generation.setPromptVersion(prompt.version());

        AiCallContext context = new AiCallContext(generation.getScene(), "GENERATION",
                generation.getId(), null, generation.getCreatedBy(), null);
        AiChatResult result = chatClient.chat(generation.getModelId(), generation.getScene(),
                List.of(ChatMessage.system(prompt.text()),
                        ChatMessage.user("请根据系统指令生成测试用例，仅返回 JSON 数组。")),
                context);

        JsonNode root = jsonExtractor.extract(result.getContent());
        JsonNode array = root.isArray() ? root
                : root.path("items").isArray() ? root.path("items")
                : root.path("cases").isArray() ? root.path("cases")
                : null;
        if (array == null) {
            throw new AiException("INVALID_SHAPE", "AI 返回的 JSON 缺少用例数组");
        }
        List<AiGenerationItem> items = new ArrayList<>();
        for (JsonNode node : array) {
            AiGenerationItem item = toItem(generation.getId(), node);
            if (item != null) {
                items.add(item);
            }
        }
        if (items.isEmpty()) {
            throw new AiException("EMPTY_RESULT", "AI 未生成有效的用例条目");
        }
        return items;
    }

    private AiGenerationItem toItem(Long generationId, JsonNode node) {
        String title = text(node, "title");
        if (title == null || title.isBlank()) {
            return null;
        }
        AiGenerationItem item = new AiGenerationItem();
        item.setGenerationId(generationId);
        item.setTitle(title.trim());
        String priority = text(node, "priority");
        item.setPriority(priority != null && PRIORITIES.contains(priority) ? priority : "P1");
        String type = text(node, "type");
        item.setType(type != null && TYPES.contains(type) ? type : "FUNCTIONAL");
        item.setPrecondition(text(node, "precondition"));
        item.setExpected(text(node, "expected"));
        JsonNode confidence = node.get("confidence");
        if (confidence != null && confidence.isNumber()) {
            item.setConfidence(Math.max(0, Math.min(100, confidence.asInt())));
        }
        item.setSteps(parseSteps(node.get("steps"), item.getExpected()));
        item.setSelected(Boolean.TRUE);
        item.setStatus("PENDING");
        return item;
    }

    private List<CaseStep> parseSteps(JsonNode stepsNode, String fallbackExpected) {
        List<CaseStep> steps = new ArrayList<>();
        if (stepsNode != null && stepsNode.isArray()) {
            for (JsonNode stepNode : stepsNode) {
                if (stepNode.isTextual()) {
                    steps.add(new CaseStep(stepNode.asText(), fallbackExpected));
                } else if (stepNode.isObject()) {
                    String action = text(stepNode, "action");
                    if (action == null) {
                        action = text(stepNode, "step");
                    }
                    String expected = text(stepNode, "expected");
                    if (action != null && !action.isBlank()) {
                        steps.add(new CaseStep(action, expected == null ? fallbackExpected : expected));
                    }
                }
            }
        }
        if (steps.isEmpty() && fallbackExpected != null && !fallbackExpected.isBlank()) {
            steps.add(new CaseStep("执行用例", fallbackExpected));
        }
        return steps;
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    public PageResult<AiGeneration> page(Long projectId, String scene, String status, int page, int size) {
        Page<AiGeneration> result = generationRepository.search(
                projectId,
                scene == null ? "" : scene,
                status == null ? "" : status,
                PageRequest.of(Math.max(page, 1) - 1, Math.min(Math.max(size, 1), 100),
                        Sort.by(Sort.Direction.DESC, "createdAt")));
        return PageResult.of(result);
    }

    public Map<String, Object> detail(Long generationId) {
        AiGeneration generation = generationRepository.findById(generationId)
                .orElseThrow(() -> new BizException(404, "生成任务不存在"));
        List<AiGenerationItem> items = itemRepository.findByGenerationIdOrderByIdAsc(generationId);
        Map<String, Object> result = new HashMap<>();
        result.put("generation", generation);
        result.put("items", items);
        return result;
    }

    @Transactional
    public int deleteTask(Long generationId) {
        AiGeneration generation = generationRepository.findById(generationId)
                .orElseThrow(() -> new BizException(404, "生成任务不存在"));
        int count = itemRepository.findByGenerationIdOrderByIdAsc(generationId).size();
        itemRepository.deleteByGenerationId(generationId);
        generationRepository.delete(generation);
        return count;
    }

    private String truncate(String text) {
        if (text == null) {
            return null;
        }
        return text.length() > 500 ? text.substring(0, 500) : text;
    }
}
