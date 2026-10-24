package com.pyq.tcm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "test_cases", uniqueConstraints = @UniqueConstraint(columnNames = "case_no"))
public class TestCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "所属项目不能为空")
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "module_id")
    private Long moduleId;

    @Column(name = "case_no", length = 50)
    private String caseNo;

    @NotBlank(message = "用例标题不能为空")
    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 10)
    private String priority;

    @Column(length = 20)
    private String type;

    @Column(length = 20)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String precondition;

    @Convert(converter = StepsConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<CaseStep> steps = new ArrayList<>();

    @Column(length = 200)
    private String tags;

    @Column(length = 50)
    private String creator;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (priority == null) {
            priority = "P1";
        }
        if (type == null) {
            type = "FUNCTIONAL";
        }
        if (status == null) {
            status = "ACTIVE";
        }
        if (steps == null) {
            steps = new ArrayList<>();
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

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getModuleId() {
        return moduleId;
    }

    public void setModuleId(Long moduleId) {
        this.moduleId = moduleId;
    }

    public String getCaseNo() {
        return caseNo;
    }

    public void setCaseNo(String caseNo) {
        this.caseNo = caseNo;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPrecondition() {
        return precondition;
    }

    public void setPrecondition(String precondition) {
        this.precondition = precondition;
    }

    public List<CaseStep> getSteps() {
        return steps;
    }

    public void setSteps(List<CaseStep> steps) {
        this.steps = steps;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
