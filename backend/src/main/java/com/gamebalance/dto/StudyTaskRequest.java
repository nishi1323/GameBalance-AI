package com.gamebalance.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public class StudyTaskRequest {

    @NotBlank(message = "Subject name is required")
    @Size(max = 100, message = "Subject name must be under 100 characters")
    private String subjectName;

    @NotBlank(message = "Task title is required")
    @Size(max = 150, message = "Title must be under 150 characters")
    private String title;

    @Size(max = 500, message = "Description must be under 500 characters")
    private String description;

    @NotNull(message = "Due date and time is required")
    private LocalDateTime dueDateTime;

    @DecimalMin(value = "0.25", message = "Estimated hours must be at least 0.25")
    @DecimalMax(value = "24.0", message = "Estimated hours cannot exceed 24")
    private double estimatedHours = 1.0;

    @NotBlank(message = "Priority is required")
    @Pattern(regexp = "LOW|MEDIUM|HIGH", message = "Priority must be LOW, MEDIUM, or HIGH")
    private String priority = "MEDIUM";

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDueDateTime() {
        return dueDateTime;
    }

    public void setDueDateTime(LocalDateTime dueDateTime) {
        this.dueDateTime = dueDateTime;
    }

    public double getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(double estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }
}
