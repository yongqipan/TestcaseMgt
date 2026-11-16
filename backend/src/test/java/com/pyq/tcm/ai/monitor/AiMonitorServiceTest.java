package com.pyq.tcm.ai.monitor;

import com.pyq.tcm.ai.repository.AiCallLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiMonitorServiceTest {

    @Mock
    private AiCallLogRepository callLogRepository;

    @Test
    void todayStatsAggregatesCallsTokensAndLatency() {
        when(callLogRepository.countByCreatedAtBetween(any(), any())).thenReturn(6L);
        when(callLogRepository.avgLatencyBetween(any(), any())).thenReturn(114.4);
        when(callLogRepository.sumTokensBetween(any(), any())).thenReturn(1120L);

        AiMonitorService service = new AiMonitorService(callLogRepository);
        Map<String, Object> stats = service.todayStats();

        assertEquals(6L, stats.get("calls"));
        assertEquals(1120L, stats.get("totalTokens"));
        assertEquals(114L, stats.get("avgLatencyMs"));
    }

    @Test
    void todayStatsHandlesEmptyData() {
        when(callLogRepository.countByCreatedAtBetween(any(), any())).thenReturn(0L);
        when(callLogRepository.avgLatencyBetween(any(), any())).thenReturn(null);
        when(callLogRepository.sumTokensBetween(any(), any())).thenReturn(null);

        AiMonitorService service = new AiMonitorService(callLogRepository);
        Map<String, Object> stats = service.todayStats();

        assertEquals(0L, stats.get("calls"));
        assertEquals(0L, stats.get("totalTokens"));
        assertEquals(0L, stats.get("avgLatencyMs"));
    }
}
