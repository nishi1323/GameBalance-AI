package com.gamebalance.repository;

import com.gamebalance.model.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findByUserIdOrderByCreatedAtAsc(String userId);
    void deleteByUserId(String userId);
}
