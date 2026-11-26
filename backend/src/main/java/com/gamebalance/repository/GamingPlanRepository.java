package com.gamebalance.repository;

import com.gamebalance.model.GamingPlan;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface GamingPlanRepository extends MongoRepository<GamingPlan, String> {
    List<GamingPlan> findByUserIdOrderByPlanDateAscStartTimeAsc(String userId);
    List<GamingPlan> findByUserIdAndPlanDateOrderByStartTimeAsc(String userId, LocalDate planDate);
    List<GamingPlan> findByUserIdAndPlanDateBetweenOrderByPlanDateAscStartTimeAsc(String userId, LocalDate start, LocalDate end);
}
