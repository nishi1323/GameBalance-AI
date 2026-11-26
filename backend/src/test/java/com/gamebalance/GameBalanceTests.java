package com.gamebalance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamebalance.config.MongoConfig;
import com.gamebalance.controller.HealthController;
import com.gamebalance.dto.*;
import com.gamebalance.exception.ConflictException;
import com.gamebalance.model.*;
import com.gamebalance.repository.*;
import com.gamebalance.security.JwtUtil;
import com.gamebalance.service.*;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.SocketTimeoutException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class GameBalanceTests {

    @Test
    void jwtGeneratesValidatesAndRejectsExpiredOrInvalidTokens() {
        String secret = "TestSecretKeyForUnitTestingOnly1234567890";
        JwtUtil jwtUtil = new JwtUtil(secret, 3600000);
        String token = jwtUtil.generateToken("user-123", "student@college.edu");

        assertTrue(jwtUtil.validateToken(token));
        assertEquals("user-123", jwtUtil.extractUserId(token));
        assertEquals("student@college.edu", jwtUtil.extractEmail(token));

        assertFalse(jwtUtil.validateToken(token + "tampered"));
        assertFalse(jwtUtil.validateToken("not.a.jwt"));

        JwtUtil expiredJwtUtil = new JwtUtil(secret, -5000);
        String expiredToken = expiredJwtUtil.generateToken("user-123", "student@college.edu");
        assertFalse(jwtUtil.validateToken(expiredToken));
    }

    @Test
    void bcryptHashesAndVerifiesPasswordsSecurely() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("MySecurePass!2026");

        assertNotEquals("MySecurePass!2026", hash);
        assertTrue(hash.startsWith("$2"));
        assertTrue(encoder.matches("MySecurePass!2026", hash));
        assertFalse(encoder.matches("WrongPassword", hash));
    }

    @Test
    void groqServiceFormatsRequestWithEnvConfigAndParsesResponse() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);

        GroqService groqService = new GroqService(restTemplate, new ObjectMapper());
        ReflectionTestUtils.setField(groqService, "apiKey", "\"gsk_test_mock_key_value\"");
        ReflectionTestUtils.setField(groqService, "model", "llama-3.3-70b-versatile");
        ReflectionTestUtils.setField(groqService, "apiUrl", "https://api.groq.com/openai/v1/chat/completions");

        assertTrue(groqService.isConfigured());

        String mockResponse = """
                {
                  "choices": [
                    {
                      "message": {
                        "role": "assistant",
                        "content": "Try finishing one 45-minute study sprint before gaming."
                      }
                    }
                  ]
                }
                """;

        server.expect(requestTo("https://api.groq.com/openai/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer gsk_test_mock_key_value"))
                .andExpect(jsonPath("$.model").value("llama-3.3-70b-versatile"))
                .andRespond(withSuccess(mockResponse, MediaType.APPLICATION_JSON));

        Optional<String> reply = groqService.generateSinglePrompt("system", "hello", 0.5, 100);
        server.verify();
        assertTrue(reply.isPresent());
        assertEquals("Try finishing one 45-minute study sprint before gaming.", reply.get());
        assertEquals("OK", groqService.getLastStatus());
    }

    @Test
    void groqServiceHandlesErrorStatusesAndTimeoutsWithoutThrowing() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);

        GroqService groqService = new GroqService(restTemplate, new ObjectMapper());
        ReflectionTestUtils.setField(groqService, "apiKey", "gsk_test_mock_key");
        ReflectionTestUtils.setField(groqService, "model", "llama-3.3-70b-versatile");
        ReflectionTestUtils.setField(groqService, "apiUrl", "https://api.groq.com/openai/v1/chat/completions");

        server.expect(requestTo("https://api.groq.com/openai/v1/chat/completions"))
                .andRespond(withUnauthorizedRequest());
        assertTrue(groqService.generateSinglePrompt("s", "u", 0.5, 50).isEmpty());
        assertEquals("INVALID_API_KEY", groqService.getLastStatus());

        server.reset();
        server.expect(requestTo("https://api.groq.com/openai/v1/chat/completions"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));
        assertTrue(groqService.generateSinglePrompt("s", "u", 0.5, 50).isEmpty());
        assertEquals("MODEL_UNAVAILABLE", groqService.getLastStatus());

        server.reset();
        server.expect(requestTo("https://api.groq.com/openai/v1/chat/completions"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));
        assertTrue(groqService.generateSinglePrompt("s", "u", 0.5, 50).isEmpty());
        assertEquals("RATE_LIMITED", groqService.getLastStatus());

        server.reset();
        server.expect(requestTo("https://api.groq.com/openai/v1/chat/completions"))
                .andRespond(withServerError());
        assertTrue(groqService.generateSinglePrompt("s", "u", 0.5, 50).isEmpty());
        assertEquals("GROQ_SERVER_ERROR", groqService.getLastStatus());

        RestTemplate timeoutTemplate = mock(RestTemplate.class);
        when(timeoutTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
                .thenThrow(new ResourceAccessException("Read timed out", new SocketTimeoutException("timeout")));
        GroqService timeoutService = new GroqService(timeoutTemplate, new ObjectMapper());
        ReflectionTestUtils.setField(timeoutService, "apiKey", "gsk_test_mock_key");
        ReflectionTestUtils.setField(timeoutService, "model", "llama-3.3-70b-versatile");
        ReflectionTestUtils.setField(timeoutService, "apiUrl", "https://api.groq.com/openai/v1/chat/completions");
        assertTrue(timeoutService.generateSinglePrompt("s", "u", 0.5, 50).isEmpty());
        assertEquals("TIMEOUT", timeoutService.getLastStatus());
    }

    @Test
    void chatServiceMarksFallbackRepliesExplicitlyWhenGroqUnavailable() {
        ChatMessageRepository chatRepo = mock(ChatMessageRepository.class);
        AuthService authService = mock(AuthService.class);
        GamingSessionService gamingService = mock(GamingSessionService.class);
        StudyTaskRepository studyRepo = mock(StudyTaskRepository.class);
        SleepRecordRepository sleepRepo = mock(SleepRecordRepository.class);
        RiskAssessmentRepository riskRepo = mock(RiskAssessmentRepository.class);
        GroqService groqService = mock(GroqService.class);

        User user = new User();
        user.setId("u1");
        user.setFullName("Test Student");
        user.setDailyGamingLimitMinutes(120);

        when(authService.getUserEntity("u1")).thenReturn(user);
        when(gamingService.getTodayGamingMinutes("u1")).thenReturn(45L);
        when(studyRepo.findByUserIdOrderByDueDateTimeAsc("u1")).thenReturn(List.of());
        when(sleepRepo.findByUserIdOrderByRecordDateDesc("u1")).thenReturn(List.of());
        when(riskRepo.findFirstByUserIdOrderByCreatedAtDesc("u1")).thenReturn(Optional.empty());
        when(chatRepo.findByUserIdOrderByCreatedAtAsc("u1")).thenReturn(List.of());
        when(chatRepo.save(any(ChatMessage.class))).thenAnswer(inv -> inv.getArgument(0));
        when(groqService.generateCompletion(anyList(), anyDouble(), anyInt())).thenReturn(Optional.empty());
        when(groqService.getLastStatus()).thenReturn("NOT_CONFIGURED");

        ChatService chatService = new ChatService(
                chatRepo, authService, gamingService, studyRepo, sleepRepo, riskRepo, groqService);
        List<ChatMessage> result = chatService.sendMessage("u1", "How do I focus on studying?");

        assertEquals(2, result.size());
        ChatMessage assistant = result.get(1);
        assertEquals("assistant", assistant.getRole());
        assertEquals(Boolean.FALSE, assistant.getAiPowered());
        assertEquals("NOT_CONFIGURED", assistant.getAiStatus());
    }

    @Test
    void riskScoringCalculatesExpectedCategoriesAndDisclaimer() {
        RiskAssessmentRepository repo = mock(RiskAssessmentRepository.class);
        GroqService groq = mock(GroqService.class);
        when(groq.generateSinglePrompt(anyString(), anyString(), anyDouble(), anyInt())).thenReturn(Optional.empty());
        when(repo.save(any(RiskAssessment.class))).thenAnswer(inv -> inv.getArgument(0));

        RiskAssessmentService service = new RiskAssessmentService(repo, groq, new ObjectMapper());

        RiskAssessmentRequest lowReq = new RiskAssessmentRequest();
        lowReq.setAvgDailyGamingHours(1.0);
        lowReq.setWeekendGamingHours(2.0);
        lowReq.setAvgSleepDuration(8.0);
        lowReq.setLateNightGamingSessions(0);
        lowReq.setMissedStudyTasks(0);

        RiskAssessment lowResult = service.analyzeAndSave("u1", lowReq);
        assertEquals("LOW", lowResult.getRiskCategory());
        assertTrue(lowResult.getRiskScore() < 25);
        assertTrue(lowResult.getDisclaimer().contains("do not constitute a medical"));

        RiskAssessmentRequest highReq = new RiskAssessmentRequest();
        highReq.setAvgDailyGamingHours(6.5);
        highReq.setWeekendGamingHours(9.0);
        highReq.setAvgSleepDuration(5.0);
        highReq.setLateNightGamingSessions(5);
        highReq.setMissedStudyTasks(4);
        highReq.setAffectsAcademics(true);
        highReq.setDifficultToStop(true);
        highReq.setAffectsPhysicalOrSocial(true);

        RiskAssessment highResult = service.analyzeAndSave("u1", highReq);
        assertTrue(highResult.getRiskScore() >= 80);
        assertEquals("VERY_HIGH", highResult.getRiskCategory());
    }

    @Test
    void gamingPlannerPreventsOverlappingSessions() {
        GamingPlanRepository planRepo = mock(GamingPlanRepository.class);
        UserRepository userRepo = mock(UserRepository.class);
        AuthService authService = mock(AuthService.class);
        GamingSessionService sessionService = mock(GamingSessionService.class);

        GamingPlan existing = new GamingPlan();
        existing.setGameName("Valorant");
        existing.setPlanDate(LocalDate.of(2026, 11, 26));
        existing.setStartTime(LocalTime.of(19, 0));
        existing.setEndTime(LocalTime.of(20, 30));

        when(planRepo.findByUserIdAndPlanDateOrderByStartTimeAsc("u1", LocalDate.of(2026, 11, 26)))
                .thenReturn(List.of(existing));

        GamingPlanService service = new GamingPlanService(planRepo, userRepo, authService, sessionService);

        GamingPlanRequest overlapping = new GamingPlanRequest();
        overlapping.setGameName("CS2");
        overlapping.setPlanDate(LocalDate.of(2026, 11, 26));
        overlapping.setStartTime(LocalTime.of(20, 0));
        overlapping.setEndTime(LocalTime.of(21, 0));

        assertThrows(ConflictException.class, () -> service.createPlan("u1", overlapping));
    }

    @Test
    void gamingSessionCalculatesDurationAndPreventsDuplicateActive() {
        GamingSessionRepository sessionRepo = mock(GamingSessionRepository.class);
        AuthService authService = mock(AuthService.class);
        when(sessionRepo.save(any(GamingSession.class))).thenAnswer(inv -> inv.getArgument(0));

        GamingSession active = new GamingSession();
        active.setId("sess-1");
        active.setUserId("u1");
        active.setGameName("Minecraft");
        active.setStartTime(Instant.now().minusSeconds(1800));
        active.setActive(true);

        when(sessionRepo.findFirstByUserIdAndActiveTrue("u1")).thenReturn(Optional.of(active));
        when(sessionRepo.findById("sess-1")).thenReturn(Optional.of(active));

        GamingSessionService service = new GamingSessionService(sessionRepo, authService);

        StartGamingSessionRequest startReq = new StartGamingSessionRequest();
        startReq.setGameName("Dota 2");
        assertThrows(ConflictException.class, () -> service.startSession("u1", startReq));

        GamingSession stopped = service.stopSession("u1", "sess-1", new StopGamingSessionRequest());
        assertFalse(stopped.isActive());
        assertTrue(stopped.getTotalDurationMinutes() >= 29 && stopped.getTotalDurationMinutes() <= 31);
    }

    @Test
    void sleepTrackerCalculatesDurationAndFlagsShortSleep() {
        SleepRecordRepository sleepRepo = mock(SleepRecordRepository.class);
        GamingSessionRepository gamingRepo = mock(GamingSessionRepository.class);
        GroqService groq = mock(GroqService.class);
        when(sleepRepo.save(any(SleepRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        SleepRecordService service = new SleepRecordService(sleepRepo, gamingRepo, groq);

        SleepRecordRequest req = new SleepRecordRequest();
        req.setSleepTime(LocalDateTime.of(2026, 11, 26, 2, 0));
        req.setWakeUpTime(LocalDateTime.of(2026, 11, 26, 7, 30));
        req.setQualityRating(2);
        req.setHadLateNightGaming(true);

        SleepRecord saved = service.createRecord("u1", req);
        assertEquals(5.5, saved.getTotalDurationHours());
        assertTrue(saved.isIrregularPattern());
        assertTrue(saved.isHadLateNightGaming());
    }

    @Test
    void mongoConfigPreservesStrictTlsHostnameVerification() {
        MongoConfig config = new MongoConfig();
        MongoClientSettings.Builder builder = MongoClientSettings.builder();
        config.mongoClientSettingsCustomizer().customize(builder);
        MongoClientSettings settings = builder.build();

        assertFalse(settings.getSslSettings().isInvalidHostNameAllowed());
    }

    @Test
    void healthControllerHandlesIntegerAndDoublePingResponses() {
        GroqService groq = mock(GroqService.class);
        when(groq.isConfigured()).thenReturn(true);
        when(groq.getConfiguredModel()).thenReturn("llama-3.3-70b-versatile");
        when(groq.getLastStatus()).thenReturn("OK");

        MongoTemplate mongoTemplate = mock(MongoTemplate.class);
        MongoDatabase mongoDb = mock(MongoDatabase.class);
        when(mongoDb.getName()).thenReturn("GameBalanceAI");
        when(mongoTemplate.getDb()).thenReturn(mongoDb);

        HealthController controller = new HealthController(groq, mongoTemplate);

        // Atlas 8.0 returns BSON int32 ok=1
        when(mongoTemplate.executeCommand(any(Document.class))).thenReturn(new Document("ok", 1));
        ResponseEntity<java.util.Map<String, Object>> atlasResp = controller.health();
        assertEquals("UP", atlasResp.getBody().get("status"));
        assertEquals(true, atlasResp.getBody().get("mongoConnected"));
        assertEquals("GameBalanceAI", atlasResp.getBody().get("database"));

        // Local mongod returns BSON double ok=1.0
        when(mongoTemplate.executeCommand(any(Document.class))).thenReturn(new Document("ok", 1.0));
        ResponseEntity<java.util.Map<String, Object>> localResp = controller.health();
        assertEquals("UP", localResp.getBody().get("status"));
        assertEquals(true, localResp.getBody().get("mongoConnected"));
        assertEquals("GameBalanceAI", localResp.getBody().get("database"));

        // Ping failure returns DEGRADED
        when(mongoTemplate.executeCommand(any(Document.class))).thenThrow(new RuntimeException("connection error"));
        ResponseEntity<java.util.Map<String, Object>> failResp = controller.health();
        assertEquals("DEGRADED", failResp.getBody().get("status"));
        assertEquals(false, failResp.getBody().get("mongoConnected"));
        assertEquals("", failResp.getBody().get("database"));
    }
}
