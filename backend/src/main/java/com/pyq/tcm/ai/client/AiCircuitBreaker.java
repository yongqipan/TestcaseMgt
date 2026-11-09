package com.pyq.tcm.ai.client;

import com.pyq.tcm.ai.config.AiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class AiCircuitBreaker {

    private static final Logger log = LoggerFactory.getLogger(AiCircuitBreaker.class);

    private final AiProperties properties;
    private final Map<String, State> states = new ConcurrentHashMap<>();

    public AiCircuitBreaker(AiProperties properties) {
        this.properties = properties;
    }

    public boolean allow(String key) {
        State state = states.computeIfAbsent(key, k -> new State());
        if (state.openedAt == null) {
            return true;
        }
        long elapsed = Instant.now().getEpochSecond() - state.openedAt;
        if (elapsed >= properties.getCircuitOpenSeconds()) {
            state.openedAt = null;
            state.failures.set(0);
            log.info("AI 熔断器 {} 尝试半开恢复", key);
            return true;
        }
        return false;
    }

    public void recordSuccess(String key) {
        State state = states.computeIfAbsent(key, k -> new State());
        state.failures.set(0);
        state.openedAt = null;
    }

    public void recordFailure(String key) {
        State state = states.computeIfAbsent(key, k -> new State());
        int failures = state.failures.incrementAndGet();
        if (failures >= properties.getCircuitFailureThreshold() && state.openedAt == null) {
            state.openedAt = Instant.now().getEpochSecond();
            log.warn("AI 熔断器 {} 已打开，持续失败次数 {}", key, failures);
        }
    }

    public boolean isOpen(String key) {
        State state = states.get(key);
        return state != null && state.openedAt != null;
    }

    private static class State {
        private final AtomicInteger failures = new AtomicInteger(0);
        private volatile Long openedAt;
    }
}
