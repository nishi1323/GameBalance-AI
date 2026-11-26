package com.gamebalance.dto;

import com.gamebalance.model.StudyTask;

import java.util.List;

public class StudySummaryResponse {
    private List<StudyTask> tasks;
    private long pendingCount;
    private long completedCount;
    private double todayStudyHours;
    private double weeklyStudyHours;
    private List<StudyTask> upcomingDeadlines;
    private List<StudyTask> upcomingReminders;
    private List<DailyChartPoint> weeklyChart;

    public List<StudyTask> getTasks() {
        return tasks;
    }

    public void setTasks(List<StudyTask> tasks) {
        this.tasks = tasks;
    }

    public long getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(long pendingCount) {
        this.pendingCount = pendingCount;
    }

    public long getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(long completedCount) {
        this.completedCount = completedCount;
    }

    public double getTodayStudyHours() {
        return todayStudyHours;
    }

    public void setTodayStudyHours(double todayStudyHours) {
        this.todayStudyHours = todayStudyHours;
    }

    public double getWeeklyStudyHours() {
        return weeklyStudyHours;
    }

    public void setWeeklyStudyHours(double weeklyStudyHours) {
        this.weeklyStudyHours = weeklyStudyHours;
    }

    public List<StudyTask> getUpcomingDeadlines() {
        return upcomingDeadlines;
    }

    public void setUpcomingDeadlines(List<StudyTask> upcomingDeadlines) {
        this.upcomingDeadlines = upcomingDeadlines;
    }

    public List<StudyTask> getUpcomingReminders() {
        return upcomingReminders;
    }

    public void setUpcomingReminders(List<StudyTask> upcomingReminders) {
        this.upcomingReminders = upcomingReminders;
    }

    public List<DailyChartPoint> getWeeklyChart() {
        return weeklyChart;
    }

    public void setWeeklyChart(List<DailyChartPoint> weeklyChart) {
        this.weeklyChart = weeklyChart;
    }
}
