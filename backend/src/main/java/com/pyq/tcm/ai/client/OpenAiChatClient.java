package com.pyq.tcm.ai.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pyq.tcm.ai.config.AiProperties;
import com.pyq.tcm.ai.model.AiCallContext;
import com.pyq.tcm.ai.model.AiModelResolver;
import com.pyq.tcm.ai.model.ResolvedModel;
import com.pyq.tcm.ai.quota.AiQuotaService;
import com.pyq.tcm.ai.log.AiCallLogService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

@Component
public class OpenAiChatClient {

    private final RestClient restClient;
    private final AiModelResolver modelResolver;
    private final AiCircuitBreaker circuitBreaker;
    private final AiQuotaService quotaService;
    private final AiCallLogService callLogService;
    private final AiProperties properties;

    public OpenAiChatClient(RestClient aiRestClient,
                            AiModelResolver modelResolver,
                            AiCircuitBreaker circuitBreaker,
                            AiQuotaService quotaService,
                            AiCallLogService callLogService,
                            AiProperties properties) {
        this.restClient = aiRestClient;
        this.modelResolver = modelResolver;
        this.circuitBreaker = circuitBreaker;
        this.quotaService = quotaService;
        this.callLogService = callLogService;
        this.properties = properties;
    }

    public AiChatResult chat(Long modelId, String scene, List<ChatMessage> messages, AiCallContext context) {
        ResolvedModel model = modelResolver.resolve(modelId, scene);
        return chat(model, messages, context);
    }

    public AiChatResult chat(ResolvedModel model, List<ChatMessage> messages, AiCallContext context) {
        quotaService.assertAvailable();
        if (!circuitBreaker.allow(model.circuitKey())) {
            throw new AiException("CIRCUIT_OPEN", "AI 服务连续失败已触发熔断，请稍后重试或切换模型");
        }
        String traceId = context != null && context.getTraceId() != null
                ? context.getTraceId()
                : UUID.randomUUID().toString().replace("-", "");
        long start = System.currentTimeMillis();
        int attempt = 0;
        AiException lastError = null;
        while (attempt <= properties.getMaxRetries()) {
            attempt++;
            try {
                AiChatResult result = doCall(model, messages);
                circuitBreaker.recordSuccess(model.circuitKey());
                result.setLatencyMs((int) (System.currentTimeMillis() - start));
                quotaService.recordUsage(model, result);
                callLogService.logSuccess(model, result, traceId, sceneOf(context));
                return result;
            } catch (AiException ex) {
                lastError = ex;
                if (!isRetryable(ex) || attempt > properties.getMaxRetries()) {
                    break;
                }
            } catch (Exception ex) {
                lastError = new AiException("CALL_FAILED", "调用 AI 服务异常: " + ex.getMessage(), ex);
                if (attempt > properties.getMaxRetries()) {
                    break;
                }
            }
        }
        circuitBreaker.recordFailure(model.circuitKey());
        int latency = (int) (System.currentTimeMillis() - start);
        callLogService.logFailure(model, traceId, latency, lastError, sceneOf(context));
        throw lastError;
    }

    private AiChatResult doCall(ResolvedModel model, List<ChatMessage> messages) {
        ChatRequest request = new ChatRequest();
        request.setModel(model.getModelCode());
        request.setMessages(messages);
        request.setTemperature(model.getTemperature());
        request.setMax_tokens(model.getMaxTokens());
        request.setStream(Boolean.FALSE);

        String url = normalizeBaseUrl(model.getBaseUrl()) + "/chat/completions";
        ChatResponse response;
        try {
            response = restClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + model.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .onStatus(status -> status.isError(), (req, res) -> {
                        String body = res.getBody() == null ? "" : new String(res.getBody().readAllBytes());
                        throw new AiException("HTTP_" + res.getStatusCode().value(),
                                "AI 服务返回错误 " + res.getStatusCode().value() + ": " + truncate(body));
                    })
                    .body(ChatResponse.class);
        } catch (AiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AiException("NETWORK_ERROR", "网络异常: " + ex.getMessage(), ex);
        }
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            throw new AiException("EMPTY_RESPONSE", "AI 服务返回空结果");
        }
        ChatResponse.Choice choice = response.getChoices().get(0);
        String content = choice.getMessage() == null ? null : choice.getMessage().getContent();
        if (content == null || content.isBlank()) {
            throw new AiException("EMPTY_CONTENT", "AI 服务返回内容为空");
        }
        AiChatResult result = new AiChatResult();
        result.setContent(content);
        result.setModelCode(response.getModel() == null ? model.getModelCode() : response.getModel());
        result.setFinishReason(choice.getFinishReason());
        if (response.getUsage() != null) {
            result.setPromptTokens(response.getUsage().getPromptTokens());
            result.setCompletionTokens(response.getUsage().getCompletionTokens());
            result.setTotalTokens(response.getUsage().getTotalTokens());
        }
        return result;
    }

    private boolean isRetryable(AiException ex) {
        String code = ex.getErrorCode();
        return "NETWORK_ERROR".equals(code)
                || "HTTP_429".equals(code)
                || (code != null && code.startsWith("HTTP_5"));
    }

    private String sceneOf(AiCallContext context) {
        return context == null ? null : context.getScene();
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return properties.getDefaultBaseUrl();
        }
        String trimmed = baseUrl.trim();
        if (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private String truncate(String text) {
        if (text == null) {
            return "";
        }
        return text.length() > 300 ? text.substring(0, 300) : text;
    }
}
