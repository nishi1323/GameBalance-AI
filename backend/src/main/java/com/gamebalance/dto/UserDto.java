package com.gamebalance.dto;

import com.gamebalance.model.User;

import java.time.Instant;

public class UserDto {
    private String id;
    private String fullName;
    private String email;
    private Integer age;
    private String collegeName;
    private Integer dailyGamingLimitMinutes;
    private String preferredGamingStart;
    private String preferredGamingEnd;
    private Instant createdAt;

    public static UserDto from(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setAge(user.getAge());
        dto.setCollegeName(user.getCollegeName());
        dto.setDailyGamingLimitMinutes(user.getDailyGamingLimitMinutes());
        dto.setPreferredGamingStart(user.getPreferredGamingStart());
        dto.setPreferredGamingEnd(user.getPreferredGamingEnd());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
