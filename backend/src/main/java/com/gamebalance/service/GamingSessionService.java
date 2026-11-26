package com.gamebalance.service;

import com.gamebalance.dto.DailyChartPoint;
import com.gamebalance.dto.GamingStatsResponse;
import com.gamebalance.dto.StartGamingSessionRequest;
import com.gamebalance.dto.StopGamingSessionRequest;
import com.gamebalance.exception.BadRequestException;
import com.gamebalance.exception.ConflictException;
import com.gamebalance.exception.ResourceNotFoundException;
import com.gamebalance.model.GamingSession;
import com.gamebalance.model.User;
import com.gamebalance.repository.GamingSessionRepository;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GamingSessionService {

    private final GamingSessionRepository sessionRepository;
    private final AuthService authService;

    public GamingSessionService(GamingSessionRepository sessionRepository, AuthService authService) {
        this.sessionRepository = sessionRepository;
        this.authService = authService;
    }

    public GamingSession startSession(String userId, StartGamingSessionRequest request) {
        sessionRepository.findFirstByUserIdAndActiveTrue(userId).ifPresent(existing -> {
            throw new ConflictException("You already have an active gaming session for '" + existing.getGameName() + "'. Stop it before starting a new one.");
        });

        Instant now = Instant.now();
        ZonedDateTime localNow = now.atZone(ZoneId.systemDefault());

        GamingSession session = new GamingSession();
        session.setUserId(userId);
        session.setGameName(request.getGameName().trim());
        session.setStartTime(now);
        session.setSessionDate(localNow.toLocalDate());
        session.setActive(true);
        session.setLateNight(isLateNightHour(localNow.getHour()));
        session.setNotes(request.getNotes() != null ? request.getNotes().trim() : "");
        session.setTotalDurationMinutes(0);
        session.setTotalDurationSeconds(0);
        session.setCreatedAt(now);

        return sessionRepository.save(session);
    }

    public GamingSession stopSession(String userId, String sessionId, StopGamingSessionRequest request) {
        GamingSession session = sessionRepository.findById(sessionId)
                .filter(s -> s.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Gaming session not found."));

        if (!session.isActive()) {
            throw new BadRequestException("This gaming session has already been stopped.");
        }

        Instant endTime = Instant.now();
        long seconds = Math.max(1, Duration.between(session.getStartTime(), endTime).getSeconds());
        long minutes = Math.max(1, Math.round(seconds / 60.0));

        ZonedDateTime localEnd = endTime.atZone(ZoneId.systemDefault());
        session.setEndTime(endTime);
        session.setTotalDurationSeconds(seconds);
        session.setTotalDurationMinutes(minutes);
        session.setActive(false);
        session.setLateNight(session.isLateNight() || isLateNightHour(localEnd.getHour()));
        if (request != null && request.getNotes() != null && !request.getNotes().isBlank()) {
            session.setNotes(request.getNotes().trim());
        }

        return sessionRepository.save(session);
    }

    public GamingSession getActiveSession(String userId) {
        return sessionRepository.findFirstByUserIdAndActiveTrue(userId).orElse(null);
    }

    public GamingStatsResponse getStats(String userId) {
        User user = authService.getUserEntity(userId);
        int dailyLimit = user.getDailyGamingLimitMinutes() != null ? user.getDailyGamingLimitMinutes() : 120;

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);
        LocalDate monthStart = today.withDayOfMonth(1);

        List<GamingSession> allSessions = sessionRepository.findByUserIdOrderByStartTimeDesc(userId);
        GamingSession activeSession = allSessions.stream().filter(GamingSession::isActive).findFirst().orElse(null);

        long todayMinutes = sumMinutesForRange(allSessions, today, today);
        long weeklyMinutes = sumMinutesForRange(allSessions, weekStart, today);
        long monthlyMinutes = sumMinutesForRange(allSessions, monthStart, today);
        long totalMinutes = allSessions.stream().mapToLong(this::effectiveMinutes).sum();

        Map<LocalDate, Long> byDate = allSessions.stream()
                .filter(s -> s.getSessionDate() != null && !s.getSessionDate().isBefore(weekStart) && !s.getSessionDate().isAfter(today))
                .collect(Collectors.groupingBy(
                        GamingSession::getSessionDate,
                        Collectors.summingLong(this::effectiveMinutes)
                ));

        List<DailyChartPoint> dailyChart = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String label = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            long mins = byDate.getOrDefault(date, 0L);
            dailyChart.add(new DailyChartPoint(date.toString(), label, mins, dailyLimit));
        }

        GamingStatsResponse response = new GamingStatsResponse();
        response.setActiveSession(activeSession);
        response.setTodayMinutes(todayMinutes);
        response.setWeeklyMinutes(weeklyMinutes);
        response.setMonthlyMinutes(monthlyMinutes);
        response.setTotalMinutes(totalMinutes);
        response.setDailyLimitMinutes(dailyLimit);
        response.setRemainingMinutes(Math.max(0, dailyLimit - todayMinutes));
        response.setExceededDailyLimit(todayMinutes > dailyLimit);
        response.setDailyChart(dailyChart);
        response.setHistory(allSessions);
        return response;
    }

    public void deleteSession(String userId, String sessionId) {
        GamingSession session = sessionRepository.findById(sessionId)
                .filter(s -> s.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Gaming session not found."));
        sessionRepository.delete(session);
    }

    public long getTodayGamingMinutes(String userId) {
        LocalDate today = LocalDate.now();
        List<GamingSession> todaySessions = sessionRepository.findByUserIdAndSessionDateOrderByStartTimeDesc(userId, today);
        return todaySessions.stream().mapToLong(this::effectiveMinutes).sum();
    }

    public long getGamingMinutesForDate(String userId, LocalDate date) {
        return sessionRepository.findByUserIdAndSessionDateOrderByStartTimeDesc(userId, date)
                .stream()
                .mapToLong(this::effectiveMinutes)
                .sum();
    }

    private long sumMinutesForRange(List<GamingSession> sessions, LocalDate start, LocalDate end) {
        return sessions.stream()
                .filter(s -> s.getSessionDate() != null && !s.getSessionDate().isBefore(start) && !s.getSessionDate().isAfter(end))
                .mapToLong(this::effectiveMinutes)
                .sum();
    }

    private long effectiveMinutes(GamingSession session) {
        if (!session.isActive()) {
            return session.getTotalDurationMinutes();
        }
        long elapsedSeconds = Math.max(0, Duration.between(session.getStartTime(), Instant.now()).getSeconds());
        return elapsedSeconds / 60;
    }

    private boolean isLateNightHour(int hour) {
        return hour >= 23 || hour < 5;
    }
}
