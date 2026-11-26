package com.gamebalance.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.SocketTimeoutException;
import java.util.*;

@Service
public class GroqService {

    private static final Logger log = LoggerFactory.getLogger(GroqService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.groq.api-key:}")
    private String apiKey;

    @Value("${app.groq.model:llama-3.3-70b-versatile}")
    private String model;

    @Value("${app.groq.api-url:https://api.groq.com/openai/v1/chat/completions}")
    private String apiUrl;

    private volatile String lastStatus = "NOT_TESTED";

    public GroqService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public boolean isConfigured() {
        String key = cleanValue(apiKey);
        return !key.isBlank() && !key.equals("your-groq-api-key-here");
    }

    public String getConfiguredModel() {
        return cleanValue(model);
    }

    public String getLastStatus() {
        return isConfigured() ? lastStatus : "NOT_CONFIGURED";
    }

    public Optional<String> generateCompletion(List<Map<String, String>> messages, double temperature, int maxTokens) {
        if (!isConfigured()) {
            lastStatus = "NOT_CONFIGURED";
            return Optional.empty();
        }

        String cleanedKey = cleanValue(apiKey);
        String cleanedModel = cleanValue(model);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(cleanedKey);
            headers.set("User-Agent", "GameBalanceAI/1.0");

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", cleanedModel);
            body.put("messages", messages);
            body.put("temperature", temperature);
            body.put("max_tokens", maxTokens);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode content = root.path("choices").path(0).path("message").path("content");
                if (!content.isMissingNode() && !content.asText().isBlank()) {
                    lastStatus = "OK";
                    return Optional.of(content.asText().trim());
                }
            }
            lastStatus = "EMPTY_RESPONSE";
        } catch (HttpStatusCodeException ex) {
            int code = ex.getStatusCode().value();
            String bodyText = ex.getResponseBodyAsString();
            if (code == 401 || code == 403) {
                lastStatus = "INVALID_API_KEY";
            } else if (code == 404 || (code == 400 && bodyText.toLowerCase(Locale.ROOT).contains("model"))) {
                lastStatus = "MODEL_UNAVAILABLE";
            } else if (code == 429) {
                lastStatus = "RATE_LIMITED";
            } else if (code >= 500) {
                lastStatus = "GROQ_SERVER_ERROR";
            } else {
                lastStatus = "HTTP_" + code;
            }
            log.warn("Groq API call failed with status={} category={}", code, lastStatus);
        } catch (ResourceAccessException ex) {
            if (ex.getCause() instanceof SocketTimeoutException) {
                lastStatus = "TIMEOUT";
            } else {
                lastStatus = "NETWORK_ERROR";
            }
            log.warn("Groq API connection failed category={}", lastStatus);
        } catch (Exception ex) {
            lastStatus = "ERROR_" + ex.getClass().getSimpleName();
            log.warn("Groq API unexpected failure category={}", lastStatus);
        }
        return Optional.empty();
    }

    public Optional<String> generateSinglePrompt(String systemPrompt, String userPrompt, double temperature, int maxTokens) {
        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        );
        return generateCompletion(messages, temperature, maxTokens);
    }

    private String cleanValue(String raw) {
        if (raw == null) return "";
        String trimmed = raw.trim();
        if ((trimmed.startsWith("\"") && trimmed.endsWith("\""))
                || (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
            if (trimmed.length() >= 2) {
                trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
            }
        }
        return trimmed;
    }
}
