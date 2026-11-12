package com.pyq.tcm.ai.quota;

import com.pyq.tcm.ai.client.AiChatResult;
import com.pyq.tcm.ai.client.AiException;
import com.pyq.tcm.ai.config.AiProperties;
import com.pyq.tcm.ai.entity.AiModel;
import com.pyq.tcm.ai.entity.AiQuota;
import com.pyq.tcm.ai.model.ResolvedModel;
import com.pyq.tcm.ai.repository.AiModelRepository;
import com.pyq.tcm.ai.repository.AiQuotaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiQuotaServiceTest {

    @Mock
    private AiQuotaRepository quotaRepository;

    @Mock
    private AiModelRepository modelRepository;

    private AiQuotaService service;

    @BeforeEach
    void setUp() {
        AiProperties properties = new AiProperties();
        properties.setDefaultTokenQuota(0L);
        properties.setAlertThreshold(80);
        service = new AiQuotaService(quotaRepository, modelRepository, properties);
    }

    private AiQuota quota(long limit, long used) {
        AiQuota quota = new AiQuota();
        quota.setQuotaMonth(service.currentMonth());
        quota.setTokenQuota(limit);
        quota.setTokenUsed(used);
        quota.setCostUsed(BigDecimal.ZERO);
        quota.setAlertThreshold(80);
        return quota;
    }

    @Test
    void assertAvailablePassesWhenQuotaUnlimited() {
        when(quotaRepository.findByQuotaMonth(anyString())).thenReturn(Optional.of(quota(0, 9999)));
        assertDoesNotThrow(() -> service.assertAvailable());
    }

    @Test
    void assertAvailablePassesWhenUnderLimit() {
        when(quotaRepository.findByQuotaMonth(anyString())).thenReturn(Optional.of(quota(1000, 500)));
        assertDoesNotThrow(() -> service.assertAvailable());
    }

    @Test
    void assertAvailableThrowsWhenExhausted() {
        when(quotaRepository.findByQuotaMonth(anyString())).thenReturn(Optional.of(quota(1000, 1000)));
        AiException ex = assertThrows(AiException.class, () -> service.assertAvailable());
        assertEquals("QUOTA_EXCEEDED", ex.getErrorCode());
    }

    @Test
    void recordUsageAccumulatesTokensAndEstimatesCost() {
        AiQuota existing = quota(0, 100);
        when(quotaRepository.findByQuotaMonth(anyString())).thenReturn(Optional.of(existing));
        AiModel model = new AiModel();
        model.setId(1L);
        model.setInputPrice(new BigDecimal("0.01"));
        model.setOutputPrice(new BigDecimal("0.02"));
        when(modelRepository.findById(1L)).thenReturn(Optional.of(model));

        AiChatResult result = new AiChatResult();
        result.setPromptTokens(500);
        result.setCompletionTokens(300);
        result.setTotalTokens(800);

        ResolvedModel resolved = new ResolvedModel(1L, 1L, "m", "code", "provider",
                "http://x", "key", 0.2, 2000);
        service.recordUsage(resolved, result);

        assertEquals(900L, existing.getTokenUsed());
        assertEquals(new BigDecimal("0.0110"), existing.getCostUsed());
    }

    @Test
    void createDefaultQuotaWhenMonthMissing() {
        when(quotaRepository.findByQuotaMonth(anyString())).thenReturn(Optional.empty());
        when(quotaRepository.save(any(AiQuota.class))).thenAnswer(inv -> inv.getArgument(0));
        AiQuota created = service.current();
        assertEquals(service.currentMonth(), created.getQuotaMonth());
        assertEquals(0L, created.getTokenUsed());
        assertEquals(BigDecimal.ZERO, created.getCostUsed());
    }
}
