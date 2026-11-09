package com.pyq.tcm.ai.model;

public class ResolvedModel {

    private final Long modelId;
    private final Long providerId;
    private final String modelName;
    private final String modelCode;
    private final String providerName;
    private final String baseUrl;
    private final String apiKey;
    private final Double temperature;
    private final Integer maxTokens;

    public ResolvedModel(Long modelId, Long providerId, String modelName, String modelCode,
                         String providerName, String baseUrl, String apiKey,
                         Double temperature, Integer maxTokens) {
        this.modelId = modelId;
        this.providerId = providerId;
        this.modelName = modelName;
        this.modelCode = modelCode;
        this.providerName = providerName;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
    }

    public Long getModelId() {
        return modelId;
    }

    public Long getProviderId() {
        return providerId;
    }

    public String getModelName() {
        return modelName;
    }

    public String getModelCode() {
        return modelCode;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public Double getTemperature() {
        return temperature;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public String circuitKey() {
        return providerId + ":" + modelId;
    }
}
