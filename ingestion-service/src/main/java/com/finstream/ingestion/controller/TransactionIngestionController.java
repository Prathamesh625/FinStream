package com.finstream.ingestion.controller;

import com.finstream.common.dto.TransactionEvent;
import com.finstream.common.dto.TransactionRequest;
import com.finstream.ingestion.service.IngestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@CrossOrigin(origins = "*")
public class TransactionIngestionController {

    @Autowired
    private IngestionService ingestionService;

    @PostMapping
    public ResponseEntity<TransactionEvent> submitTransaction(@RequestBody TransactionRequest request) {
        long startTime = System.currentTimeMillis();
        TransactionEvent event = ingestionService.ingestTransaction(request);
        return ResponseEntity.ok(event);
    }
}
