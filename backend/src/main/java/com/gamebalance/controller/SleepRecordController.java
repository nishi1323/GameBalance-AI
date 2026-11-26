package com.gamebalance.controller;

import com.gamebalance.dto.SleepRecordRequest;
import com.gamebalance.dto.SleepSummaryResponse;
import com.gamebalance.model.SleepRecord;
import com.gamebalance.security.AuthUserPrincipal;
import com.gamebalance.service.SleepRecordService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sleep")
public class SleepRecordController {

    private final SleepRecordService sleepService;

    public SleepRecordController(SleepRecordService sleepService) {
        this.sleepService = sleepService;
    }

    @GetMapping
    public ResponseEntity<SleepSummaryResponse> getSummary(@AuthenticationPrincipal AuthUserPrincipal principal) {
        return ResponseEntity.ok(sleepService.getSummary(principal.getId()));
    }

    @PostMapping
    public ResponseEntity<SleepRecord> create(@AuthenticationPrincipal AuthUserPrincipal principal,
                                              @Valid @RequestBody SleepRecordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sleepService.createRecord(principal.getId(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@AuthenticationPrincipal AuthUserPrincipal principal,
                                                      @PathVariable String id) {
        sleepService.deleteRecord(principal.getId(), id);
        return ResponseEntity.ok(Map.of("message", "Sleep record deleted."));
    }
}
