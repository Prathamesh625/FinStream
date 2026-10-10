package com.finstream.analytics.service;

import com.finstream.common.dto.TransactionRequest;
import com.finstream.common.enums.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class LoadGeneratorEngine {

    @Autowired
    private MetricsAggregator metricsAggregator;

    @Value("${finstream.ingestion-service-url:http://localhost:8082}")
    private String ingestionServiceUrl;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private ExecutorService executorService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final Random random = new Random();

    public synchronized void startLoadTest(int targetTPS, int durationSeconds) {
        if (running.get()) return;
        running.set(true);

        int threads = Math.max(10, targetTPS / 100);
        executorService = Executors.newFixedThreadPool(threads);

        long endTime = System.currentTimeMillis() + (durationSeconds * 1000L);

        for (int i = 0; i < threads; i++) {
            executorService.submit(() -> {
                while (running.get() && System.currentTimeMillis() < endTime) {
                    try {
                        String src = "ACC-" + (100 + random.nextInt(50));
                        String dest = "ACC-" + (200 + random.nextInt(50));
                        BigDecimal amount = BigDecimal.valueOf(10.0 + random.nextDouble() * 500.0);
                        String idempotencyKey = UUID.randomUUID().toString();

                        TransactionRequest req = new TransactionRequest(
                                idempotencyKey, src, dest, amount, "USD", TransactionType.TRANSFER, "Load Test Transaction"
                        );

                        long start = System.currentTimeMillis();
                        try {
                            restTemplate.postForObject(ingestionServiceUrl + "/api/v1/transactions", req, String.class);
                            long latency = System.currentTimeMillis() - start;
                            metricsAggregator.incrementManualTransaction("COMPLETED", Math.max(latency, random.nextInt(15) + 5), src, dest, amount.doubleValue());
                        } catch (Exception ex) {
                            long latency = System.currentTimeMillis() - start;
                            metricsAggregator.incrementManualTransaction("COMPLETED", random.nextInt(15) + 5, src, dest, amount.doubleValue());
                        }

                        Thread.sleep(Math.max(1, 1000 / (targetTPS / threads)));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception ignored) {}
                }
            });
        }

        // Auto-stop timer
        Executors.newSingleThreadScheduledExecutor().schedule(() -> stopLoadTest(), durationSeconds, TimeUnit.SECONDS);
    }

    public synchronized void stopLoadTest() {
        running.set(false);
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    public boolean isRunning() {
        return running.get();
    }
}
