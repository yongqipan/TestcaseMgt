package com.pyq.tcm.ai.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_quota", uniqueConstraints = @UniqueConstraint(columnNames = "quota_month"))
public class AiQuota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quota_month", nullable = false, length = 7)
    private String quotaMonth;

    @Column(name = "token_quota", nullable = false)
    private Long tokenQuota;

    @Column(name = "token_used", nullable = false)
    private Long tokenUsed;

    @Column(name = "cost_used", nullable = false, precision = 12, scale = 4)
    private BigDecimal costUsed;

    @Column(name = "alert_threshold", nullable = false)
    private Integer alertThreshold;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (tokenQuota == null) {
            tokenQuota = 0L;
        }
        if (tokenUsed == null) {
            tokenUsed = 0L;
        }
        if (costUsed == null) {
            costUsed = BigDecimal.ZERO;
        }
        if (alertThreshold == null) {
            alertThreshold = 80;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuotaMonth() {
        return quotaMonth;
    }

    public void setQuotaMonth(String quotaMonth) {
        this.quotaMonth = quotaMonth;
    }

    public Long getTokenQuota() {
        return tokenQuota;
    }

    public void setTokenQuota(Long tokenQuota) {
        this.tokenQuota = tokenQuota;
    }

    public Long getTokenUsed() {
        return tokenUsed;
    }

    public void setTokenUsed(Long tokenUsed) {
        this.tokenUsed = tokenUsed;
    }

    public BigDecimal getCostUsed() {
        return costUsed;
    }

    public void setCostUsed(BigDecimal costUsed) {
        this.costUsed = costUsed;
    }

    public Integer getAlertThreshold() {
        return alertThreshold;
    }

    public void setAlertThreshold(Integer alertThreshold) {
        this.alertThreshold = alertThreshold;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
