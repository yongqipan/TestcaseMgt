package com.pyq.tcm.ai.dto;

public class QuotaRequest {

    private Long tokenQuota;
    private Integer alertThreshold;

    public Long getTokenQuota() {
        return tokenQuota;
    }

    public void setTokenQuota(Long tokenQuota) {
        this.tokenQuota = tokenQuota;
    }

    public Integer getAlertThreshold() {
        return alertThreshold;
    }

    public void setAlertThreshold(Integer alertThreshold) {
        this.alertThreshold = alertThreshold;
    }
}
