package com.gamebalance.dto;

import com.gamebalance.model.SleepRecord;

import java.util.List;

public class SleepSummaryResponse {
    private List<SleepRecord> records;
    private double avgSleepHours;
    private double latestSleepHours;
    private long irregularNightsCount;
    private double lateNightGamingSleepAvg;
    private double regularSleepAvg;
    private List<DailyChartPoint> weeklyChart;
    private String aiSuggestion;
    private List<String> dailyRecommendations;

    public List<SleepRecord> getRecords() {
        return records;
    }

    public void setRecords(List<SleepRecord> records) {
        this.records = records;
    }

    public double getAvgSleepHours() {
        return avgSleepHours;
    }

    public void setAvgSleepHours(double avgSleepHours) {
        this.avgSleepHours = avgSleepHours;
    }

    public double getLatestSleepHours() {
        return latestSleepHours;
    }

    public void setLatestSleepHours(double latestSleepHours) {
        this.latestSleepHours = latestSleepHours;
    }

    public long getIrregularNightsCount() {
        return irregularNightsCount;
    }

    public void setIrregularNightsCount(long irregularNightsCount) {
        this.irregularNightsCount = irregularNightsCount;
    }

    public double getLateNightGamingSleepAvg() {
        return lateNightGamingSleepAvg;
    }

    public void setLateNightGamingSleepAvg(double lateNightGamingSleepAvg) {
        this.lateNightGamingSleepAvg = lateNightGamingSleepAvg;
    }

    public double getRegularSleepAvg() {
        return regularSleepAvg;
    }

    public void setRegularSleepAvg(double regularSleepAvg) {
        this.regularSleepAvg = regularSleepAvg;
    }

    public List<DailyChartPoint> getWeeklyChart() {
        return weeklyChart;
    }

    public void setWeeklyChart(List<DailyChartPoint> weeklyChart) {
        this.weeklyChart = weeklyChart;
    }

    public String getAiSuggestion() {
        return aiSuggestion;
    }

    public void setAiSuggestion(String aiSuggestion) {
        this.aiSuggestion = aiSuggestion;
    }

    public List<String> getDailyRecommendations() {
        return dailyRecommendations;
    }

    public void setDailyRecommendations(List<String> dailyRecommendations) {
        this.dailyRecommendations = dailyRecommendations;
    }
}
