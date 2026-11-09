package com.pyq.tcm.ai.log;

import com.pyq.tcm.ai.client.AiChatResult;
import com.pyq.tcm.ai.client.AiException;
import com.pyq.tcm.ai.entity.AiCallLog;
import com.pyq.tcm.ai.model.ResolvedModel;
import com.pyq.tcm.ai.repository.AiCallLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AiCallLogService {

    private static final Logger log = LoggerFactory.getLogger(AiCallLogService.class);

    private final AiCallLogRepository callLogRepository;

    public AiCallLogService(AiCallLogRepository callLogRepository) {
        this.callLogRepository = callLogRepository;
    }

    public void logSuccess(ResolvedModel model, AiChatResult result, String traceId, String scene) {
        try {
            AiCallLog entity = baseLog(model, traceId, scene);
            entity.setStatus("SUCCESS");
            entity.setPromptTokens(result.getPromptTokens());
            entity.setCompletionTokens(result.getCompletionTokens());
            entity.setTotalTokens(result.getTotalTokens());
            entity.setLatencyMs(result.getLatencyMs());
            callLogRepository.save(entity);
        } catch (Exception e) {
            log.warn("写入 AI 调用成功日志失败: {}", e.getMessage());
        }
    }

    public void logFailure(ResolvedModel model, String traceId, int latency, AiException error, String scene) {
        try {
            AiCallLog entity = baseLog(model, traceId, scene);
            entity.setStatus("FAILED");
            entity.setLatencyMs(latency);
            if (error != null) {
                entity.setErrorCode(error.getErrorCode());
                entity.setErrorMsg(truncate(error.getMessage()));
            }
            callLogRepository.save(entity);
        } catch (Exception e) {
            log.warn("写入 AI 调用失败日志失败: {}", e.getMessage());
        }
    }

    private AiCallLog baseLog(ResolvedModel model, String traceId, String scene) {
        AiCallLog entity = new AiCallLog();
        entity.setTraceId(traceId);
        entity.setScene(scene);
        entity.setModelId(model.getModelId());
        entity.setModelName(model.getModelName());
        return entity;
    }

    private String truncate(String text) {
        if (text == null) {
            return null;
        }
        return text.length() > 500 ? text.substring(0, 500) : text;
    }
}
