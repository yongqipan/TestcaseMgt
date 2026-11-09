package com.pyq.tcm.ai.controller;

import com.pyq.tcm.ai.dto.AcceptRequest;
import com.pyq.tcm.ai.dto.GenerationRequest;
import com.pyq.tcm.ai.entity.AiGeneration;
import com.pyq.tcm.ai.generation.AiGenerationAcceptService;
import com.pyq.tcm.ai.generation.AiGenerationService;
import com.pyq.tcm.common.ApiResponse;
import com.pyq.tcm.common.PageResult;
import com.pyq.tcm.entity.TestCase;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/generations")
public class AiGenerationController {

    private final AiGenerationService generationService;
    private final AiGenerationAcceptService acceptService;

    public AiGenerationController(AiGenerationService generationService,
                                  AiGenerationAcceptService acceptService) {
        this.generationService = generationService;
        this.acceptService = acceptService;
    }

    @GetMapping
    public ApiResponse<PageResult<AiGeneration>> page(@RequestParam Long projectId,
                                                      @RequestParam(required = false) String scene,
                                                      @RequestParam(required = false) String status,
                                                      @RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(generationService.page(projectId, scene, status, page, size));
    }

    @PostMapping
    public ApiResponse<AiGeneration> create(@RequestBody GenerationRequest request,
                                            HttpServletRequest httpRequest) {
        String username = (String) httpRequest.getAttribute("username");
        AiGeneration generation = generationService.createTask(request, username);
        generationService.runTask(generation.getId());
        return ApiResponse.ok(generation);
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.ok(generationService.detail(id));
    }

    @PostMapping("/{id}/accept")
    public ApiResponse<List<TestCase>> accept(@PathVariable Long id,
                                              @RequestBody(required = false) AcceptRequest request,
                                              HttpServletRequest httpRequest) {
        String username = (String) httpRequest.getAttribute("username");
        return ApiResponse.ok(acceptService.accept(id, request, username));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        generationService.deleteTask(id);
        return ApiResponse.ok();
    }
}
