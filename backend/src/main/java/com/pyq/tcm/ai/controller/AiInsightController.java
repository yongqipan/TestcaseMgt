package com.pyq.tcm.ai.controller;

import com.pyq.tcm.ai.dto.ReportRequest;
import com.pyq.tcm.ai.dto.ReviewRequest;
import com.pyq.tcm.ai.entity.AiReport;
import com.pyq.tcm.ai.entity.AiReview;
import com.pyq.tcm.ai.report.AiReportService;
import com.pyq.tcm.ai.review.AiReviewService;
import com.pyq.tcm.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiInsightController {

    private final AiReviewService reviewService;
    private final AiReportService reportService;

    public AiInsightController(AiReviewService reviewService, AiReportService reportService) {
        this.reviewService = reviewService;
        this.reportService = reportService;
    }

    @PostMapping("/reviews")
    public ApiResponse<Map<String, Object>> review(@RequestBody ReviewRequest request,
                                                   HttpServletRequest httpRequest) {
        String username = (String) httpRequest.getAttribute("username");
        AiReview review = reviewService.review(request, username);
        return ApiResponse.ok(reviewService.view(review));
    }

    @GetMapping("/reviews")
    public ApiResponse<List<AiReview>> reviewHistory(@RequestParam Long caseId) {
        return ApiResponse.ok(reviewService.history(caseId));
    }

    @PostMapping("/reports")
    public ApiResponse<AiReport> report(@RequestBody ReportRequest request,
                                        HttpServletRequest httpRequest) {
        String username = (String) httpRequest.getAttribute("username");
        return ApiResponse.ok(reportService.generate(request, username));
    }

    @GetMapping("/reports")
    public ApiResponse<List<AiReport>> reportHistory(@RequestParam Long planId) {
        return ApiResponse.ok(reportService.history(planId));
    }

    @GetMapping("/reports/latest")
    public ApiResponse<AiReport> latestReport(@RequestParam Long planId) {
        return ApiResponse.ok(reportService.latest(planId));
    }
}
