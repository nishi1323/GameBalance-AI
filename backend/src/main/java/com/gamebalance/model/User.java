package com.gamebalance.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String fullName;

    @Indexed(unique = true)
    private String email;

    private String passwordHash;

    private Integer age;

    private String collegeName;

    private Integer dailyGamingLimitMinutes = 120;

    private String preferredGamingStart = "18:00";

    private String preferredGamingEnd = "21:00";

    private Instant createdAt;

    private Instant updatedAt;

    public User() {
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
