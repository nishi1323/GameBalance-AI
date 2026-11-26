package com.gamebalance.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "sleep_records")
public class SleepRecord {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private LocalDate recordDate;

    private LocalDateTime sleepTime;

    private LocalDateTime wakeUpTime;

    private double totalDurationHours;

    private int qualityRating = 3;

    private boolean hadLateNightGaming;

    private boolean irregularPattern;

    private String notes;

    private Instant createdAt;

    public SleepRecord() {
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

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }

    public LocalDateTime getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(LocalDateTime sleepTime) {
        this.sleepTime = sleepTime;
    }

    public LocalDateTime getWakeUpTime() {
        return wakeUpTime;
    }

    public void setWakeUpTime(LocalDateTime wakeUpTime) {
        this.wakeUpTime = wakeUpTime;
    }

    public double getTotalDurationHours() {
        return totalDurationHours;
    }

    public void setTotalDurationHours(double totalDurationHours) {
        this.totalDurationHours = totalDurationHours;
    }

    public int getQualityRating() {
        return qualityRating;
    }

    public void setQualityRating(int qualityRating) {
        this.qualityRating = qualityRating;
    }

    public boolean isHadLateNightGaming() {
        return hadLateNightGaming;
    }

    public void setHadLateNightGaming(boolean hadLateNightGaming) {
        this.hadLateNightGaming = hadLateNightGaming;
    }

    public boolean isIrregularPattern() {
        return irregularPattern;
    }

    public void setIrregularPattern(boolean irregularPattern) {
        this.irregularPattern = irregularPattern;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
