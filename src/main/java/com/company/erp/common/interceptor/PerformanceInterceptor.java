package com.company.erp.common.interceptor;

import com.company.erp.common.service.PerformanceMonitoringService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PerformanceInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceInterceptor.class);

    private final PerformanceMonitoringService performanceMonitoringService;

    public PerformanceInterceptor(PerformanceMonitoringService performanceMonitoringService) {
        this.performanceMonitoringService = performanceMonitoringService;
    }

    @Around("@annotation(com.company.erp.common.annotation.Monitored) || " +
            "execution(* com.company.erp.*.service.*.*(..))")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String operationName = joinPoint.getSignature().getDeclaringType().getSimpleName() +
                "." + joinPoint.getSignature().getName();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            performanceMonitoringService.recordOperation(operationName, duration);

            if (duration > 1000) { // Log operations taking more than 1 second
                logger.info("Operation {} completed in {}ms", operationName, duration);
            }

            return result;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            performanceMonitoringService.recordOperation(operationName + ".ERROR", duration);

            logger.error("Operation {} failed after {}ms: {}", operationName, duration, e.getMessage());
            throw e;
        }
    }
}
