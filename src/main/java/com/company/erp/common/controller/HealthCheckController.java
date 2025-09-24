package com.company.erp.common.controller;

import com.company.erp.common.service.DatabaseOptimizationService;
import com.company.erp.common.service.PerformanceMonitoringService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthCheckController {

    private final PerformanceMonitoringService performanceService;
    private final DatabaseOptimizationService databaseService;

    public HealthCheckController(PerformanceMonitoringService performanceService,
                                 DatabaseOptimizationService databaseService) {
        this.performanceService = performanceService;
        this.databaseService = databaseService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("version", "1.0.0");

        return ResponseEntity.ok(health);
    }

    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealthCheck() {
        Map<String, Object> health = new HashMap<>();

        // Basic info
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());

        // Performance metrics
        health.put("performance", performanceService.getPerformanceMetrics());

        // Database metrics
        health.put("database", databaseService.getDatabaseMetrics());

        // System info
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> system = new HashMap<>();
        system.put("processors", runtime.availableProcessors());
        system.put("freeMemoryMB", runtime.freeMemory() / 1024 / 1024);
        system.put("totalMemoryMB", runtime.totalMemory() / 1024 / 1024);
        system.put("maxMemoryMB", runtime.maxMemory() / 1024 / 1024);
        health.put("system", system);

        return ResponseEntity.ok(health);
    }

    @GetMapping("/metrics/clear")
    public ResponseEntity<Map<String, String>> clearMetrics() {
        performanceService.clearMetrics();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Performance metrics cleared successfully");
        return ResponseEntity.ok(response);
    }
}