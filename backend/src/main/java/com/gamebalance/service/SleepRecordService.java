package com.gamebalance.service;

import com.gamebalance.dto.DailyChartPoint;
import com.gamebalance.dto.SleepRecordRequest;
import com.gamebalance.dto.SleepSummaryResponse;
import com.gamebalance.exception.BadRequestException;
import com.gamebalance.exception.ResourceNotFoundException;
import com.gamebalance.model.GamingSession;
import com.gamebalance.model.SleepRecord;
import com.gamebalance.repository.GamingSessionRepository;
import com.gamebalance.repository.SleepRecordRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SleepRecordService {

    private final SleepRecordRepository sleepRepository;
    private final GamingSessionRepository gamingRepository;
    private final GroqService groqService;

    public SleepRecordService(SleepRecordRepository sleepRepository,
                              GamingSessionRepository gamingRepository,
                              GroqService groqService) {
        this.sleepRepository = sleepRepository;
        this.gamingRepository = gamingRepository;
        this.groqService = groqService;
    }

    public SleepRecord createRecord(String userId, SleepRecordRequest request) {
        if (!request.getWakeUpTime().isAfter(request.getSleepTime())) {
            throw new BadRequestException("Wake-up time must be after sleep time.");
        }

        double hours = Duration.between(request.getSleepTime(), request.getWakeUpTime()).toMinutes() / 60.0;
        if (hours > 24.0) {
            throw new BadRequestException("Sleep duration cannot exceed 24 hours.");
        }
        double roundedHours = round2(hours);

        LocalDate recordDate = request.getWakeUpTime().toLocalDate();
        boolean lateNightGaming = request.getHadLateNightGaming() != null
                ? request.getHadLateNightGaming()
                : detectLateNightGamingBeforeSleep(userId, request.getSleepTime().toLocalDate());

        int sleepHour = request.getSleepTime().getHour();
        boolean irregular = roundedHours < 6.5 || roundedHours > 10.5 || (sleepHour >= 1 && sleepHour < 6);

        SleepRecord record = new SleepRecord();
        record.setUserId(userId);
        record.setRecordDate(recordDate);
        record.setSleepTime(request.getSleepTime());
        record.setWakeUpTime(request.getWakeUpTime());
        record.setTotalDurationHours(roundedHours);
        record.setQualityRating(request.getQualityRating());
        record.setHadLateNightGaming(lateNightGaming);
        record.setIrregularPattern(irregular);
        record.setNotes(request.getNotes() != null ? request.getNotes().trim() : "");
        record.setCreatedAt(Instant.now());

        return sleepRepository.save(record);
    }

    public void deleteRecord(String userId, String recordId) {
        SleepRecord record = sleepRepository.findById(recordId)
                .filter(r -> r.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Sleep record not found."));
        sleepRepository.delete(record);
    }

    public SleepSummaryResponse getSummary(String userId) {
        List<SleepRecord> records = sleepRepository.findByUserIdOrderByRecordDateDesc(userId);
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);

        double avgSleep = records.isEmpty() ? 0.0 : round2(records.stream()
                .mapToDouble(SleepRecord::getTotalDurationHours)
                .average()
                .orElse(0.0));
        double latestSleep = records.isEmpty() ? 0.0 : records.get(0).getTotalDurationHours();
        long irregularCount = records.stream().filter(SleepRecord::isIrregularPattern).count();

        double lateNightGamingAvg = round2(records.stream()
                .filter(SleepRecord::isHadLateNightGaming)
                .mapToDouble(SleepRecord::getTotalDurationHours)
                .average()
                .orElse(0.0));

        double regularAvg = round2(records.stream()
                .filter(r -> !r.isHadLateNightGaming())
                .mapToDouble(SleepRecord::getTotalDurationHours)
                .average()
                .orElse(0.0));

        Map<LocalDate, Double> byDate = records.stream()
                .filter(r -> r.getRecordDate() != null && !r.getRecordDate().isBefore(weekStart) && !r.getRecordDate().isAfter(today))
                .collect(Collectors.toMap(
                        SleepRecord::getRecordDate,
                        SleepRecord::getTotalDurationHours,
                        (a, b) -> a
                ));

        List<DailyChartPoint> weeklyChart = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            String label = d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            weeklyChart.add(new DailyChartPoint(d.toString(), label, round2(byDate.getOrDefault(d, 0.0)), 8.0));
        }

        String aiSuggestion = buildAiSleepSuggestion(records, avgSleep, irregularCount, lateNightGamingAvg, regularAvg);

        SleepSummaryResponse response = new SleepSummaryResponse();
        response.setRecords(records);
        response.setAvgSleepHours(avgSleep);
        response.setLatestSleepHours(latestSleep);
        response.setIrregularNightsCount(irregularCount);
        response.setLateNightGamingSleepAvg(lateNightGamingAvg);
        response.setRegularSleepAvg(regularAvg);
        response.setWeeklyChart(weeklyChart);
        response.setAiSuggestion(aiSuggestion);
        response.setDailyRecommendations(List.of(
                "Maintain a consistent sleep and wake window, even on weekends.",
                "Take a 5–10 minute screen & stretch break between gaming sessions.",
                "Include at least 30 minutes of daily physical activity.",
                "Avoid competitive or intense gaming within 45–60 minutes of bedtime.",
                "Pair evening gaming rewards with completed afternoon study goals."
        ));
        return response;
    }

    private boolean detectLateNightGamingBeforeSleep(String userId, LocalDate sleepDate) {
        List<GamingSession> sessions = gamingRepository.findByUserIdAndSessionDateBetweenOrderByStartTimeDesc(
                userId, sleepDate.minusDays(1), sleepDate);
        return sessions.stream().anyMatch(GamingSession::isLateNight);
    }

    private String buildAiSleepSuggestion(List<SleepRecord> records, double avgSleep, long irregularCount,
                                          double lateNightGamingAvg, double regularAvg) {
        if (records.isEmpty()) {
            return "Log your sleep and wake-up times for a few nights to unlock personalized AI insights on how your gaming schedule affects your rest.";
        }

        String prompt = String.format(
                "Student sleep metrics: totalLogs=%d, avgSleepHours=%.2f, irregularNights=%d, " +
                "avgSleepAfterLateNightGaming=%.2f, avgSleepWithoutLateNightGaming=%.2f. " +
                "Give a concise, encouraging 2-3 sentence sleep improvement suggestion without any medical diagnosis.",
                records.size(), avgSleep, irregularCount, lateNightGamingAvg, regularAvg
        );

        return groqService.generateSinglePrompt(
                "You are a friendly student sleep and wellness coach. Keep responses practical, non-clinical, and concise (2-3 sentences).",
                prompt,
                0.5,
                180
        ).orElseGet(() -> {
            if (lateNightGamingAvg > 0 && regularAvg > 0 && lateNightGamingAvg + 0.5 < regularAvg) {
                return String.format(
                        "On nights with late-night gaming, your sleep averages %.1f hours compared to %.1f hours otherwise. " +
                        "Setting a firm gaming curfew 45 minutes before bed can restore over %.1f hours of recovery sleep.",
                        lateNightGamingAvg, regularAvg, regularAvg - lateNightGamingAvg
                );
            }
            if (avgSleep < 7.0) {
                return String.format(
                        "Your average sleep is %.1f hours, slightly below the 7–9 hour college target. " +
                        "Try shifting your last gaming session earlier by 30 minutes and dimming screens before sleep.",
                        avgSleep
                );
            }
            return String.format(
                    "Great consistency! You are averaging %.1f hours of sleep across %d logged night(s). " +
                    "Keep protecting your wind-down routine and taking short breaks between study and gaming.",
                    avgSleep, records.size()
            );
        });
    }

    private double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
