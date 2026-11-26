package com.gamebalance.repository;

import com.gamebalance.model.SleepRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface SleepRecordRepository extends MongoRepository<SleepRecord, String> {
    List<SleepRecord> findByUserIdOrderByRecordDateDesc(String userId);
    List<SleepRecord> findByUserIdAndRecordDateBetweenOrderByRecordDateAsc(String userId, LocalDate start, LocalDate end);
}
