package com.finstream.analytics.controller;

import com.finstream.analytics.service.LoadGeneratorEngine;
import com.finstream.analytics.service.MetricsAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    @Autowired
    private MetricsAggregator metricsAggregator;

    @Autowired
    private LoadGeneratorEngine loadGeneratorEngine;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        Map<String, Object> metrics = metricsAggregator.getMetricsSummary();
        metrics.put("loadTestActive", loadGeneratorEngine.isRunning());
        return ResponseEntity.ok(metrics);
    }

    @PostMapping("/load-test/start")
    public ResponseEntity<Map<String, Object>> startLoadTest(@RequestBody Map<String, Object> params) {
        int tps = Integer.parseInt(params.getOrDefault("tps", 1200).toString());
        int duration = Integer.parseInt(params.getOrDefault("duration", 30).toString());

        loadGeneratorEngine.startLoadTest(tps, duration);
        return ResponseEntity.ok(Map.of(
                "status", "STARTED",
                "targetTPS", tps,
                "durationSeconds", duration
        ));
    }

    @PostMapping("/load-test/stop")
    public ResponseEntity<Map<String, String>> stopLoadTest() {
        loadGeneratorEngine.stopLoadTest();
        return ResponseEntity.ok(Map.of("status", "STOPPED"));
    }
}
