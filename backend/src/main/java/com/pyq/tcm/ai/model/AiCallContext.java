package com.pyq.tcm.ai.model;

public class AiCallContext {

    private final String scene;
    private final String bizType;
    private final Long bizId;
    private final Long userId;
    private final String username;
    private final String traceId;

    public AiCallContext(String scene, String bizType, Long bizId, Long userId, String username, String traceId) {
        this.scene = scene;
        this.bizType = bizType;
        this.bizId = bizId;
        this.userId = userId;
        this.username = username;
        this.traceId = traceId;
    }

    public String getScene() {
        return scene;
    }

    public String getBizType() {
        return bizType;
    }

    public Long getBizId() {
        return bizId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getTraceId() {
        return traceId;
    }
}
