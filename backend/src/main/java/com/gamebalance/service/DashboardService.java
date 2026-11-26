package com.gamebalance.service;

import com.gamebalance.dto.*;
import com.gamebalance.model.GamingSession;
import com.gamebalance.model.RiskAssessment;
import com.gamebalance.model.SleepRecord;
import com.gamebalance.model.StudyTask;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    private final GamingSessionService gamingSessionService;
    private final StudyTaskService studyTaskService;
    private final SleepRecordService sleepRecordService;
    private final RiskAssessmentService riskAssessmentService;
    private final GroqService groqService;

    public DashboardService(GamingSessionService gamingSessionService,
                            StudyTaskService studyTaskService,
                            SleepRecordService sleepRecordService,
                            RiskAssessmentService riskAssessmentService,
                            GroqService groqService) {
        this.gamingSessionService = gamingSessionService;
        this.studyTaskService = studyTaskService;
        this.sleepRecordService = sleepRecordService;
        this.riskAssessmentService = riskAssessmentService;
        this.groqService = groqService;
    }

    public DashboardOverviewResponse getOverview(String userId) {
        GamingStatsResponse gamingStats = gamingSessionService.getStats(userId);
        StudySummaryResponse studySummary = studyTaskService.getSummary(userId);
        SleepSummaryResponse sleepSummary = sleepRecordService.getSummary(userId);
        RiskAssessment latestRisk = riskAssessmentService.getLatest(userId);

        List<GamingSession> recentSessions = gamingStats.getHistory().stream().limit(5).toList();
        List<StudyTask> upcomingTasks = studySummary.getUpcomingDeadlines().stream().limit(5).toList();
        SleepRecord latestSleep = sleepSummary.getRecords().isEmpty() ? null : sleepSummary.getRecords().get(0);

        String riskLevel = latestRisk != null ? latestRisk.getRiskCategory() : "NOT_ASSESSED";
        Integer riskScore = latestRisk != null ? latestRisk.getRiskScore() : null;

        String sleepSummaryText = latestSleep == null
                ? "No sleep logs recorded yet. Log last night's sleep to track your recovery."
                : String.format("Last logged: %.1f hrs (7-night average: %.1f hrs). %s",
                latestSleep.getTotalDurationHours(),
                sleepSummary.getAvgSleepHours(),
                latestSleep.isIrregularPattern() ? "Irregular sleep timing detected." : "Healthy sleep rhythm.");

        String dailySuggestion = buildDailySuggestion(
                gamingStats.getTodayMinutes(),
                gamingStats.getDailyLimitMinutes(),
                studySummary.getTodayStudyHours(),
                studySummary.getPendingCount(),
                sleepSummary.getAvgSleepHours(),
                riskLevel
        );

        DashboardOverviewResponse response = new DashboardOverviewResponse();
        response.setTodayGamingMinutes(gamingStats.getTodayMinutes());
        response.setDailyGamingLimitMinutes(gamingStats.getDailyLimitMinutes());
        response.setRemainingGamingMinutes(gamingStats.getRemainingMinutes());
        response.setGamingLimitExceeded(gamingStats.isExceededDailyLimit());
        response.setTotalStudyHours(studySummary.getWeeklyStudyHours());
        response.setTodayStudyHours(studySummary.getTodayStudyHours());
        response.setLatestSleepHours(sleepSummary.getLatestSleepHours());
        response.setAvgSleepHours(sleepSummary.getAvgSleepHours());
        response.setRiskLevel(riskLevel);
        response.setRiskScore(riskScore);
        response.setWeeklyGamingChart(gamingStats.getDailyChart());
        response.setWeeklyStudyChart(studySummary.getWeeklyChart());
        response.setRecentGamingSessions(recentSessions);
        response.setUpcomingStudyTasks(upcomingTasks);
        response.setLatestSleepRecord(latestSleep);
        response.setSleepSummaryText(sleepSummaryText);
        response.setAiWellnessSuggestion(dailySuggestion);
        return response;
    }

    private String buildDailySuggestion(long todayGamingMins, int limitMins, double todayStudyHours,
                                        long pendingTasks, double avgSleepHours, String riskLevel) {
        String prompt = String.format(
                "Student today snapshot: gaming=%d mins (limit=%d mins), completedStudyToday=%.1f hrs, pendingTasks=%d, avgSleep=%.1f hrs, riskEstimate=%s. " +
                "Provide 1 encouraging, actionable 2-sentence daily wellness tip for today (non-medical).",
                todayGamingMins, limitMins, todayStudyHours, pendingTasks, avgSleepHours, riskLevel
        );

        return groqService.generateSinglePrompt(
                "You are GameBalance AI, a supportive student wellness mentor. Give concise, practical 2-sentence daily guidance.",
                prompt,
                0.5,
                140
        ).orElseGet(() -> {
            if (todayGamingMins > limitMins) {
                return String.format(
                        "You've passed today's %d-minute gaming target. Step away for a 15-minute walk and tackle one of your %d pending study tasks before winding down tonight.",
                        limitMins, pendingTasks
                );
            }
            if (pendingTasks > 0 && todayStudyHours < 1.0) {
                return String.format(
                        "You have %d pending study task(s) and %d minutes of gaming budget remaining today. Complete a focused 45-minute study block first so your evening gaming feels guilt-free!",
                        pendingTasks, Math.max(0, limitMins - todayGamingMins)
                );
            }
            if (avgSleepHours > 0 && avgSleepHours < 7.0) {
                return String.format(
                        "Your recent sleep averages %.1f hours. Wrap up screens 45 minutes before bed tonight to boost focus and tomorrow's study retention.",
                        avgSleepHours
                );
            }
            return "You're maintaining a solid student balance today! Keep taking short stretch breaks every hour and stick to your planned gaming window.";
        });
    }
}
