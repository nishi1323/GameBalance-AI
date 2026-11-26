package com.gamebalance.config;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.gamebalance.repository")
public class MongoConfig {

    private static final Logger log = LoggerFactory.getLogger(MongoConfig.class);

    @Bean
    public ApplicationRunner mongoStartupVerifier(MongoTemplate mongoTemplate) {
        return args -> {
            try {
                Document ping = mongoTemplate.executeCommand(new Document("ping", 1));
                log.info("Connected to MongoDB database '{}' (ping ok={})",
                        mongoTemplate.getDb().getName(), ping.get("ok"));
            } catch (Exception ex) {
                log.warn("MongoDB ping check failed on startup: {}", ex.getMessage());
            }
        };
    }
}
