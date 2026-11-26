package com.gamebalance.controller;

import com.gamebalance.dto.RiskAssessmentRequest;
import com.gamebalance.model.RiskAssessment;
import com.gamebalance.security.AuthUserPrincipal;
import com.gamebalance.service.RiskAssessmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/risk")
public class RiskAssessmentController {

    private final RiskAssessmentService riskService;

    public RiskAssessmentController(RiskAssessmentService riskService) {
        this.riskService = riskService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<RiskAssessment> analyze(@AuthenticationPrincipal AuthUserPrincipal principal,
                                                  @Valid @RequestBody RiskAssessmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(riskService.analyzeAndSave(principal.getId(), request));
    }

    @GetMapping("/history")
    public ResponseEntity<List<RiskAssessment>> history(@AuthenticationPrincipal AuthUserPrincipal principal) {
        return ResponseEntity.ok(riskService.getHistory(principal.getId()));
    }

    @GetMapping("/latest")
    public ResponseEntity<RiskAssessment> latest(@AuthenticationPrincipal AuthUserPrincipal principal) {
        return ResponseEntity.ok(riskService.getLatest(principal.getId()));
    }
}
