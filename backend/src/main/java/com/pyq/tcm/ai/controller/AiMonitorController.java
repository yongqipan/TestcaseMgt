package com.pyq.tcm.ai.controller;

import com.pyq.tcm.ai.entity.AiCallLog;
import com.pyq.tcm.ai.entity.AiQuota;
import com.pyq.tcm.ai.monitor.AiMonitorService;
import com.pyq.tcm.ai.quota.AiQuotaService;
import com.pyq.tcm.common.ApiResponse;
import com.pyq.tcm.common.PageResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/monitor")
public class AiMonitorController {

    private final AiMonitorService monitorService;
    private final AiQuotaService quotaService;

    public AiMonitorController(AiMonitorService monitorService, AiQuotaService quotaService) {
        this.monitorService = monitorService;
        this.quotaService = quotaService;
    }

    @GetMapping("/logs")
    public ApiResponse<PageResult<AiCallLog>> logs(@RequestParam(required = false) String scene,
                                                   @RequestParam(required = false) String modelName,
                                                   @RequestParam(required = false) String status,
                                                   @RequestParam(required = false) String username,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(monitorService.logs(scene, modelName, status, username, page, size));
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats() {
        return ApiResponse.ok(monitorService.todayStats());
    }

    @GetMapping("/tokens-by-scene")
    public ApiResponse<List<Map<String, Object>>> tokensByScene(@RequestParam(defaultValue = "7") int days) {
        return ApiResponse.ok(monitorService.tokensByScene(days));
    }

    @GetMapping("/quota")
    public ApiResponse<AiQuota> quota() {
        return ApiResponse.ok(quotaService.current());
    }
}
