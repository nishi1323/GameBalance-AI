package com.gamebalance.controller;

import com.gamebalance.service.GroqService;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private static final Logger log = LoggerFactory.getLogger(HealthController.class);

    private final GroqService groqService;
    private final MongoTemplate mongoTemplate;

    public HealthController(GroqService groqService, MongoTemplate mongoTemplate) {
        this.groqService = groqService;
        this.mongoTemplate = mongoTemplate;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        boolean mongoConnected = false;
        String databaseName = "";
        try {
            Document ping = mongoTemplate.executeCommand(new Document("ping", 1));
            Object okVal = ping != null ? ping.get("ok") : null;
            mongoConnected = okVal instanceof Number num && num.doubleValue() == 1.0;
            if (mongoConnected) {
                databaseName = mongoTemplate.getDb().getName();
            }
        } catch (Exception ex) {
            log.warn("Health check MongoDB ping failed: {}", ex.getMessage());
            mongoConnected = false;
            databaseName = "";
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", mongoConnected ? "UP" : "DEGRADED");
        body.put("application", "GameBalance AI");
        body.put("mongoConnected", mongoConnected);
        body.put("database", databaseName);
        body.put("groqConfigured", groqService.isConfigured());
        body.put("groqModel", groqService.getConfiguredModel());
        body.put("groqStatus", groqService.getLastStatus());
        body.put("timestamp", Instant.now());
        return ResponseEntity.ok(body);
    }
}
