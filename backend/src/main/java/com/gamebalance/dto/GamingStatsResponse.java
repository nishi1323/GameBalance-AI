package com.gamebalance.dto;

import com.gamebalance.model.GamingSession;

import java.util.List;

public class GamingStatsResponse {
    private GamingSession activeSession;
    private long todayMinutes;
    private long weeklyMinutes;
    private long monthlyMinutes;
    private long totalMinutes;
    private int dailyLimitMinutes;
    private long remainingMinutes;
    private boolean exceededDailyLimit;
    private List<DailyChartPoint> dailyChart;
    private List<GamingSession> history;

    public GamingSession getActiveSession() {
        return activeSession;
    }

    public void setActiveSession(GamingSession activeSession) {
        this.activeSession = activeSession;
    }

    public long getTodayMinutes() {
        return todayMinutes;
    }

    public void setTodayMinutes(long todayMinutes) {
        this.todayMinutes = todayMinutes;
    }

    public long getWeeklyMinutes() {
        return weeklyMinutes;
    }

    public void setWeeklyMinutes(long weeklyMinutes) {
        this.weeklyMinutes = weeklyMinutes;
    }

    public long getMonthlyMinutes() {
        return monthlyMinutes;
    }

    public void setMonthlyMinutes(long monthlyMinutes) {
        this.monthlyMinutes = monthlyMinutes;
    }

    public long getTotalMinutes() {
        return totalMinutes;
    }

    public void setTotalMinutes(long totalMinutes) {
        this.totalMinutes = totalMinutes;
    }

    public int getDailyLimitMinutes() {
        return dailyLimitMinutes;
    }

    public void setDailyLimitMinutes(int dailyLimitMinutes) {
        this.dailyLimitMinutes = dailyLimitMinutes;
    }

    public long getRemainingMinutes() {
        return remainingMinutes;
    }

    public void setRemainingMinutes(long remainingMinutes) {
        this.remainingMinutes = remainingMinutes;
    }

    public boolean isExceededDailyLimit() {
        return exceededDailyLimit;
    }

    public void setExceededDailyLimit(boolean exceededDailyLimit) {
        this.exceededDailyLimit = exceededDailyLimit;
    }

    public List<DailyChartPoint> getDailyChart() {
        return dailyChart;
    }

    public void setDailyChart(List<DailyChartPoint> dailyChart) {
        this.dailyChart = dailyChart;
    }

    public List<GamingSession> getHistory() {
        return history;
    }

    public void setHistory(List<GamingSession> history) {
        this.history = history;
    }
}
