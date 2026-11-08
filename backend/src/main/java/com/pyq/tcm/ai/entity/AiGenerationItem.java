package com.pyq.tcm.ai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pyq.tcm.entity.CaseStep;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ai_generation_item")
public class AiGenerationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "generation_id", nullable = false)
    private Long generationId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 10)
    private String priority;

    @Column(length = 20)
    private String type;

    @Column(columnDefinition = "TEXT")
    private String precondition;

    @Column(name = "steps_json", columnDefinition = "TEXT")
    private String stepsJson;

    @Column(columnDefinition = "TEXT")
    private String expected;

    private Integer confidence;

    @Column(nullable = false)
    private Boolean selected;

    @Column(name = "accepted_case_id")
    private Long acceptedCaseId;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (selected == null) {
            selected = Boolean.TRUE;
        }
        if (status == null) {
            status = "PENDING";
        }
    }

    @Transient
    public List<CaseStep> getSteps() {
        return StepsJsonCodec.decode(stepsJson);
    }

    public void setSteps(List<CaseStep> steps) {
        this.stepsJson = StepsJsonCodec.encode(steps);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGenerationId() {
        return generationId;
    }

    public void setGenerationId(Long generationId) {
        this.generationId = generationId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPrecondition() {
        return precondition;
    }

    public void setPrecondition(String precondition) {
        this.precondition = precondition;
    }

    @JsonIgnore
    public String getStepsJson() {
        return stepsJson;
    }

    public void setStepsJson(String stepsJson) {
        this.stepsJson = stepsJson;
    }

    public String getExpected() {
        return expected;
    }

    public void setExpected(String expected) {
        this.expected = expected;
    }

    public Integer getConfidence() {
        return confidence;
    }

    public void setConfidence(Integer confidence) {
        this.confidence = confidence;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public Long getAcceptedCaseId() {
        return acceptedCaseId;
    }

    public void setAcceptedCaseId(Long acceptedCaseId) {
        this.acceptedCaseId = acceptedCaseId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
