package com.pyq.tcm.ai.client;

import com.pyq.tcm.ai.config.AiProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiCircuitBreakerTest {

    private AiCircuitBreaker breaker(int threshold, int openSeconds) {
        AiProperties properties = new AiProperties();
        properties.setCircuitFailureThreshold(threshold);
        properties.setCircuitOpenSeconds(openSeconds);
        return new AiCircuitBreaker(properties);
    }

    @Test
    void allowsTrafficByDefault() {
        AiCircuitBreaker breaker = breaker(5, 60);
        assertTrue(breaker.allow("m1"));
        assertFalse(breaker.isOpen("m1"));
    }

    @Test
    void opensAfterThresholdFailures() {
        AiCircuitBreaker breaker = breaker(3, 60);
        breaker.recordFailure("m1");
        breaker.recordFailure("m1");
        assertTrue(breaker.allow("m1"));
        assertFalse(breaker.isOpen("m1"));
        breaker.recordFailure("m1");
        assertTrue(breaker.isOpen("m1"));
        assertFalse(breaker.allow("m1"));
    }

    @Test
    void successResetsFailures() {
        AiCircuitBreaker breaker = breaker(3, 60);
        breaker.recordFailure("m1");
        breaker.recordFailure("m1");
        breaker.recordSuccess("m1");
        breaker.recordFailure("m1");
        assertFalse(breaker.isOpen("m1"));
        assertTrue(breaker.allow("m1"));
    }

    @Test
    void halfOpensAfterOpenSeconds() {
        AiCircuitBreaker breaker = breaker(1, 0);
        breaker.recordFailure("m1");
        assertTrue(breaker.isOpen("m1"));
        assertTrue(breaker.allow("m1"));
    }

    @Test
    void tracksKeysIndependently() {
        AiCircuitBreaker breaker = breaker(1, 60);
        breaker.recordFailure("m1");
        assertTrue(breaker.isOpen("m1"));
        assertFalse(breaker.isOpen("m2"));
    }
}
