package com.pyq.tcm.ai.review;

import com.fasterxml.jackson.databind.JsonNode;
import com.pyq.tcm.ai.client.AiChatResult;
import com.pyq.tcm.ai.client.AiException;
import com.pyq.tcm.ai.client.ChatMessage;
import com.pyq.tcm.ai.client.OpenAiChatClient;
import com.pyq.tcm.ai.dto.ReviewRequest;
import com.pyq.tcm.ai.entity.AiReview;
import com.pyq.tcm.ai.model.AiCallContext;
import com.pyq.tcm.ai.prompt.AiJsonExtractor;
import com.pyq.tcm.ai.prompt.AiPromptService;
import com.pyq.tcm.ai.repository.AiReviewRepository;
import com.pyq.tcm.common.BizException;
import com.pyq.tcm.entity.CaseStep;
import com.pyq.tcm.entity.TestCase;
import com.pyq.tcm.repository.TestCaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiReviewService {

    private final AiReviewRepository reviewRepository;
    private final TestCaseRepository testCaseRepository;
    private final AiPromptService promptService;
    private final AiJsonExtractor jsonExtractor;
    private final OpenAiChatClient chatClient;

    public AiReviewService(AiReviewRepository reviewRepository,
                           TestCaseRepository testCaseRepository,
                           AiPromptService promptService,
                           AiJsonExtractor jsonExtractor,
                           OpenAiChatClient chatClient) {
        this.reviewRepository = reviewRepository;
        this.testCaseRepository = testCaseRepository;
        this.promptService = promptService;
        this.jsonExtractor = jsonExtractor;
        this.chatClient = chatClient;
    }

    @Transactional
    public AiReview review(ReviewRequest request, String username) {
        if (request.getCaseId() == null) {
            throw new BizException("请选择要评审的用例");
        }
        TestCase testCase = testCaseRepository.findById(request.getCaseId())
                .orElseThrow(() -> new BizException(404, "测试用例不存在"));

        Map<String, String> variables = new HashMap<>();
        variables.put("title", testCase.getTitle());
        variables.put("precondition", nullSafe(testCase.getPrecondition()));
        variables.put("steps", renderSteps(testCase.getSteps()));
        variables.put("expected", nullSafe(lastExpected(testCase.getSteps())));
        AiPromptService.RenderedPrompt prompt = promptService.render("REVIEW_CASE", variables);

        AiCallContext context = new AiCallContext("REVIEW_CASE", "CASE",
                testCase.getId(), null, username, null);
        AiChatResult result = chatClient.chat(request.getModelId(), "REVIEW_CASE",
                List.of(ChatMessage.system(prompt.text()),
                        ChatMessage.user("请评审该测试用例并仅返回 JSON 对象。")),
                context);

        JsonNode root = jsonExtractor.extract(result.getContent());
        int totalScore = clampScore(intValue(root, "totalScore", "score"));
        String dimensionJson = root.has("dimensions") ? root.get("dimensions").toString()
                : root.has("dimension") ? root.get("dimension").toString() : "{}";
        String suggestions = root.has("suggestions")
                ? root.get("suggestions").toString()
                : "[]";

        AiReview review = new AiReview();
        review.setCaseId(testCase.getId());
        review.setModelId(request.getModelId());
        review.setTotalScore(totalScore);
        review.setDimensionJson(dimensionJson);
        review.setSuggestions(suggestions);
        review.setCreatedBy(username);
        AiReview saved = reviewRepository.save(review);

        testCase.setAiReviewScore(totalScore);
        testCaseRepository.save(testCase);
        return saved;
    }

    public List<AiReview> history(Long caseId) {
        return reviewRepository.findByCaseIdOrderByCreatedAtDesc(caseId);
    }

    private String renderSteps(List<CaseStep> steps) {
        if (steps == null || steps.isEmpty()) {
            return "无";
        }
        StringBuilder sb = new StringBuilder();
        int index = 1;
        for (CaseStep step : steps) {
            sb.append(index++).append(". 操作：").append(nullSafe(step.getAction()))
                    .append("；预期：").append(nullSafe(step.getExpected())).append("\n");
        }
        return sb.toString();
    }

    private String lastExpected(List<CaseStep> steps) {
        if (steps == null || steps.isEmpty()) {
            return "";
        }
        CaseStep last = steps.get(steps.size() - 1);
        return last.getExpected();
    }

    private int clampScore(int score) {
        return Math.max(0, Math.min(100, score));
    }

    private int intValue(JsonNode root, String primary, String fallback) {
        JsonNode node = root.get(primary);
        if (node == null) {
            node = root.get(fallback);
        }
        if (node == null || !node.isNumber()) {
            throw new AiException("INVALID_REVIEW", "AI 评审结果缺少有效评分");
        }
        return node.asInt();
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    public Map<String, Object> view(AiReview review) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", review.getId());
        view.put("caseId", review.getCaseId());
        view.put("totalScore", review.getTotalScore());
        view.put("dimensions", review.getDimensionJson());
        view.put("suggestions", review.getSuggestions());
        view.put("createdBy", review.getCreatedBy());
        view.put("createdAt", review.getCreatedAt());
        return view;
    }
}
