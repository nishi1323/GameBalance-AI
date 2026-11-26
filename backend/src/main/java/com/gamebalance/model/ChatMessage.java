package com.gamebalance.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "chat_history")
public class ChatMessage {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String role;

    private String content;

    private Boolean aiPowered;

    private String aiStatus;

    private Instant createdAt;

    public ChatMessage() {
    }

    public ChatMessage(String userId, String role, String content, Instant createdAt) {
        this.userId = userId;
        this.role = role;
        this.content = content;
        this.createdAt = createdAt;
    }

    public ChatMessage(String userId, String role, String content, Boolean aiPowered, String aiStatus, Instant createdAt) {
        this.userId = userId;
        this.role = role;
        this.content = content;
        this.aiPowered = aiPowered;
        this.aiStatus = aiStatus;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getAiPowered() {
        return aiPowered;
    }

    public void setAiPowered(Boolean aiPowered) {
        this.aiPowered = aiPowered;
    }

    public String getAiStatus() {
        return aiStatus;
    }

    public void setAiStatus(String aiStatus) {
        this.aiStatus = aiStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
