package com.gamebalance.controller;

import com.gamebalance.dto.ChatRequest;
import com.gamebalance.model.ChatMessage;
import com.gamebalance.security.AuthUserPrincipal;
import com.gamebalance.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatMessage>> getHistory(@AuthenticationPrincipal AuthUserPrincipal principal) {
        return ResponseEntity.ok(chatService.getHistory(principal.getId()));
    }

    @PostMapping("/message")
    public ResponseEntity<List<ChatMessage>> sendMessage(@AuthenticationPrincipal AuthUserPrincipal principal,
                                                         @Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(chatService.sendMessage(principal.getId(), request.getMessage()));
    }

    @DeleteMapping("/history")
    public ResponseEntity<Map<String, String>> clearHistory(@AuthenticationPrincipal AuthUserPrincipal principal) {
        chatService.clearHistory(principal.getId());
        return ResponseEntity.ok(Map.of("message", "Conversation history cleared."));
    }
}
