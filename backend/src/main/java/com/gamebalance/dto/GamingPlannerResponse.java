package com.gamebalance.dto;

import com.gamebalance.model.GamingPlan;

import java.util.List;

public class GamingPlannerResponse {
    private int dailyLimitMinutes;
    private String preferredGamingStart;
    private String preferredGamingEnd;
    private long todayPlannedMinutes;
    private long todayActualMinutes;
    private long availableMinutesToday;
    private boolean exceededLimit;
    private List<GamingPlan> plans;
    private List<DailyChartPoint> plannedVsActualChart;
    private List<String> recommendations;

    public int getDailyLimitMinutes() {
        return dailyLimitMinutes;
    }

    public void setDailyLimitMinutes(int dailyLimitMinutes) {
        this.dailyLimitMinutes = dailyLimitMinutes;
    }

    public String getPreferredGamingStart() {
        return preferredGamingStart;
    }

    public void setPreferredGamingStart(String preferredGamingStart) {
        this.preferredGamingStart = preferredGamingStart;
    }

    public String getPreferredGamingEnd() {
        return preferredGamingEnd;
    }

    public void setPreferredGamingEnd(String preferredGamingEnd) {
        this.preferredGamingEnd = preferredGamingEnd;
    }

    public long getTodayPlannedMinutes() {
        return todayPlannedMinutes;
    }

    public void setTodayPlannedMinutes(long todayPlannedMinutes) {
        this.todayPlannedMinutes = todayPlannedMinutes;
    }

    public long getTodayActualMinutes() {
        return todayActualMinutes;
    }

    public void setTodayActualMinutes(long todayActualMinutes) {
        this.todayActualMinutes = todayActualMinutes;
    }

    public long getAvailableMinutesToday() {
        return availableMinutesToday;
    }

    public void setAvailableMinutesToday(long availableMinutesToday) {
        this.availableMinutesToday = availableMinutesToday;
    }

    public boolean isExceededLimit() {
        return exceededLimit;
    }

    public void setExceededLimit(boolean exceededLimit) {
        this.exceededLimit = exceededLimit;
    }

    public List<GamingPlan> getPlans() {
        return plans;
    }

    public void setPlans(List<GamingPlan> plans) {
        this.plans = plans;
    }

    public List<DailyChartPoint> getPlannedVsActualChart() {
        return plannedVsActualChart;
    }

    public void setPlannedVsActualChart(List<DailyChartPoint> plannedVsActualChart) {
        this.plannedVsActualChart = plannedVsActualChart;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }
}
