package com.pyq.tcm.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "tcm.ai")
public class AiProperties {

    private int connectTimeoutMs = 5000;
    private int readTimeoutMs = 60000;
    private int maxRetries = 1;
    private int circuitFailureThreshold = 5;
    private int circuitOpenSeconds = 60;
    private long defaultTokenQuota = 0L;
    private int alertThreshold = 80;
    private String cryptoSecret = "";
    private boolean allowInsecureCrypto = true;
    private double defaultTemperature = 0.2;
    private int defaultMaxTokens = 2000;
    private String defaultBaseUrl = "https://api.openai.com/v1";
    private int taskPoolSize = 2;
    private int taskQueueCapacity = 20;
    private int rateLimitMaxRequests = 10;
    private long rateLimitWindowSeconds = 60;
    private int maxRequirementLength = 8000;

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getCircuitFailureThreshold() {
        return circuitFailureThreshold;
    }

    public void setCircuitFailureThreshold(int circuitFailureThreshold) {
        this.circuitFailureThreshold = circuitFailureThreshold;
    }

    public int getCircuitOpenSeconds() {
        return circuitOpenSeconds;
    }

    public void setCircuitOpenSeconds(int circuitOpenSeconds) {
        this.circuitOpenSeconds = circuitOpenSeconds;
    }

    public long getDefaultTokenQuota() {
        return defaultTokenQuota;
    }

    public void setDefaultTokenQuota(long defaultTokenQuota) {
        this.defaultTokenQuota = defaultTokenQuota;
    }

    public int getAlertThreshold() {
        return alertThreshold;
    }

    public void setAlertThreshold(int alertThreshold) {
        this.alertThreshold = alertThreshold;
    }

    public String getCryptoSecret() {
        return cryptoSecret;
    }

    public void setCryptoSecret(String cryptoSecret) {
        this.cryptoSecret = cryptoSecret;
    }

    public boolean isAllowInsecureCrypto() {
        return allowInsecureCrypto;
    }

    public void setAllowInsecureCrypto(boolean allowInsecureCrypto) {
        this.allowInsecureCrypto = allowInsecureCrypto;
    }

    public double getDefaultTemperature() {
        return defaultTemperature;
    }

    public void setDefaultTemperature(double defaultTemperature) {
        this.defaultTemperature = defaultTemperature;
    }

    public int getDefaultMaxTokens() {
        return defaultMaxTokens;
    }

    public void setDefaultMaxTokens(int defaultMaxTokens) {
        this.defaultMaxTokens = defaultMaxTokens;
    }

    public String getDefaultBaseUrl() {
        return defaultBaseUrl;
    }

    public void setDefaultBaseUrl(String defaultBaseUrl) {
        this.defaultBaseUrl = defaultBaseUrl;
    }

    public int getTaskPoolSize() {
        return taskPoolSize;
    }

    public void setTaskPoolSize(int taskPoolSize) {
        this.taskPoolSize = taskPoolSize;
    }

    public int getTaskQueueCapacity() {
        return taskQueueCapacity;
    }

    public void setTaskQueueCapacity(int taskQueueCapacity) {
        this.taskQueueCapacity = taskQueueCapacity;
    }

    public int getRateLimitMaxRequests() {
        return rateLimitMaxRequests;
    }

    public void setRateLimitMaxRequests(int rateLimitMaxRequests) {
        this.rateLimitMaxRequests = rateLimitMaxRequests;
    }

    public long getRateLimitWindowSeconds() {
        return rateLimitWindowSeconds;
    }

    public void setRateLimitWindowSeconds(long rateLimitWindowSeconds) {
        this.rateLimitWindowSeconds = rateLimitWindowSeconds;
    }

    public int getMaxRequirementLength() {
        return maxRequirementLength;
    }

    public void setMaxRequirementLength(int maxRequirementLength) {
        this.maxRequirementLength = maxRequirementLength;
    }
}
