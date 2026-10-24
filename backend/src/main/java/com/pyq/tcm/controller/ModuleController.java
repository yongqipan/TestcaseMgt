package com.pyq.tcm.controller;

import com.pyq.tcm.common.ApiResponse;
import com.pyq.tcm.entity.Module;
import com.pyq.tcm.service.ModuleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/modules")
public class ModuleController {

    private final ModuleService moduleService;

    public ModuleController(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    @GetMapping("/tree")
    public ApiResponse<List<Map<String, Object>>> tree(@RequestParam Long projectId) {
        return ApiResponse.ok(moduleService.tree(projectId));
    }

    @PostMapping
    public ApiResponse<Module> create(@Valid @RequestBody Module module) {
        return ApiResponse.ok(moduleService.create(module));
    }

    @PutMapping("/{id}")
    public ApiResponse<Module> update(@PathVariable Long id, @Valid @RequestBody Module module) {
        return ApiResponse.ok(moduleService.update(id, module));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        moduleService.delete(id);
        return ApiResponse.ok();
    }
}
