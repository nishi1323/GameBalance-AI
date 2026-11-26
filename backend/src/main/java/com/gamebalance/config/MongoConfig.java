package com.gamebalance.config;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableMongoRepositories(basePackages = "com.gamebalance.repository")
public class MongoConfig {

    private static final Logger log = LoggerFactory.getLogger(MongoConfig.class);

    @Bean
    public MongoClientSettingsBuilderCustomizer mongoClientSettingsCustomizer() {
        // Keep Spring Boot's default secure TLS (cert + hostname verification enabled for mongodb+srv://)
        // and apply resilient cross-region connection/handshake timeouts (e.g. Render Oregon -> Atlas Mumbai).
        return builder -> builder
                .applyToSslSettings(ssl -> ssl.invalidHostNameAllowed(false))
                .applyToSocketSettings(socket -> socket
                        .connectTimeout(15, TimeUnit.SECONDS)
                        .readTimeout(20, TimeUnit.SECONDS))
                .applyToClusterSettings(cluster -> cluster
                        .serverSelectionTimeout(20, TimeUnit.SECONDS));
    }

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
