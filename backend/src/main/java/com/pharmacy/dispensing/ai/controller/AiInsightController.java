package com.pharmacy.dispensing.ai.controller;

import com.pharmacy.dispensing.ai.dto.AiInsightsSummaryDto;
import com.pharmacy.dispensing.ai.service.AiAssistanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
public class AiInsightController {

    private final AiAssistanceService aiAssistanceService;

    public AiInsightController(AiAssistanceService aiAssistanceService) {
        this.aiAssistanceService = aiAssistanceService;
    }

    /**
     * GET /api/v1/ai/insights/summary
     * Unified AI Decision-Support Summary containing:
     * - Scikit-Learn ML Demand Forecasts
     * - Expiry-Waste Risk Assessments
     * - Smart Procurement Recommendations
     */
    @GetMapping("/insights/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST', 'AUDITOR')")
    public ResponseEntity<AiInsightsSummaryDto> getInsightsSummary() {
        return ResponseEntity.ok(aiAssistanceService.getInsightsSummary());
    }
}
