package com.gamebalance.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class RiskAssessmentRequest {

    @DecimalMin(value = "0.0", message = "Average daily gaming hours cannot be negative")
    @DecimalMax(value = "24.0", message = "Average daily gaming hours cannot exceed 24")
    private double avgDailyGamingHours;

    @DecimalMin(value = "0.0", message = "Weekend gaming hours cannot be negative")
    @DecimalMax(value = "24.0", message = "Weekend gaming hours cannot exceed 24")
    private double weekendGamingHours;

    @DecimalMin(value = "0.0", message = "Average sleep duration cannot be negative")
    @DecimalMax(value = "24.0", message = "Average sleep duration cannot exceed 24")
    private double avgSleepDuration;

    @Min(value = 0, message = "Late-night sessions cannot be negative")
    @Max(value = 30, message = "Late-night sessions per week seems too high")
    private int lateNightGamingSessions;

    @Min(value = 0, message = "Missed study tasks cannot be negative")
    @Max(value = 100, message = "Missed study tasks cannot exceed 100")
    private int missedStudyTasks;

    private boolean affectsAcademics;
    private boolean difficultToStop;
    private boolean affectsPhysicalOrSocial;

    public double getAvgDailyGamingHours() {
        return avgDailyGamingHours;
    }

    public void setAvgDailyGamingHours(double avgDailyGamingHours) {
        this.avgDailyGamingHours = avgDailyGamingHours;
    }

    public double getWeekendGamingHours() {
        return weekendGamingHours;
    }

    public void setWeekendGamingHours(double weekendGamingHours) {
        this.weekendGamingHours = weekendGamingHours;
    }

    public double getAvgSleepDuration() {
        return avgSleepDuration;
    }

    public void setAvgSleepDuration(double avgSleepDuration) {
        this.avgSleepDuration = avgSleepDuration;
    }

    public int getLateNightGamingSessions() {
        return lateNightGamingSessions;
    }

    public void setLateNightGamingSessions(int lateNightGamingSessions) {
        this.lateNightGamingSessions = lateNightGamingSessions;
    }

    public int getMissedStudyTasks() {
        return missedStudyTasks;
    }

    public void setMissedStudyTasks(int missedStudyTasks) {
        this.missedStudyTasks = missedStudyTasks;
    }

    public boolean isAffectsAcademics() {
        return affectsAcademics;
    }

    public void setAffectsAcademics(boolean affectsAcademics) {
        this.affectsAcademics = affectsAcademics;
    }

    public boolean isDifficultToStop() {
        return difficultToStop;
    }

    public void setDifficultToStop(boolean difficultToStop) {
        this.difficultToStop = difficultToStop;
    }

    public boolean isAffectsPhysicalOrSocial() {
        return affectsPhysicalOrSocial;
    }

    public void setAffectsPhysicalOrSocial(boolean affectsPhysicalOrSocial) {
        this.affectsPhysicalOrSocial = affectsPhysicalOrSocial;
    }
}
