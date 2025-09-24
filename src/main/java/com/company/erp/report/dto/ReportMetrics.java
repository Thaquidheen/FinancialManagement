package com.company.erp.report.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReportMetrics {
    private String metricName;
    private BigDecimal currentValue;
    private BigDecimal previousValue;
    private BigDecimal changePercentage;
    private String trend; // UP, DOWN, STABLE
    private String unit;
    private LocalDateTime calculatedAt;

    // Constructors
    public ReportMetrics() {}

    public ReportMetrics(String metricName, BigDecimal currentValue) {
        this.metricName = metricName;
        this.currentValue = currentValue;
        this.calculatedAt = LocalDateTime.now();
    }

    // Business methods
    public void calculateTrend() {
        if (previousValue != null && currentValue != null) {
            if (currentValue.compareTo(previousValue) > 0) {
                this.trend = "UP";
                this.changePercentage = currentValue.subtract(previousValue)
                        .divide(previousValue, 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(new BigDecimal("100"));
            } else if (currentValue.compareTo(previousValue) < 0) {
                this.trend = "DOWN";
                this.changePercentage = previousValue.subtract(currentValue)
                        .divide(previousValue, 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(new BigDecimal("100")).negate();
            } else {
                this.trend = "STABLE";
                this.changePercentage = BigDecimal.ZERO;
            }
        }
    }

    // Getters and Setters
    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }

    public BigDecimal getCurrentValue() { return currentValue; }
    public void setCurrentValue(BigDecimal currentValue) { this.currentValue = currentValue; }

    public BigDecimal getPreviousValue() { return previousValue; }
    public void setPreviousValue(BigDecimal previousValue) { this.previousValue = previousValue; }

    public BigDecimal getChangePercentage() { return changePercentage; }
    public void setChangePercentage(BigDecimal changePercentage) { this.changePercentage = changePercentage; }

    public String getTrend() { return trend; }
    public void setTrend(String trend) { this.trend = trend; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public LocalDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; }

    @Override
    public String toString() {
        return "ReportMetrics{" +
                "metricName='" + metricName + '\'' +
                ", currentValue=" + currentValue +
                ", trend='" + trend + '\'' +
                ", changePercentage=" + changePercentage +
                '}';
    }
}