package com.finstream.fraud.service;

import com.finstream.common.dto.FraudAlert;
import com.finstream.common.dto.TransactionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class FraudDetectionService {

    @Autowired(required = false)
    private KafkaTemplate<String, FraudAlert> alertKafkaTemplate;

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    @Value("${finstream.kafka.topics.alerts:finstream.fraud-alerts}")
    private String alertsTopic;

    public void analyzeTransaction(TransactionEvent event) {
        if (event == null || event.getSourceAccountId() == null) return;

        String accountId = event.getSourceAccountId();
        String velocityKey = "velocity:" + accountId;

        long currentVelocity = 1;
        if (redisTemplate != null) {
            try {
                Long incremented = redisTemplate.opsForValue().increment(velocityKey);
                if (incremented != null) {
                    currentVelocity = incremented;
                    if (incremented == 1) {
                        redisTemplate.expire(velocityKey, Duration.ofSeconds(5));
                    }
                }
            } catch (Exception ignored) {}
        }

        // Velocity Rule Check (> 10 transactions in 5s)
        if (currentVelocity > 10) {
            triggerAlert(event, "VELOCITY_SPIKE", "CRITICAL", "High transaction velocity detected (" + currentVelocity + " txns / 5s)");
        }

        // High Amount Rule Check ($25,000+)
        if (event.getAmount() != null && event.getAmount().doubleValue() >= 25000.0) {
            triggerAlert(event, "HIGH_VALUE_TRANSFER", "WARNING", "Large value transfer threshold exceeded ($" + event.getAmount() + ")");
        }
    }

    private void triggerAlert(TransactionEvent event, String rule, String severity, String message) {
        FraudAlert alert = new FraudAlert(
                "ALERT-" + UUID.randomUUID().toString().substring(0, 8),
                event.getTransactionId(),
                event.getSourceAccountId(),
                event.getAmount(),
                rule + ": " + message,
                severity,
                Instant.now()
        );

        if (alertKafkaTemplate != null) {
            try {
                alertKafkaTemplate.send(alertsTopic, alert.getAccountId(), alert);
            } catch (Exception ignored) {}
        }
    }
}
