package com.finstream.ingestion.service;

import com.finstream.common.dto.TransactionEvent;
import com.finstream.common.dto.TransactionRequest;
import com.finstream.common.enums.TransactionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class IngestionService {

    @Autowired(required = false)
    private KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    @Value("${finstream.kafka.topics.transactions:finstream.transactions}")
    private String transactionsTopic;

    public TransactionEvent ingestTransaction(TransactionRequest request) {
        String idempotencyKey = request.getIdempotencyKey();

        // 1. Idempotency validation using Redis
        if (idempotencyKey != null && redisTemplate != null) {
            Boolean setIfAbsent = redisTemplate.opsForValue().setIfAbsent("idempotency:" + idempotencyKey, "PROCESSING", Duration.ofMinutes(15));
            if (Boolean.FALSE.equals(setIfAbsent)) {
                TransactionEvent duplicateEvent = new TransactionEvent(
                        "TXN-DUP-" + UUID.randomUUID().toString().substring(0, 8),
                        idempotencyKey,
                        request.getSourceAccountId(),
                        request.getDestinationAccountId(),
                        request.getAmount(),
                        request.getCurrency() != null ? request.getCurrency() : "USD",
                        request.getType(),
                        TransactionStatus.REJECTED_DUPLICATE,
                        "Duplicate Idempotency Key",
                        Instant.now()
                );
                return duplicateEvent;
            }
        }

        // 2. Build TransactionEvent
        String txnId = "TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6);
        TransactionEvent event = new TransactionEvent(
                txnId,
                idempotencyKey,
                request.getSourceAccountId(),
                request.getDestinationAccountId(),
                request.getAmount(),
                request.getCurrency() != null ? request.getCurrency() : "USD",
                request.getType(),
                TransactionStatus.PENDING,
                request.getReferenceNote(),
                Instant.now()
        );

        // 3. Publish to Kafka
        if (kafkaTemplate != null) {
            try {
                kafkaTemplate.send(transactionsTopic, event.getSourceAccountId(), event);
            } catch (Exception e) {
                // System.err.println("Failed to publish to Kafka: " + e.getMessage());
            }
        }

        return event;
    }
}
