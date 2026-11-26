package com.gamebalance.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class SleepRecordRequest {

    @NotNull(message = "Sleep time is required")
    private LocalDateTime sleepTime;

    @NotNull(message = "Wake-up time is required")
    private LocalDateTime wakeUpTime;

    @Min(value = 1, message = "Quality rating must be between 1 and 5")
    @Max(value = 5, message = "Quality rating must be between 1 and 5")
    private int qualityRating = 3;

    private Boolean hadLateNightGaming;

    @Size(max = 300, message = "Notes must be under 300 characters")
    private String notes;

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

    public int getQualityRating() {
        return qualityRating;
    }

    public void setQualityRating(int qualityRating) {
        this.qualityRating = qualityRating;
    }

    public Boolean getHadLateNightGaming() {
        return hadLateNightGaming;
    }

    public void setHadLateNightGaming(Boolean hadLateNightGaming) {
        this.hadLateNightGaming = hadLateNightGaming;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
