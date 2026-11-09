package com.pyq.tcm.ai.quota;

import com.pyq.tcm.ai.client.AiChatResult;
import com.pyq.tcm.ai.client.AiException;
import com.pyq.tcm.ai.config.AiProperties;
import com.pyq.tcm.ai.entity.AiModel;
import com.pyq.tcm.ai.entity.AiQuota;
import com.pyq.tcm.ai.model.ResolvedModel;
import com.pyq.tcm.ai.repository.AiModelRepository;
import com.pyq.tcm.ai.repository.AiQuotaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class AiQuotaService {

    private static final Logger log = LoggerFactory.getLogger(AiQuotaService.class);
    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("yyyy-MM");

    private final AiQuotaRepository quotaRepository;
    private final AiModelRepository modelRepository;
    private final AiProperties properties;

    public AiQuotaService(AiQuotaRepository quotaRepository,
                          AiModelRepository modelRepository,
                          AiProperties properties) {
        this.quotaRepository = quotaRepository;
        this.modelRepository = modelRepository;
        this.properties = properties;
    }

    public String currentMonth() {
        return LocalDate.now().format(MONTH);
    }

    public AiQuota current() {
        String month = currentMonth();
        return quotaRepository.findByQuotaMonth(month).orElseGet(() -> createDefault(month));
    }

    public void assertAvailable() {
        AiQuota quota = current();
        if (quota.getTokenQuota() > 0 && quota.getTokenUsed() >= quota.getTokenQuota()) {
            throw new AiException("QUOTA_EXCEEDED", "本月 AI 调用配额已用尽，请联系管理员调整配额");
        }
    }

    @Transactional
    public void recordUsage(ResolvedModel model, AiChatResult result) {
        AiQuota quota = current();
        int totalTokens = result.getTotalTokens() == null ? 0 : result.getTotalTokens();
        quota.setTokenUsed(quota.getTokenUsed() + totalTokens);
        AiModel entity = modelRepository.findById(model.getModelId()).orElse(null);
        if (entity != null) {
            BigDecimal cost = estimateCost(entity, result);
            quota.setCostUsed(quota.getCostUsed().add(cost));
        }
        quotaRepository.save(quota);
        if (quota.getTokenQuota() > 0) {
            long percent = quota.getTokenUsed() * 100 / quota.getTokenQuota();
            if (percent >= quota.getAlertThreshold()) {
                log.warn("AI 月度配额使用已达 {}%（{}/{} tokens）",
                        percent, quota.getTokenUsed(), quota.getTokenQuota());
            }
        }
    }

    @Transactional
    public AiQuota update(com.pyq.tcm.ai.dto.QuotaRequest request) {
        AiQuota quota = current();
        if (request.getTokenQuota() != null) {
            quota.setTokenQuota(Math.max(0, request.getTokenQuota()));
        }
        if (request.getAlertThreshold() != null) {
            int threshold = Math.max(1, Math.min(100, request.getAlertThreshold()));
            quota.setAlertThreshold(threshold);
        }
        return quotaRepository.save(quota);
    }

    private BigDecimal estimateCost(AiModel model, AiChatResult result) {
        BigDecimal input = model.getInputPrice() == null ? BigDecimal.ZERO : model.getInputPrice();
        BigDecimal output = model.getOutputPrice() == null ? BigDecimal.ZERO : model.getOutputPrice();
        int promptTokens = result.getPromptTokens() == null ? 0 : result.getPromptTokens();
        int completionTokens = result.getCompletionTokens() == null ? 0 : result.getCompletionTokens();
        BigDecimal cost = input.multiply(BigDecimal.valueOf(promptTokens))
                .add(output.multiply(BigDecimal.valueOf(completionTokens)))
                .divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP);
        return cost;
    }

    private AiQuota createDefault(String month) {
        AiQuota quota = new AiQuota();
        quota.setQuotaMonth(month);
        quota.setTokenQuota(properties.getDefaultTokenQuota());
        quota.setTokenUsed(0L);
        quota.setCostUsed(BigDecimal.ZERO);
        quota.setAlertThreshold(properties.getAlertThreshold());
        return quotaRepository.save(quota);
    }
}
