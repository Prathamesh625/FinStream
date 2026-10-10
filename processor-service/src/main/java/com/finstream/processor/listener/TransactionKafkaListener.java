package com.finstream.processor.listener;

import com.finstream.common.dto.TransactionEvent;
import com.finstream.processor.service.TransactionProcessorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionKafkaListener {

    @Autowired
    private TransactionProcessorService processorService;

    @KafkaListener(topics = "${finstream.kafka.topics.transactions:finstream.transactions}", groupId = "transaction-processor-group")
    public void listen(TransactionEvent event) {
        if (event != null) {
            processorService.processTransaction(event);
        }
    }
}
