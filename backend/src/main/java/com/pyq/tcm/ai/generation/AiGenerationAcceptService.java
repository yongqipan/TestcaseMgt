package com.pyq.tcm.ai.generation;

import com.pyq.tcm.ai.dto.AcceptRequest;
import com.pyq.tcm.ai.entity.AiGeneration;
import com.pyq.tcm.ai.entity.AiGenerationItem;
import com.pyq.tcm.ai.repository.AiGenerationItemRepository;
import com.pyq.tcm.ai.repository.AiGenerationRepository;
import com.pyq.tcm.common.BizException;
import com.pyq.tcm.entity.Project;
import com.pyq.tcm.entity.TestCase;
import com.pyq.tcm.repository.ProjectRepository;
import com.pyq.tcm.repository.TestCaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class AiGenerationAcceptService {

    private final AiGenerationRepository generationRepository;
    private final AiGenerationItemRepository itemRepository;
    private final TestCaseRepository testCaseRepository;
    private final ProjectRepository projectRepository;

    public AiGenerationAcceptService(AiGenerationRepository generationRepository,
                                     AiGenerationItemRepository itemRepository,
                                     TestCaseRepository testCaseRepository,
                                     ProjectRepository projectRepository) {
        this.generationRepository = generationRepository;
        this.itemRepository = itemRepository;
        this.testCaseRepository = testCaseRepository;
        this.projectRepository = projectRepository;
    }

    @Transactional
    public List<TestCase> accept(Long generationId, AcceptRequest request, String username) {
        AiGeneration generation = generationRepository.findById(generationId)
                .orElseThrow(() -> new BizException(404, "生成任务不存在"));
        if (!"SUCCEEDED".equals(generation.getStatus())) {
            throw new BizException("生成任务尚未完成，无法采纳");
        }
        Project project = projectRepository.findById(generation.getProjectId())
                .orElseThrow(() -> new BizException("项目不存在"));

        List<AiGenerationItem> items = selectItems(generationId, request);
        if (items.isEmpty()) {
            throw new BizException("请选择要采纳的用例");
        }

        List<TestCase> created = new ArrayList<>();
        int accepted = generation.getAcceptedCount() == null ? 0 : generation.getAcceptedCount();
        for (AiGenerationItem item : items) {
            if (item.getAcceptedCaseId() != null) {
                continue;
            }
            TestCase testCase = new TestCase();
            testCase.setProjectId(generation.getProjectId());
            testCase.setModuleId(generation.getModuleId());
            testCase.setCaseNo(nextCaseNo(project));
            testCase.setTitle(item.getTitle());
            testCase.setPriority(item.getPriority());
            testCase.setType(item.getType());
            testCase.setStatus("ACTIVE");
            testCase.setPrecondition(item.getPrecondition());
            testCase.setSteps(item.getSteps());
            testCase.setCreator(username);
            testCase.setSource("AI");
            testCase.setAiConfidence(item.getConfidence());
            TestCase saved = testCaseRepository.save(testCase);

            item.setAcceptedCaseId(saved.getId());
            item.setStatus("ACCEPTED");
            item.setSelected(Boolean.FALSE);
            itemRepository.save(item);
            created.add(saved);
            accepted++;
        }
        generation.setAcceptedCount(accepted);
        generationRepository.save(generation);
        return created;
    }

    private List<AiGenerationItem> selectItems(Long generationId, AcceptRequest request) {
        List<AiGenerationItem> all = itemRepository.findByGenerationIdOrderByIdAsc(generationId);
        if (request == null || request.getItemIds() == null || request.getItemIds().isEmpty()) {
            return all;
        }
        List<Long> ids = request.getItemIds().stream()
                .filter(Objects::nonNull)
                .toList();
        return all.stream().filter(item -> ids.contains(item.getId())).toList();
    }

    private String nextCaseNo(Project project) {
        long sequence = testCaseRepository.countByProjectId(project.getId()) + 1;
        String caseNo = String.format("TC-%s-%03d", project.getCode(), sequence);
        while (testCaseRepository.existsByCaseNo(caseNo)) {
            sequence++;
            caseNo = String.format("TC-%s-%03d", project.getCode(), sequence);
        }
        return caseNo;
    }
}
