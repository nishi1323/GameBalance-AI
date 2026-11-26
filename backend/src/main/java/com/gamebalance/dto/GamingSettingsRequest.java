package com.gamebalance.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class GamingSettingsRequest {

    @NotNull(message = "Daily gaming limit is required")
    @Min(value = 15, message = "Daily limit must be at least 15 minutes")
    @Max(value = 720, message = "Daily limit cannot exceed 720 minutes")
    private Integer dailyGamingLimitMinutes;

    @NotBlank(message = "Preferred start time is required")
    private String preferredGamingStart;

    @NotBlank(message = "Preferred end time is required")
    private String preferredGamingEnd;

    public Integer getDailyGamingLimitMinutes() {
        return dailyGamingLimitMinutes;
    }

    public void setDailyGamingLimitMinutes(Integer dailyGamingLimitMinutes) {
        this.dailyGamingLimitMinutes = dailyGamingLimitMinutes;
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
}
