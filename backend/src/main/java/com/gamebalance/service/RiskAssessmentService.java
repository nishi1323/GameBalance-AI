package com.gamebalance.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamebalance.dto.RiskAssessmentRequest;
import com.gamebalance.model.RiskAssessment;
import com.gamebalance.repository.RiskAssessmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RiskAssessmentService {

    private static final Logger log = LoggerFactory.getLogger(RiskAssessmentService.class);

    public static final String EDUCATIONAL_DISCLAIMER =
            "Educational Wellness Estimate Only: This score and feedback are generated for student self-awareness and habit planning. " +
            "They do not constitute a medical or clinical diagnosis of gaming disorder. If gaming causes significant distress or impairment, " +
            "please consult a qualified counselor, campus wellness center, or healthcare professional.";

    private final RiskAssessmentRepository riskRepository;
    private final GroqService groqService;
    private final ObjectMapper objectMapper;

    public RiskAssessmentService(RiskAssessmentRepository riskRepository, GroqService groqService, ObjectMapper objectMapper) {
        this.riskRepository = riskRepository;
        this.groqService = groqService;
        this.objectMapper = objectMapper;
    }

    public RiskAssessment analyzeAndSave(String userId, RiskAssessmentRequest req) {
        int score = calculateRuleBasedScore(req);
        String category = categorizeScore(score);

        RiskAssessment assessment = new RiskAssessment();
        assessment.setUserId(userId);
        assessment.setAvgDailyGamingHours(req.getAvgDailyGamingHours());
        assessment.setWeekendGamingHours(req.getWeekendGamingHours());
        assessment.setAvgSleepDuration(req.getAvgSleepDuration());
        assessment.setLateNightGamingSessions(req.getLateNightGamingSessions());
        assessment.setMissedStudyTasks(req.getMissedStudyTasks());
        assessment.setAffectsAcademics(req.isAffectsAcademics());
        assessment.setDifficultToStop(req.isDifficultToStop());
        assessment.setAffectsPhysicalOrSocial(req.isAffectsPhysicalOrSocial());
        assessment.setRiskScore(score);
        assessment.setRiskCategory(category);
        assessment.setDisclaimer(EDUCATIONAL_DISCLAIMER);
        assessment.setCreatedAt(Instant.now());

        boolean aiApplied = enrichWithGroqAi(assessment);
        assessment.setAiPowered(aiApplied);
        if (!aiApplied) {
            populateRuleBasedInsights(assessment);
        }

        return riskRepository.save(assessment);
    }

    public List<RiskAssessment> getHistory(String userId) {
        return riskRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public RiskAssessment getLatest(String userId) {
        return riskRepository.findFirstByUserIdOrderByCreatedAtDesc(userId).orElse(null);
    }

    private int calculateRuleBasedScore(RiskAssessmentRequest req) {
        double score = 0;

        // Daily gaming hours (up to 22 pts)
        if (req.getAvgDailyGamingHours() > 1.5) {
            score += Math.min(22, (req.getAvgDailyGamingHours() - 1.5) * 4.5);
        }

        // Weekend gaming hours (up to 12 pts)
        if (req.getWeekendGamingHours() > 3.0) {
            score += Math.min(12, (req.getWeekendGamingHours() - 3.0) * 2.5);
        }

        // Sleep deficit (up to 18 pts)
        if (req.getAvgSleepDuration() < 7.5) {
            score += Math.min(18, (7.5 - req.getAvgSleepDuration()) * 5.5);
        }

        // Late-night gaming sessions per week (up to 14 pts)
        score += Math.min(14, req.getLateNightGamingSessions() * 2.5);

        // Missed study tasks (up to 12 pts)
        score += Math.min(12, req.getMissedStudyTasks() * 2.5);

        // Behavioral indicators (up to 22 pts)
        if (req.isAffectsAcademics()) score += 8;
        if (req.isDifficultToStop()) score += 8;
        if (req.isAffectsPhysicalOrSocial()) score += 6;

        return (int) Math.max(0, Math.min(100, Math.round(score)));
    }

    private String categorizeScore(int score) {
        if (score < 25) return "LOW";
        if (score < 55) return "MODERATE";
        if (score < 80) return "HIGH";
        return "VERY_HIGH";
    }

    private boolean enrichWithGroqAi(RiskAssessment a) {
        String systemPrompt = """
                You are a supportive student digital-wellness advisor inside GameBalance AI.
                Analyze the student's self-reported gaming, sleep, and study metrics.
                IMPORTANT: Never present this as a medical or clinical diagnosis. Frame everything as an educational wellness estimate.
                Respond strictly with a valid JSON object (no markdown fences) containing:
                {
                  "explanation": "2-3 supportive sentences explaining the wellness estimate",
                  "unhealthyPatterns": ["short bullet 1", "short bullet 2"],
                  "sleepAndAcademicImpact": "2 sentences on sleep and academic impact",
                  "recommendations": ["actionable tip 1", "actionable tip 2", "actionable tip 3", "actionable tip 4"]
                }
                """;

        String userPrompt = String.format(
                "Metrics: avgDailyGamingHours=%.1f, weekendGamingHours=%.1f, avgSleepDuration=%.1f, " +
                "lateNightSessionsPerWeek=%d, missedStudyTasks=%d, affectsAcademics=%s, difficultToStop=%s, " +
                "affectsPhysicalOrSocial=%s. Calculated Wellness Risk Score=%d/100 (%s).",
                a.getAvgDailyGamingHours(), a.getWeekendGamingHours(), a.getAvgSleepDuration(),
                a.getLateNightGamingSessions(), a.getMissedStudyTasks(), a.isAffectsAcademics(),
                a.isDifficultToStop(), a.isAffectsPhysicalOrSocial(), a.getRiskScore(), a.getRiskCategory()
        );

        Optional<String> raw = groqService.generateSinglePrompt(systemPrompt, userPrompt, 0.4, 550);
        if (raw.isEmpty()) {
            return false;
        }

        try {
            String cleaned = extractJsonObject(raw.get());
            JsonNode node = objectMapper.readTree(cleaned);

            String explanation = node.path("explanation").asText("");
            String impact = node.path("sleepAndAcademicImpact").asText("");
            List<String> patterns = readStringList(node.path("unhealthyPatterns"));
            List<String> recs = readStringList(node.path("recommendations"));

            if (explanation.isBlank() || recs.isEmpty()) {
                return false;
            }

            a.setExplanation(explanation);
            a.setSleepAndAcademicImpact(impact.isBlank() ? buildDefaultImpactText(a) : impact);
            a.setUnhealthyPatterns(patterns.isEmpty() ? buildDefaultPatterns(a) : patterns);
            a.setRecommendations(recs);
            return true;
        } catch (Exception ex) {
            log.warn("Failed to parse Groq risk JSON: {}", ex.getMessage());
            return false;
        }
    }

    private String extractJsonObject(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    private List<String> readStringList(JsonNode arrayNode) {
        List<String> list = new ArrayList<>();
        if (arrayNode.isArray()) {
            for (JsonNode item : arrayNode) {
                String val = item.asText("").trim();
                if (!val.isBlank()) list.add(val);
            }
        }
        return list;
    }

    private void populateRuleBasedInsights(RiskAssessment a) {
        a.setExplanation(String.format(
                "Based on your self-reported routine (%.1f hrs/day gaming, %.1f hrs/night sleep, and %d late-night sessions/week), " +
                "your educational wellness risk score is %d/100 (%s). This estimate highlights where small schedule adjustments can protect your focus and rest.",
                a.getAvgDailyGamingHours(), a.getAvgSleepDuration(), a.getLateNightGamingSessions(),
                a.getRiskScore(), a.getRiskCategory().replace("_", " ")
        ));
        a.setUnhealthyPatterns(buildDefaultPatterns(a));
        a.setSleepAndAcademicImpact(buildDefaultImpactText(a));
        a.setRecommendations(buildDefaultRecommendations(a));
    }

    private List<String> buildDefaultPatterns(RiskAssessment a) {
        List<String> patterns = new ArrayList<>();
        if (a.getAvgDailyGamingHours() > 3.0) {
            patterns.add("High weekday gaming volume (" + a.getAvgDailyGamingHours() + " hrs/day) may crowd out study blocks.");
        }
        if (a.getWeekendGamingHours() > 5.0) {
            patterns.add("Long weekend gaming marathons (" + a.getWeekendGamingHours() + " hrs/day) can disrupt Monday energy.");
        }
        if (a.getLateNightGamingSessions() >= 2) {
            patterns.add("Frequent late-night sessions (" + a.getLateNightGamingSessions() + "/week) delay circadian wind-down.");
        }
        if (a.isDifficultToStop()) {
            patterns.add("Difficulty ending sessions on time suggests 'one more match' inertia.");
        }
        if (a.getMissedStudyTasks() > 0 || a.isAffectsAcademics()) {
            patterns.add("Academic deadlines are competing directly with unplanned gaming time.");
        }
        if (a.isAffectsPhysicalOrSocial()) {
            patterns.add("Reduced offline physical movement or social time outside games.");
        }
        if (patterns.isEmpty()) {
            patterns.add("No major high-risk gaming patterns detected in this snapshot; keep maintaining clear boundaries.");
        }
        return patterns;
    }

    private String buildDefaultImpactText(RiskAssessment a) {
        String sleepPart = a.getAvgSleepDuration() < 7.0
                ? String.format("Averaging %.1f hours of sleep puts you below the 7–9 hour student target, which can reduce memory consolidation and focus.", a.getAvgSleepDuration())
                : String.format("Your %.1f hours of sleep is close to a healthy student baseline, though late-night screens can still reduce deep sleep quality.", a.getAvgSleepDuration());
        String academicPart = (a.isAffectsAcademics() || a.getMissedStudyTasks() > 0)
                ? String.format(" With %d missed study task(s) noted, shifting gaming to post-study reward windows will help protect coursework.", a.getMissedStudyTasks())
                : " Keeping study tasks scheduled ahead of gaming blocks helps sustain steady academic progress.";
        return sleepPart + academicPart;
    }

    private List<String> buildDefaultRecommendations(RiskAssessment a) {
        List<String> recs = new ArrayList<>();
        recs.add("Use the Smart Gaming Planner to set a hard stop at least 45 minutes before your target bedtime.");
        if (a.getMissedStudyTasks() > 0 || a.isAffectsAcademics()) {
            recs.add("Complete one high-priority study block before launching your first game each day.");
        } else {
            recs.add("Maintain a 50-minute study / 10-minute break rhythm during study sessions.");
        }
        if (a.isDifficultToStop()) {
            recs.add("Enable a 15-minute wrap-up alarm when starting the Gaming Tracker so you never start a new match near your limit.");
        }
        if (a.isAffectsPhysicalOrSocial() || a.getAvgDailyGamingHours() > 2.5) {
            recs.add("Schedule 25–30 minutes of brisk walking, stretching, or campus sports between afternoon classes and evening play.");
        } else {
            recs.add("Keep your daily gaming limit visible on the Dashboard and review your weekly balance every Sunday.");
        }
        return recs;
    }
}
