package com.gamebalance.service;

import com.gamebalance.dto.DailyChartPoint;
import com.gamebalance.dto.GamingPlanRequest;
import com.gamebalance.dto.GamingPlannerResponse;
import com.gamebalance.dto.GamingSettingsRequest;
import com.gamebalance.exception.BadRequestException;
import com.gamebalance.exception.ConflictException;
import com.gamebalance.exception.ResourceNotFoundException;
import com.gamebalance.model.GamingPlan;
import com.gamebalance.model.User;
import com.gamebalance.repository.GamingPlanRepository;
import com.gamebalance.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class GamingPlanService {

    private final GamingPlanRepository planRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final GamingSessionService gamingSessionService;

    public GamingPlanService(GamingPlanRepository planRepository,
                             UserRepository userRepository,
                             AuthService authService,
                             GamingSessionService gamingSessionService) {
        this.planRepository = planRepository;
        this.userRepository = userRepository;
        this.authService = authService;
        this.gamingSessionService = gamingSessionService;
    }

    public GamingPlan createPlan(String userId, GamingPlanRequest request) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BadRequestException("End time must be after start time.");
        }

        List<GamingPlan> sameDayPlans = planRepository.findByUserIdAndPlanDateOrderByStartTimeAsc(userId, request.getPlanDate());
        for (GamingPlan existing : sameDayPlans) {
            if (request.getStartTime().isBefore(existing.getEndTime())
                    && existing.getStartTime().isBefore(request.getEndTime())) {
                throw new ConflictException(String.format(
                        "Overlapping session: '%s' is already planned from %s to %s on %s.",
                        existing.getGameName(), existing.getStartTime(), existing.getEndTime(), request.getPlanDate()
                ));
            }
        }

        long durationMinutes = Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
        if (durationMinutes < 10) {
            throw new BadRequestException("Planned gaming session must be at least 10 minutes long.");
        }

        GamingPlan plan = new GamingPlan();
        plan.setUserId(userId);
        plan.setGameName(request.getGameName().trim());
        plan.setPlanDate(request.getPlanDate());
        plan.setStartTime(request.getStartTime());
        plan.setEndTime(request.getEndTime());
        plan.setPlannedDurationMinutes(durationMinutes);
        plan.setNotes(request.getNotes() != null ? request.getNotes().trim() : "");
        plan.setCreatedAt(Instant.now());

        return planRepository.save(plan);
    }

    public void deletePlan(String userId, String planId) {
        GamingPlan plan = planRepository.findById(planId)
                .filter(p -> p.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Gaming plan not found."));
        planRepository.delete(plan);
    }

    public GamingPlannerResponse updateSettings(String userId, GamingSettingsRequest request) {
        try {
            LocalTime start = LocalTime.parse(request.getPreferredGamingStart());
            LocalTime end = LocalTime.parse(request.getPreferredGamingEnd());
            if (!end.isAfter(start)) {
                throw new BadRequestException("Preferred gaming end time must be after start time.");
            }
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Invalid time format. Use HH:mm.");
        }

        User user = authService.getUserEntity(userId);
        user.setDailyGamingLimitMinutes(request.getDailyGamingLimitMinutes());
        user.setPreferredGamingStart(request.getPreferredGamingStart());
        user.setPreferredGamingEnd(request.getPreferredGamingEnd());
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        return getPlannerOverview(userId);
    }

    public GamingPlannerResponse getPlannerOverview(String userId) {
        User user = authService.getUserEntity(userId);
        int dailyLimit = user.getDailyGamingLimitMinutes() != null ? user.getDailyGamingLimitMinutes() : 120;
        String prefStart = user.getPreferredGamingStart() != null ? user.getPreferredGamingStart() : "18:00";
        String prefEnd = user.getPreferredGamingEnd() != null ? user.getPreferredGamingEnd() : "21:00";

        LocalDate today = LocalDate.now();
        List<GamingPlan> allPlans = planRepository.findByUserIdOrderByPlanDateAscStartTimeAsc(userId);

        long todayPlannedMinutes = allPlans.stream()
                .filter(p -> today.equals(p.getPlanDate()))
                .mapToLong(GamingPlan::getPlannedDurationMinutes)
                .sum();
        long todayActualMinutes = gamingSessionService.getTodayGamingMinutes(userId);

        List<DailyChartPoint> plannedVsActual = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String label = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            long planned = allPlans.stream()
                    .filter(p -> date.equals(p.getPlanDate()))
                    .mapToLong(GamingPlan::getPlannedDurationMinutes)
                    .sum();
            long actual = gamingSessionService.getGamingMinutesForDate(userId, date);
            plannedVsActual.add(new DailyChartPoint(date.toString(), label, planned, actual));
        }

        GamingPlannerResponse response = new GamingPlannerResponse();
        response.setDailyLimitMinutes(dailyLimit);
        response.setPreferredGamingStart(prefStart);
        response.setPreferredGamingEnd(prefEnd);
        response.setTodayPlannedMinutes(todayPlannedMinutes);
        response.setTodayActualMinutes(todayActualMinutes);
        response.setAvailableMinutesToday(Math.max(0, dailyLimit - todayActualMinutes));
        response.setExceededLimit(todayActualMinutes > dailyLimit);
        response.setPlans(allPlans);
        response.setPlannedVsActualChart(plannedVsActual);
        response.setRecommendations(buildBalancedRecommendations(dailyLimit, prefStart, prefEnd, todayPlannedMinutes, todayActualMinutes));
        return response;
    }

    private List<String> buildBalancedRecommendations(int limit, String prefStart, String prefEnd, long planned, long actual) {
        List<String> list = new ArrayList<>();
        list.add(String.format("Keep gaming inside your preferred window (%s – %s) after wrapping up key study tasks.", prefStart, prefEnd));
        if (actual > limit) {
            list.add(String.format("You have exceeded today's %d-min limit by %d mins. Take a hydration/stretch break and switch to a low-screen study review.", limit, actual - limit));
        } else {
            list.add(String.format("You have %d mins of healthy gaming budget left today out of %d mins.", Math.max(0, limit - actual), limit));
        }
        if (planned > limit) {
            list.add("Today's scheduled gaming exceeds your daily limit. Consider trimming one session or splitting it across the weekend.");
        } else {
            list.add("Pair every 60-minute gaming block with a 45–60 minute focused study block and a 10-minute walk.");
        }
        list.add("End all gaming at least 45 minutes before bedtime so blue light and adrenaline do not delay sleep.");
        return list;
    }
}
