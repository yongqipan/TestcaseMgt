package com.pyq.tcm.ai.report;

import com.fasterxml.jackson.databind.JsonNode;
import com.pyq.tcm.ai.client.AiChatResult;
import com.pyq.tcm.ai.client.ChatMessage;
import com.pyq.tcm.ai.client.OpenAiChatClient;
import com.pyq.tcm.ai.dto.ReportRequest;
import com.pyq.tcm.ai.entity.AiReport;
import com.pyq.tcm.ai.model.AiCallContext;
import com.pyq.tcm.ai.prompt.AiJsonExtractor;
import com.pyq.tcm.ai.prompt.AiPromptService;
import com.pyq.tcm.ai.repository.AiReportRepository;
import com.pyq.tcm.common.BizException;
import com.pyq.tcm.entity.TestPlan;
import com.pyq.tcm.entity.TestRun;
import com.pyq.tcm.repository.TestPlanRepository;
import com.pyq.tcm.repository.TestRunRepository;
import com.pyq.tcm.service.TestPlanService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class AiReportService {

    private final AiReportRepository reportRepository;
    private final TestPlanRepository testPlanRepository;
    private final TestRunRepository testRunRepository;
    private final TestPlanService testPlanService;
    private final AiPromptService promptService;
    private final AiJsonExtractor jsonExtractor;
    private final OpenAiChatClient chatClient;

    public AiReportService(AiReportRepository reportRepository,
                           TestPlanRepository testPlanRepository,
                           TestRunRepository testRunRepository,
                           TestPlanService testPlanService,
                           AiPromptService promptService,
                           AiJsonExtractor jsonExtractor,
                           OpenAiChatClient chatClient) {
        this.reportRepository = reportRepository;
        this.testPlanRepository = testPlanRepository;
        this.testRunRepository = testRunRepository;
        this.testPlanService = testPlanService;
        this.promptService = promptService;
        this.jsonExtractor = jsonExtractor;
        this.chatClient = chatClient;
    }

    @Transactional
    public AiReport generate(ReportRequest request, String username) {
        if (request.getPlanId() == null) {
            throw new BizException("请选择测试计划");
        }
        TestPlan plan = testPlanRepository.findById(request.getPlanId())
                .orElseThrow(() -> new BizException(404, "测试计划不存在"));
        Map<String, Object> stats = testPlanService.stats(plan.getId());
        String details = renderDetails(testRunRepository.findByPlanId(plan.getId()));

        java.util.Map<String, String> variables = new java.util.HashMap<>();
        variables.put("planName", plan.getName());
        variables.put("statistics", stats.toString());
        variables.put("details", details);
        AiPromptService.RenderedPrompt prompt = promptService.render("GENERATE_REPORT", variables);

        AiCallContext context = new AiCallContext("GENERATE_REPORT", "PLAN",
                plan.getId(), null, username, null);
        AiChatResult result = chatClient.chat(request.getModelId(), "GENERATE_REPORT",
                List.of(ChatMessage.system(prompt.text()),
                        ChatMessage.user("请生成测试报告并仅返回 JSON 对象。")),
                context);

        JsonNode root = jsonExtractor.extract(result.getContent());
        AiReport report = new AiReport();
        report.setPlanId(plan.getId());
        report.setModelId(request.getModelId());
        report.setReportType(request.getReportType() == null || request.getReportType().isBlank()
                ? "SUMMARY" : request.getReportType());
        report.setContentMd(text(root, "contentMd", "content"));
        report.setConclusion(text(root, "conclusion", "summary"));
        report.setRiskLevel(text(root, "riskLevel", "risk"));
        report.setReleaseAdvice(text(root, "releaseAdvice", "advice"));
        report.setCreatedBy(username);
        return reportRepository.save(report);
    }

    public List<AiReport> history(Long planId) {
        return reportRepository.findByPlanIdOrderByCreatedAtDesc(planId);
    }

    public AiReport latest(Long planId) {
        return reportRepository.findFirstByPlanIdOrderByCreatedAtDesc(planId)
                .orElseThrow(() -> new BizException(404, "该计划暂无 AI 报告"));
    }

    private String renderDetails(List<TestRun> runs) {
        if (runs == null || runs.isEmpty()) {
            return "暂无执行记录";
        }
        StringBuilder sb = new StringBuilder();
        for (TestRun run : runs) {
            if (!"PASS".equals(run.getStatus()) && run.getRemark() != null && !run.getRemark().isBlank()) {
                sb.append("- 用例ID ").append(run.getCaseId())
                        .append(" 状态 ").append(run.getStatus())
                        .append("，备注：").append(run.getRemark()).append("\n");
            }
        }
        return sb.length() == 0 ? "无失败或阻塞备注" : sb.toString();
    }

    private String text(JsonNode root, String primary, String fallback) {
        JsonNode node = root.get(primary);
        if (node == null) {
            node = root.get(fallback);
        }
        return node == null || node.isNull() ? null : node.asText();
    }
}
