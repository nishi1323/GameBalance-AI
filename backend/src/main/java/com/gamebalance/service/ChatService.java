package com.gamebalance.service;

import com.gamebalance.model.*;
import com.gamebalance.repository.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
public class ChatService {

    private final ChatMessageRepository chatRepository;
    private final AuthService authService;
    private final GamingSessionService gamingSessionService;
    private final StudyTaskRepository studyTaskRepository;
    private final SleepRecordRepository sleepRepository;
    private final RiskAssessmentRepository riskRepository;
    private final GroqService groqService;

    public ChatService(ChatMessageRepository chatRepository,
                       AuthService authService,
                       GamingSessionService gamingSessionService,
                       StudyTaskRepository studyTaskRepository,
                       SleepRecordRepository sleepRepository,
                       RiskAssessmentRepository riskRepository,
                       GroqService groqService) {
        this.chatRepository = chatRepository;
        this.authService = authService;
        this.gamingSessionService = gamingSessionService;
        this.studyTaskRepository = studyTaskRepository;
        this.sleepRepository = sleepRepository;
        this.riskRepository = riskRepository;
        this.groqService = groqService;
    }

    public List<ChatMessage> getHistory(String userId) {
        return chatRepository.findByUserIdOrderByCreatedAtAsc(userId);
    }

    public void clearHistory(String userId) {
        chatRepository.deleteByUserId(userId);
    }

    public List<ChatMessage> sendMessage(String userId, String userText) {
        String trimmed = userText.trim();
        ChatMessage userMsg = chatRepository.save(new ChatMessage(userId, "user", trimmed, Instant.now()));

        String systemPrompt = buildContextAwareSystemPrompt(userId);
        List<ChatMessage> recentHistory = chatRepository.findByUserIdOrderByCreatedAtAsc(userId);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));

        int startIdx = Math.max(0, recentHistory.size() - 12);
        for (int i = startIdx; i < recentHistory.size(); i++) {
            ChatMessage m = recentHistory.get(i);
            messages.add(Map.of("role", m.getRole(), "content", m.getContent()));
        }

        Optional<String> aiReply = groqService.generateCompletion(messages, 0.65, 500);
        boolean aiPowered = aiReply.isPresent();
        String aiStatus = aiPowered ? "OK" : groqService.getLastStatus();
        String reply = aiReply.orElseGet(() -> buildFallbackReply(userId, trimmed));

        ChatMessage assistantMsg = chatRepository.save(
                new ChatMessage(userId, "assistant", reply, aiPowered, aiStatus, Instant.now())
        );
        return List.of(userMsg, assistantMsg);
    }

    private String buildContextAwareSystemPrompt(String userId) {
        User user = authService.getUserEntity(userId);
        long todayGamingMins = gamingSessionService.getTodayGamingMinutes(userId);
        int dailyLimit = user.getDailyGamingLimitMinutes() != null ? user.getDailyGamingLimitMinutes() : 120;

        List<StudyTask> tasks = studyTaskRepository.findByUserIdOrderByDueDateTimeAsc(userId);
        long pendingTasks = tasks.stream().filter(t -> !t.isCompleted()).count();
        long completedTasks = tasks.stream().filter(StudyTask::isCompleted).count();

        List<SleepRecord> sleepRecords = sleepRepository.findByUserIdOrderByRecordDateDesc(userId);
        String sleepContext = sleepRecords.isEmpty()
                ? "no sleep records logged yet"
                : String.format("latest sleep %.1f hrs", sleepRecords.get(0).getTotalDurationHours());

        Optional<RiskAssessment> latestRisk = riskRepository.findFirstByUserIdOrderByCreatedAtDesc(userId);
        String riskContext = latestRisk
                .map(r -> String.format("%s (%d/100)", r.getRiskCategory(), r.getRiskScore()))
                .orElse("not yet assessed");

        return String.format("""
                You are GameBalance AI Assistant, a friendly, empathetic student wellness coach helping college students balance gaming, academics, sleep, physical health, and stress.
                Student Context (use subtly when helpful, never recite raw JSON):
                - Name: %s
                - Today's tracked gaming: %d mins (Daily limit: %d mins)
                - Study tasks: %d pending, %d completed
                - Sleep: %s
                - Latest Educational Wellness Risk Estimate: %s

                Guidelines:
                - Give practical, supportive advice on gaming habits, time management, study productivity, sleep hygiene, physical movement, and stress management.
                - NEVER provide medical or psychiatric diagnoses.
                - If the student describes severe distress, depression, self-harm, or serious impairment, gently encourage speaking with a campus counselor, trusted adult, or healthcare professional.
                - Keep replies clear, warm, actionable, and concise (around 3-6 sentences or short bullet points).
                """,
                user.getFullName(), todayGamingMins, dailyLimit, pendingTasks, completedTasks, sleepContext, riskContext);
    }

    private String buildFallbackReply(String userId, String userText) {
        String lower = userText.toLowerCase(Locale.ROOT);
        long todayMins = gamingSessionService.getTodayGamingMinutes(userId);
        User user = authService.getUserEntity(userId);
        int limit = user.getDailyGamingLimitMinutes() != null ? user.getDailyGamingLimitMinutes() : 120;

        if (lower.contains("depress") || lower.contains("hopeless") || lower.contains("panic") || lower.contains("can't cope") || lower.contains("suicide")) {
            return "I'm really glad you reached out. While I'm an AI wellness assistant and cannot provide medical or mental-health care, what you're describing deserves real human support. Please consider talking with your college counseling center, a trusted family member, or a licensed healthcare professional right away.";
        }
        if (lower.contains("sleep") || lower.contains("tired") || lower.contains("late")) {
            return "Great sleep makes both studying and gaming feel much better! Try setting a screen curfew 45 minutes before bed, stopping competitive matches by 10:30 PM, and logging your wake-up time in the Sleep Tracker so you can spot patterns.";
        }
        if (lower.contains("study") || lower.contains("exam") || lower.contains("procrastin") || lower.contains("focus")) {
            return "Try the 'Study First, Play After' rule: pick one 45-minute priority task from your Study Planner, put your phone in another room, and reward yourself with a scheduled gaming slot once it's checked off!";
        }
        return String.format(
                "You're at %d/%d gaming minutes today (%s). For steady balance, try breaking study tasks into 45-minute blocks with 10-minute movement breaks, and save gaming for your planned evening window. (Tip: Add your GROQ_API_KEY in the backend environment to enable full live LLM responses!)",
                todayMins, limit, LocalDate.now()
        );
    }
}
