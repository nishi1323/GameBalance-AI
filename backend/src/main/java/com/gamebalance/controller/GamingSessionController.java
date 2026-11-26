package com.gamebalance.controller;

import com.gamebalance.dto.GamingStatsResponse;
import com.gamebalance.dto.StartGamingSessionRequest;
import com.gamebalance.dto.StopGamingSessionRequest;
import com.gamebalance.model.GamingSession;
import com.gamebalance.security.AuthUserPrincipal;
import com.gamebalance.service.GamingSessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/gaming")
public class GamingSessionController {

    private final GamingSessionService gamingSessionService;

    public GamingSessionController(GamingSessionService gamingSessionService) {
        this.gamingSessionService = gamingSessionService;
    }

    @GetMapping("/stats")
    public ResponseEntity<GamingStatsResponse> getStats(@AuthenticationPrincipal AuthUserPrincipal principal) {
        return ResponseEntity.ok(gamingSessionService.getStats(principal.getId()));
    }

    @GetMapping("/active")
    public ResponseEntity<GamingSession> getActive(@AuthenticationPrincipal AuthUserPrincipal principal) {
        return ResponseEntity.ok(gamingSessionService.getActiveSession(principal.getId()));
    }

    @PostMapping("/start")
    public ResponseEntity<GamingSession> start(@AuthenticationPrincipal AuthUserPrincipal principal,
                                               @Valid @RequestBody StartGamingSessionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(gamingSessionService.startSession(principal.getId(), request));
    }

    @PostMapping("/stop/{id}")
    public ResponseEntity<GamingSession> stop(@AuthenticationPrincipal AuthUserPrincipal principal,
                                              @PathVariable String id,
                                              @Valid @RequestBody(required = false) StopGamingSessionRequest request) {
        return ResponseEntity.ok(gamingSessionService.stopSession(principal.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@AuthenticationPrincipal AuthUserPrincipal principal,
                                                      @PathVariable String id) {
        gamingSessionService.deleteSession(principal.getId(), id);
        return ResponseEntity.ok(Map.of("message", "Gaming session deleted."));
    }
}
