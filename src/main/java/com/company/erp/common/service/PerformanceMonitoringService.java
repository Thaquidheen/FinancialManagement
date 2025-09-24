package com.company.erp.common.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class PerformanceMonitoringService {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitoringService.class);

    private final Map<String, AtomicLong> operationCounters = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> operationDurations = new ConcurrentHashMap<>();
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

    public void recordOperation(String operationName, long durationMs) {
        operationCounters.computeIfAbsent(operationName, k -> new AtomicLong(0)).incrementAndGet();
        operationDurations.computeIfAbsent(operationName, k -> new AtomicLong(0)).addAndGet(durationMs);

        // Log slow operations
        if (durationMs > 5000) { // 5 seconds
            logger.warn("Slow operation detected: {} took {}ms", operationName, durationMs);
        }
    }

    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        // JVM Metrics
        metrics.put("memory", getMemoryMetrics());
        metrics.put("threads", getThreadMetrics());
        metrics.put("operations", getOperationMetrics());

        return metrics;
    }

    private Map<String, Object> getMemoryMetrics() {
        Map<String, Object> memory = new HashMap<>();

        long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
        long heapMax = memoryBean.getHeapMemoryUsage().getMax();
        long nonHeapUsed = memoryBean.getNonHeapMemoryUsage().getUsed();

        memory.put("heapUsedMB", heapUsed / 1024 / 1024);
        memory.put("heapMaxMB", heapMax / 1024 / 1024);
        memory.put("heapUtilizationPercent", (heapUsed * 100.0) / heapMax);
        memory.put("nonHeapUsedMB", nonHeapUsed / 1024 / 1024);

        return memory;
    }

    private Map<String, Object> getThreadMetrics() {
        Map<String, Object> threads = new HashMap<>();

        threads.put("threadCount", threadBean.getThreadCount());
        threads.put("peakThreadCount", threadBean.getPeakThreadCount());
        threads.put("daemonThreadCount", threadBean.getDaemonThreadCount());

        return threads;
    }

    private Map<String, Object> getOperationMetrics() {
        Map<String, Object> operations = new HashMap<>();

        for (Map.Entry<String, AtomicLong> entry : operationCounters.entrySet()) {
            String operation = entry.getKey();
            long count = entry.getValue().get();
            long totalDuration = operationDurations.get(operation).get();

            Map<String, Object> operationStats = new HashMap<>();
            operationStats.put("count", count);
            operationStats.put("totalDurationMs", totalDuration);
            operationStats.put("averageDurationMs", count > 0 ? totalDuration / count : 0);

            operations.put(operation, operationStats);
        }

        return operations;
    }

    public void clearMetrics() {
        operationCounters.clear();
        operationDurations.clear();
        logger.info("Performance metrics cleared");
    }
}