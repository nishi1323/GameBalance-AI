package com.gamebalance.controller;

import com.gamebalance.dto.StudySummaryResponse;
import com.gamebalance.dto.StudyTaskRequest;
import com.gamebalance.model.StudyTask;
import com.gamebalance.security.AuthUserPrincipal;
import com.gamebalance.service.StudyTaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/study")
public class StudyTaskController {

    private final StudyTaskService studyTaskService;

    public StudyTaskController(StudyTaskService studyTaskService) {
        this.studyTaskService = studyTaskService;
    }

    @GetMapping
    public ResponseEntity<StudySummaryResponse> getSummary(@AuthenticationPrincipal AuthUserPrincipal principal) {
        return ResponseEntity.ok(studyTaskService.getSummary(principal.getId()));
    }

    @PostMapping
    public ResponseEntity<StudyTask> create(@AuthenticationPrincipal AuthUserPrincipal principal,
                                            @Valid @RequestBody StudyTaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(studyTaskService.createTask(principal.getId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudyTask> update(@AuthenticationPrincipal AuthUserPrincipal principal,
                                            @PathVariable String id,
                                            @Valid @RequestBody StudyTaskRequest request) {
        return ResponseEntity.ok(studyTaskService.updateTask(principal.getId(), id, request));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<StudyTask> toggle(@AuthenticationPrincipal AuthUserPrincipal principal,
                                            @PathVariable String id) {
        return ResponseEntity.ok(studyTaskService.toggleComplete(principal.getId(), id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@AuthenticationPrincipal AuthUserPrincipal principal,
                                                      @PathVariable String id) {
        studyTaskService.deleteTask(principal.getId(), id);
        return ResponseEntity.ok(Map.of("message", "Study task deleted."));
    }
}
