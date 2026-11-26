package com.gamebalance.service;

import com.gamebalance.dto.DailyChartPoint;
import com.gamebalance.dto.StudySummaryResponse;
import com.gamebalance.dto.StudyTaskRequest;
import com.gamebalance.exception.ResourceNotFoundException;
import com.gamebalance.model.StudyTask;
import com.gamebalance.repository.StudyTaskRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StudyTaskService {

    private final StudyTaskRepository taskRepository;

    public StudyTaskService(StudyTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public StudyTask createTask(String userId, StudyTaskRequest request) {
        StudyTask task = new StudyTask();
        task.setUserId(userId);
        applyFields(task, request);
        task.setCompleted(false);
        Instant now = Instant.now();
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        return taskRepository.save(task);
    }

    public StudyTask updateTask(String userId, String taskId, StudyTaskRequest request) {
        StudyTask task = getOwnedTask(userId, taskId);
        applyFields(task, request);
        task.setUpdatedAt(Instant.now());
        return taskRepository.save(task);
    }

    public StudyTask toggleComplete(String userId, String taskId) {
        StudyTask task = getOwnedTask(userId, taskId);
        boolean nextState = !task.isCompleted();
        task.setCompleted(nextState);
        task.setCompletedAt(nextState ? Instant.now() : null);
        task.setUpdatedAt(Instant.now());
        return taskRepository.save(task);
    }

    public void deleteTask(String userId, String taskId) {
        StudyTask task = getOwnedTask(userId, taskId);
        taskRepository.delete(task);
    }

    public StudySummaryResponse getSummary(String userId) {
        List<StudyTask> allTasks = taskRepository.findByUserIdOrderByDueDateTimeAsc(userId);
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderCutoff = now.plusHours(24);

        long pendingCount = allTasks.stream().filter(t -> !t.isCompleted()).count();
        long completedCount = allTasks.stream().filter(StudyTask::isCompleted).count();

        double todayStudyHours = round2(allTasks.stream()
                .filter(StudyTask::isCompleted)
                .filter(t -> completedDate(t).equals(today))
                .mapToDouble(StudyTask::getEstimatedHours)
                .sum());

        double weeklyStudyHours = round2(allTasks.stream()
                .filter(StudyTask::isCompleted)
                .filter(t -> {
                    LocalDate d = completedDate(t);
                    return !d.isBefore(weekStart) && !d.isAfter(today);
                })
                .mapToDouble(StudyTask::getEstimatedHours)
                .sum());

        List<StudyTask> upcomingDeadlines = allTasks.stream()
                .filter(t -> !t.isCompleted())
                .limit(6)
                .toList();

        List<StudyTask> upcomingReminders = allTasks.stream()
                .filter(t -> !t.isCompleted() && t.getDueDateTime() != null)
                .filter(t -> !t.getDueDateTime().isBefore(now.minusMinutes(15)) && !t.getDueDateTime().isAfter(reminderCutoff))
                .toList();

        Map<LocalDate, Double> hoursByDate = allTasks.stream()
                .filter(StudyTask::isCompleted)
                .filter(t -> {
                    LocalDate d = completedDate(t);
                    return !d.isBefore(weekStart) && !d.isAfter(today);
                })
                .collect(Collectors.groupingBy(
                        this::completedDate,
                        Collectors.summingDouble(StudyTask::getEstimatedHours)
                ));

        List<DailyChartPoint> weeklyChart = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String label = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            weeklyChart.add(new DailyChartPoint(date.toString(), label, round2(hoursByDate.getOrDefault(date, 0.0))));
        }

        StudySummaryResponse response = new StudySummaryResponse();
        response.setTasks(allTasks);
        response.setPendingCount(pendingCount);
        response.setCompletedCount(completedCount);
        response.setTodayStudyHours(todayStudyHours);
        response.setWeeklyStudyHours(weeklyStudyHours);
        response.setUpcomingDeadlines(upcomingDeadlines);
        response.setUpcomingReminders(upcomingReminders);
        response.setWeeklyChart(weeklyChart);
        return response;
    }

    private StudyTask getOwnedTask(String userId, String taskId) {
        return taskRepository.findById(taskId)
                .filter(t -> t.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Study task not found."));
    }

    private void applyFields(StudyTask task, StudyTaskRequest request) {
        task.setSubjectName(request.getSubjectName().trim());
        task.setTitle(request.getTitle().trim());
        task.setDescription(request.getDescription() != null ? request.getDescription().trim() : "");
        task.setDueDateTime(request.getDueDateTime());
        task.setEstimatedHours(request.getEstimatedHours());
        task.setPriority(request.getPriority().toUpperCase());
    }

    private LocalDate completedDate(StudyTask task) {
        if (task.getCompletedAt() != null) {
            return task.getCompletedAt().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        if (task.getDueDateTime() != null) {
            return task.getDueDateTime().toLocalDate();
        }
        return LocalDate.now();
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
