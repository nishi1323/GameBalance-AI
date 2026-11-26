package com.gamebalance.repository;

import com.gamebalance.model.GamingSession;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GamingSessionRepository extends MongoRepository<GamingSession, String> {
    Optional<GamingSession> findFirstByUserIdAndActiveTrue(String userId);
    List<GamingSession> findByUserIdOrderByStartTimeDesc(String userId);
    List<GamingSession> findByUserIdAndSessionDateBetweenOrderByStartTimeDesc(String userId, LocalDate start, LocalDate end);
    List<GamingSession> findByUserIdAndSessionDateOrderByStartTimeDesc(String userId, LocalDate date);
}
