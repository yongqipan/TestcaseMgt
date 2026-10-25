package com.pyq.tcm.controller;

import com.pyq.tcm.common.ApiResponse;
import com.pyq.tcm.common.PageResult;
import com.pyq.tcm.entity.TestCase;
import com.pyq.tcm.entity.TestPlan;
import com.pyq.tcm.entity.TestRun;
import com.pyq.tcm.service.TestPlanService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test-plans")
public class TestPlanController {

    private final TestPlanService testPlanService;

    public TestPlanController(TestPlanService testPlanService) {
        this.testPlanService = testPlanService;
    }

    @GetMapping
    public ApiResponse<PageResult<TestPlan>> page(@RequestParam(required = false) Long projectId,
                                                  @RequestParam(defaultValue = "") String keyword,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(testPlanService.page(projectId, keyword, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<TestPlan> get(@PathVariable Long id) {
        return ApiResponse.ok(testPlanService.get(id));
    }

    @GetMapping("/{id}/cases")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.ok(testPlanService.detail(id));
    }

    @GetMapping("/{id}/available-cases")
    public ApiResponse<PageResult<TestCase>> availableCases(@PathVariable Long id,
                                                            @RequestParam(required = false) Long moduleId,
                                                            @RequestParam(defaultValue = "") String keyword,
                                                            @RequestParam(defaultValue = "") String priority,
                                                            @RequestParam(defaultValue = "") String status,
                                                            @RequestParam(defaultValue = "1") int page,
                                                            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(testPlanService.availableCases(id, moduleId, keyword, priority, status, page, size));
    }

    @PostMapping("/{id}/cases")
    public ApiResponse<Map<String, Object>> addCases(@PathVariable Long id,
                                                     @RequestBody CaseIdsRequest request) {
        int added = testPlanService.addCases(id, request.caseIds());
        return ApiResponse.ok(Map.of("added", added));
    }

    @DeleteMapping("/{id}/cases/{caseId}")
    public ApiResponse<Void> removeCase(@PathVariable Long id, @PathVariable Long caseId) {
        testPlanService.removeCase(id, caseId);
        return ApiResponse.ok();
    }

    @PostMapping
    public ApiResponse<TestPlan> create(@Valid @RequestBody TestPlan plan, HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        return ApiResponse.ok(testPlanService.create(plan, username));
    }

    @PutMapping("/{id}")
    public ApiResponse<TestPlan> update(@PathVariable Long id, @Valid @RequestBody TestPlan plan) {
        return ApiResponse.ok(testPlanService.update(id, plan));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        testPlanService.delete(id);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/runs")
    public ApiResponse<TestRun> submitRun(@PathVariable Long id,
                                          @RequestBody RunRequest request,
                                          HttpServletRequest httpRequest) {
        String username = (String) httpRequest.getAttribute("username");
        String executor = request.executor() == null || request.executor().isBlank()
                ? username : request.executor();
        return ApiResponse.ok(testPlanService.submitRun(id, request.caseId(), request.status(),
                request.remark(), executor));
    }

    @GetMapping("/{id}/stats")
    public ApiResponse<Map<String, Object>> stats(@PathVariable Long id) {
        return ApiResponse.ok(testPlanService.stats(id));
    }

    public record RunRequest(Long caseId, String status, String remark, String executor) {
    }

    public record CaseIdsRequest(List<Long> caseIds) {
    }
}
