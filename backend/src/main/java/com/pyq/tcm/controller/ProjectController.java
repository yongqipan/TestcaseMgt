package com.pyq.tcm.controller;

import com.pyq.tcm.common.ApiResponse;
import com.pyq.tcm.common.PageResult;
import com.pyq.tcm.entity.Project;
import com.pyq.tcm.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ApiResponse<PageResult<Project>> page(@RequestParam(defaultValue = "") String keyword,
                                                 @RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(projectService.page(keyword, page, size));
    }

    @GetMapping("/all")
    public ApiResponse<List<Project>> all() {
        return ApiResponse.ok(projectService.all());
    }

    @GetMapping("/{id}")
    public ApiResponse<Project> get(@PathVariable Long id) {
        return ApiResponse.ok(projectService.get(id));
    }

    @PostMapping
    public ApiResponse<Project> create(@Valid @RequestBody Project project) {
        return ApiResponse.ok(projectService.create(project));
    }

    @PutMapping("/{id}")
    public ApiResponse<Project> update(@PathVariable Long id, @Valid @RequestBody Project project) {
        return ApiResponse.ok(projectService.update(id, project));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        projectService.delete(id);
        return ApiResponse.ok();
    }
}
