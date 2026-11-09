package com.pyq.tcm.ai.monitor;

import com.pyq.tcm.ai.entity.AiCallLog;
import com.pyq.tcm.ai.repository.AiCallLogRepository;
import com.pyq.tcm.common.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiMonitorService {

    private final AiCallLogRepository callLogRepository;

    public AiMonitorService(AiCallLogRepository callLogRepository) {
        this.callLogRepository = callLogRepository;
    }

    public PageResult<AiCallLog> logs(String scene, String modelName, String status, String username,
                                      int page, int size) {
        Page<AiCallLog> result = callLogRepository.search(
                scene == null ? "" : scene,
                modelName == null ? "" : modelName,
                status == null ? "" : status,
                username == null ? "" : username,
                PageRequest.of(Math.max(page, 1) - 1, Math.min(Math.max(size, 1), 100),
                        Sort.by(Sort.Direction.DESC, "createdAt")));
        return PageResult.of(result);
    }

    public Map<String, Object> todayStats() {
        LocalDateTime from = LocalDate.now().atStartOfDay();
        LocalDateTime to = from.plusDays(1);
        Object[] summary = callLogRepository.summaryBetween(from, to);
        long calls = 0;
        double avgLatency = 0;
        if (summary != null && summary.length == 2 && summary[0] != null) {
            calls = ((Number) summary[0]).longValue();
            avgLatency = summary[1] == null ? 0 : ((Number) summary[1]).doubleValue();
        }
        Long tokens = callLogRepository.sumTokensBetween(from, to);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("calls", calls);
        result.put("totalTokens", tokens == null ? 0L : tokens);
        result.put("avgLatencyMs", Math.round(avgLatency));
        return result;
    }

    public List<Map<String, Object>> tokensByScene(int days) {
        int span = Math.max(1, Math.min(days, 90));
        LocalDateTime from = LocalDate.now().minusDays(span - 1L).atStartOfDay();
        LocalDateTime to = LocalDate.now().plusDays(1).atStartOfDay();
        return callLogRepository.sumTokensByScene(from, to).stream()
                .map(row -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("scene", row[0]);
                    item.put("tokens", row[1] == null ? 0L : ((Number) row[1]).longValue());
                    return item;
                })
                .toList();
    }
}
