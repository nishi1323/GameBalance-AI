package com.gamebalance.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "risk_assessments")
public class RiskAssessment {

    @Id
    private String id;

    @Indexed
    private String userId;

    private double avgDailyGamingHours;
    private double weekendGamingHours;
    private double avgSleepDuration;
    private int lateNightGamingSessions;
    private int missedStudyTasks;
    private boolean affectsAcademics;
    private boolean difficultToStop;
    private boolean affectsPhysicalOrSocial;

    private int riskScore;
    private String riskCategory;
    private String explanation;
    private List<String> unhealthyPatterns = new ArrayList<>();
    private String sleepAndAcademicImpact;
    private List<String> recommendations = new ArrayList<>();
    private String disclaimer;
    private boolean aiPowered;

    private Instant createdAt;

    public RiskAssessment() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

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

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public String getRiskCategory() {
        return riskCategory;
    }

    public void setRiskCategory(String riskCategory) {
        this.riskCategory = riskCategory;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public List<String> getUnhealthyPatterns() {
        return unhealthyPatterns;
    }

    public void setUnhealthyPatterns(List<String> unhealthyPatterns) {
        this.unhealthyPatterns = unhealthyPatterns;
    }

    public String getSleepAndAcademicImpact() {
        return sleepAndAcademicImpact;
    }

    public void setSleepAndAcademicImpact(String sleepAndAcademicImpact) {
        this.sleepAndAcademicImpact = sleepAndAcademicImpact;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }

    public boolean isAiPowered() {
        return aiPowered;
    }

    public void setAiPowered(boolean aiPowered) {
        this.aiPowered = aiPowered;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
