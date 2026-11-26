package com.gamebalance.repository;

import com.gamebalance.model.RiskAssessment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RiskAssessmentRepository extends MongoRepository<RiskAssessment, String> {
    List<RiskAssessment> findByUserIdOrderByCreatedAtDesc(String userId);
    Optional<RiskAssessment> findFirstByUserIdOrderByCreatedAtDesc(String userId);
}
