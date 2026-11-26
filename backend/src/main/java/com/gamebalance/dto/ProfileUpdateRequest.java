package com.gamebalance.dto;

import jakarta.validation.constraints.*;

public class ProfileUpdateRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @NotNull(message = "Age is required")
    @Min(value = 13, message = "Age must be at least 13")
    @Max(value = 100, message = "Age must be 100 or less")
    private Integer age;

    @Size(max = 150, message = "College name must be under 150 characters")
    private String collegeName;

    @Min(value = 15, message = "Daily gaming limit must be at least 15 minutes")
    @Max(value = 720, message = "Daily gaming limit cannot exceed 720 minutes")
    private Integer dailyGamingLimitMinutes;

    private String preferredGamingStart;
    private String preferredGamingEnd;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }

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
