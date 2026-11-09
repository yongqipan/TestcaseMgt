package com.pyq.tcm.ai.dto;

public class GenerationRequest {

    private Long projectId;
    private Long moduleId;
    private String requirement;
    private String fileRefs;
    private Long modelId;
    private String scene;

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

    public String getRequirement() {
        return requirement;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }

    public String getFileRefs() {
        return fileRefs;
    }

    public void setFileRefs(String fileRefs) {
        this.fileRefs = fileRefs;
    }

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }
}
