package com.gamebalance.repository;

import com.gamebalance.model.StudyTask;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface StudyTaskRepository extends MongoRepository<StudyTask, String> {
    List<StudyTask> findByUserIdOrderByDueDateTimeAsc(String userId);
    List<StudyTask> findByUserIdAndCompletedOrderByDueDateTimeAsc(String userId, boolean completed);
}
