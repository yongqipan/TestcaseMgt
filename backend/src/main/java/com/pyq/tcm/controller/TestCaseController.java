package com.pyq.tcm.controller;

import com.pyq.tcm.common.ApiResponse;
import com.pyq.tcm.common.PageResult;
import com.pyq.tcm.entity.TestCase;
import com.pyq.tcm.service.TestCaseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test-cases")
public class TestCaseController {

    private final TestCaseService testCaseService;

    public TestCaseController(TestCaseService testCaseService) {
        this.testCaseService = testCaseService;
    }

    @GetMapping
    public ApiResponse<PageResult<TestCase>> page(@RequestParam Long projectId,
                                                  @RequestParam(required = false) Long moduleId,
                                                  @RequestParam(defaultValue = "") String keyword,
                                                  @RequestParam(defaultValue = "") String priority,
                                                  @RequestParam(defaultValue = "") String status,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(testCaseService.page(projectId, moduleId, keyword, priority, status, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<TestCase> get(@PathVariable Long id) {
        return ApiResponse.ok(testCaseService.get(id));
    }

    @PostMapping
    public ApiResponse<TestCase> create(@Valid @RequestBody TestCase testCase, HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        return ApiResponse.ok(testCaseService.create(testCase, username));
    }

    @PutMapping("/{id}")
    public ApiResponse<TestCase> update(@PathVariable Long id, @Valid @RequestBody TestCase testCase) {
        return ApiResponse.ok(testCaseService.update(id, testCase));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        testCaseService.delete(id);
        return ApiResponse.ok();
    }
}
