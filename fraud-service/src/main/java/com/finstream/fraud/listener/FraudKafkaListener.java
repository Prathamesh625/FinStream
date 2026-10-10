package com.finstream.fraud.listener;

import com.finstream.common.dto.TransactionEvent;
import com.finstream.fraud.service.FraudDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class FraudKafkaListener {

    @Autowired
    private FraudDetectionService fraudService;

    @KafkaListener(topics = "${finstream.kafka.topics.transactions:finstream.transactions}", groupId = "fraud-detection-group")
    public void listen(TransactionEvent event) {
        if (event != null) {
            fraudService.analyzeTransaction(event);
        }
    }
}
