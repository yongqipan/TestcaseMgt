package com.pyq.tcm.ai.controller;

import com.pyq.tcm.ai.admin.AiModelAdminService;
import com.pyq.tcm.ai.admin.AiPromptAdminService;
import com.pyq.tcm.ai.admin.AiProviderService;
import com.pyq.tcm.ai.client.AiConnectionTester;
import com.pyq.tcm.ai.dto.ModelRequest;
import com.pyq.tcm.ai.dto.PromptRequest;
import com.pyq.tcm.ai.dto.ProviderRequest;
import com.pyq.tcm.ai.dto.QuotaRequest;
import com.pyq.tcm.ai.entity.AiPromptTemplate;
import com.pyq.tcm.ai.quota.AiQuotaService;
import com.pyq.tcm.ai.entity.AiQuota;
import com.pyq.tcm.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/admin")
public class AiAdminController {

    private final AiProviderService providerService;
    private final AiModelAdminService modelService;
    private final AiPromptAdminService promptService;
    private final AiConnectionTester connectionTester;
    private final AiQuotaService quotaService;

    public AiAdminController(AiProviderService providerService,
                             AiModelAdminService modelService,
                             AiPromptAdminService promptService,
                             AiConnectionTester connectionTester,
                             AiQuotaService quotaService) {
        this.providerService = providerService;
        this.modelService = modelService;
        this.promptService = promptService;
        this.connectionTester = connectionTester;
        this.quotaService = quotaService;
    }

    @GetMapping("/providers")
    public ApiResponse<List<Map<String, Object>>> providers() {
        return ApiResponse.ok(providerService.list());
    }

    @PostMapping("/providers")
    public ApiResponse<Map<String, Object>> createProvider(@RequestBody ProviderRequest request) {
        return ApiResponse.ok(providerService.create(request));
    }

    @PutMapping("/providers/{id}")
    public ApiResponse<Map<String, Object>> updateProvider(@PathVariable Long id,
                                                           @RequestBody ProviderRequest request) {
        return ApiResponse.ok(providerService.update(id, request));
    }

    @DeleteMapping("/providers/{id}")
    public ApiResponse<Void> deleteProvider(@PathVariable Long id) {
        providerService.delete(id);
        return ApiResponse.ok();
    }

    @GetMapping("/models")
    public ApiResponse<List<Map<String, Object>>> models() {
        return ApiResponse.ok(modelService.list());
    }

    @PostMapping("/models")
    public ApiResponse<Map<String, Object>> createModel(@RequestBody ModelRequest request) {
        return ApiResponse.ok(modelService.create(request));
    }

    @PutMapping("/models/{id}")
    public ApiResponse<Map<String, Object>> updateModel(@PathVariable Long id,
                                                        @RequestBody ModelRequest request) {
        return ApiResponse.ok(modelService.update(id, request));
    }

    @DeleteMapping("/models/{id}")
    public ApiResponse<Void> deleteModel(@PathVariable Long id) {
        modelService.delete(id);
        return ApiResponse.ok();
    }

    @PostMapping("/models/{id}/test")
    public ApiResponse<AiConnectionTester.ProbeResult> testModel(@PathVariable Long id) {
        return ApiResponse.ok(connectionTester.test(id));
    }

    @GetMapping("/prompts")
    public ApiResponse<List<AiPromptTemplate>> prompts(@RequestParam(required = false) String scene) {
        if (scene == null || scene.isBlank()) {
            return ApiResponse.ok(promptService.list());
        }
        return ApiResponse.ok(promptService.listByScene(scene));
    }

    @PostMapping("/prompts")
    public ApiResponse<AiPromptTemplate> createPrompt(@RequestBody PromptRequest request,
                                                      HttpServletRequest httpRequest) {
        return ApiResponse.ok(promptService.create(request, username(httpRequest)));
    }

    @PutMapping("/prompts/{id}")
    public ApiResponse<AiPromptTemplate> updatePrompt(@PathVariable Long id,
                                                      @RequestBody PromptRequest request,
                                                      HttpServletRequest httpRequest) {
        return ApiResponse.ok(promptService.update(id, request, username(httpRequest)));
    }

    @DeleteMapping("/prompts/{id}")
    public ApiResponse<Void> deletePrompt(@PathVariable Long id) {
        promptService.delete(id);
        return ApiResponse.ok();
    }

    @GetMapping("/quota")
    public ApiResponse<AiQuota> quota() {
        return ApiResponse.ok(quotaService.current());
    }

    @PutMapping("/quota")
    public ApiResponse<AiQuota> updateQuota(@RequestBody QuotaRequest request) {
        return ApiResponse.ok(quotaService.update(request));
    }

    private String username(HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        return username == null ? "system" : username;
    }
}
