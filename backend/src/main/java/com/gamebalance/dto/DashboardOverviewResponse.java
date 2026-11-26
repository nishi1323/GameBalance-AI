package com.gamebalance.dto;

import com.gamebalance.model.GamingSession;
import com.gamebalance.model.SleepRecord;
import com.gamebalance.model.StudyTask;

import java.util.List;

public class DashboardOverviewResponse {
    private long todayGamingMinutes;
    private int dailyGamingLimitMinutes;
    private long remainingGamingMinutes;
    private boolean gamingLimitExceeded;
    private double totalStudyHours;
    private double todayStudyHours;
    private double latestSleepHours;
    private double avgSleepHours;
    private String riskLevel;
    private Integer riskScore;
    private List<DailyChartPoint> weeklyGamingChart;
    private List<DailyChartPoint> weeklyStudyChart;
    private List<GamingSession> recentGamingSessions;
    private List<StudyTask> upcomingStudyTasks;
    private SleepRecord latestSleepRecord;
    private String sleepSummaryText;
    private String aiWellnessSuggestion;

    public long getTodayGamingMinutes() {
        return todayGamingMinutes;
    }

    public void setTodayGamingMinutes(long todayGamingMinutes) {
        this.todayGamingMinutes = todayGamingMinutes;
    }

    public int getDailyGamingLimitMinutes() {
        return dailyGamingLimitMinutes;
    }

    public void setDailyGamingLimitMinutes(int dailyGamingLimitMinutes) {
        this.dailyGamingLimitMinutes = dailyGamingLimitMinutes;
    }

    public long getRemainingGamingMinutes() {
        return remainingGamingMinutes;
    }

    public void setRemainingGamingMinutes(long remainingGamingMinutes) {
        this.remainingGamingMinutes = remainingGamingMinutes;
    }

    public boolean isGamingLimitExceeded() {
        return gamingLimitExceeded;
    }

    public void setGamingLimitExceeded(boolean gamingLimitExceeded) {
        this.gamingLimitExceeded = gamingLimitExceeded;
    }

    public double getTotalStudyHours() {
        return totalStudyHours;
    }

    public void setTotalStudyHours(double totalStudyHours) {
        this.totalStudyHours = totalStudyHours;
    }

    public double getTodayStudyHours() {
        return todayStudyHours;
    }

    public void setTodayStudyHours(double todayStudyHours) {
        this.todayStudyHours = todayStudyHours;
    }

    public double getLatestSleepHours() {
        return latestSleepHours;
    }

    public void setLatestSleepHours(double latestSleepHours) {
        this.latestSleepHours = latestSleepHours;
    }

    public double getAvgSleepHours() {
        return avgSleepHours;
    }

    public void setAvgSleepHours(double avgSleepHours) {
        this.avgSleepHours = avgSleepHours;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public List<DailyChartPoint> getWeeklyGamingChart() {
        return weeklyGamingChart;
    }

    public void setWeeklyGamingChart(List<DailyChartPoint> weeklyGamingChart) {
        this.weeklyGamingChart = weeklyGamingChart;
    }

    public List<DailyChartPoint> getWeeklyStudyChart() {
        return weeklyStudyChart;
    }

    public void setWeeklyStudyChart(List<DailyChartPoint> weeklyStudyChart) {
        this.weeklyStudyChart = weeklyStudyChart;
    }

    public List<GamingSession> getRecentGamingSessions() {
        return recentGamingSessions;
    }

    public void setRecentGamingSessions(List<GamingSession> recentGamingSessions) {
        this.recentGamingSessions = recentGamingSessions;
    }

    public List<StudyTask> getUpcomingStudyTasks() {
        return upcomingStudyTasks;
    }

    public void setUpcomingStudyTasks(List<StudyTask> upcomingStudyTasks) {
        this.upcomingStudyTasks = upcomingStudyTasks;
    }

    public SleepRecord getLatestSleepRecord() {
        return latestSleepRecord;
    }

    public void setLatestSleepRecord(SleepRecord latestSleepRecord) {
        this.latestSleepRecord = latestSleepRecord;
    }

    public String getSleepSummaryText() {
        return sleepSummaryText;
    }

    public void setSleepSummaryText(String sleepSummaryText) {
        this.sleepSummaryText = sleepSummaryText;
    }

    public String getAiWellnessSuggestion() {
        return aiWellnessSuggestion;
    }

    public void setAiWellnessSuggestion(String aiWellnessSuggestion) {
        this.aiWellnessSuggestion = aiWellnessSuggestion;
    }
}
