package com.gamebalance.controller;

import com.gamebalance.dto.GamingPlanRequest;
import com.gamebalance.dto.GamingPlannerResponse;
import com.gamebalance.dto.GamingSettingsRequest;
import com.gamebalance.model.GamingPlan;
import com.gamebalance.security.AuthUserPrincipal;
import com.gamebalance.service.GamingPlanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/planner")
public class GamingPlanController {

    private final GamingPlanService planService;

    public GamingPlanController(GamingPlanService planService) {
        this.planService = planService;
    }

    @GetMapping
    public ResponseEntity<GamingPlannerResponse> getOverview(@AuthenticationPrincipal AuthUserPrincipal principal) {
        return ResponseEntity.ok(planService.getPlannerOverview(principal.getId()));
    }

    @PostMapping("/plans")
    public ResponseEntity<GamingPlan> createPlan(@AuthenticationPrincipal AuthUserPrincipal principal,
                                                 @Valid @RequestBody GamingPlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(planService.createPlan(principal.getId(), request));
    }

    @DeleteMapping("/plans/{id}")
    public ResponseEntity<Map<String, String>> deletePlan(@AuthenticationPrincipal AuthUserPrincipal principal,
                                                          @PathVariable String id) {
        planService.deletePlan(principal.getId(), id);
        return ResponseEntity.ok(Map.of("message", "Planned gaming session removed."));
    }

    @PutMapping("/settings")
    public ResponseEntity<GamingPlannerResponse> updateSettings(@AuthenticationPrincipal AuthUserPrincipal principal,
                                                                @Valid @RequestBody GamingSettingsRequest request) {
        return ResponseEntity.ok(planService.updateSettings(principal.getId(), request));
    }
}
