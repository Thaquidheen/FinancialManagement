package com.company.erp.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for date and time operations
 */
public class DateUtil {

    public static final DateTimeFormatter DEFAULT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DEFAULT_DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    public static final DateTimeFormatter DISPLAY_DATETIME_FORMAT = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    private DateUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Get the start of the current month
     */
    public static LocalDate getStartOfCurrentMonth() {
        return LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
    }

    /**
     * Get the end of the current month
     */
    public static LocalDate getEndOfCurrentMonth() {
        return LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * Get the start of the current quarter
     */
    public static LocalDate getStartOfCurrentQuarter() {
        LocalDate now = LocalDate.now();
        int currentQuarter = (now.getMonthValue() - 1) / 3 + 1;
        int startMonth = (currentQuarter - 1) * 3 + 1;
        return LocalDate.of(now.getYear(), startMonth, 1);
    }

    /**
     * Get the end of the current quarter
     */
    public static LocalDate getEndOfCurrentQuarter() {
        LocalDate startOfQuarter = getStartOfCurrentQuarter();
        return startOfQuarter.plusMonths(3).minusDays(1);
    }

    /**
     * Get the start of the current year
     */
    public static LocalDate getStartOfCurrentYear() {
        return LocalDate.now().with(TemporalAdjusters.firstDayOfYear());
    }

    /**
     * Get the end of the current year
     */
    public static LocalDate getEndOfCurrentYear() {
        return LocalDate.now().with(TemporalAdjusters.lastDayOfYear());
    }

    /**
     * Get date range for the last N days
     */
    public static List<LocalDate> getDateRangeForLastNDays(int days) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            dates.add(current);
            current = current.plusDays(1);
        }

        return dates;
    }

    /**
     * Get date range between two dates
     */
    public static List<LocalDate> getDateRange(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            dates.add(current);
            current = current.plusDays(1);
        }

        return dates;
    }

    /**
     * Get month range between two dates
     */
    public static List<YearMonth> getMonthRange(LocalDate startDate, LocalDate endDate) {
        List<YearMonth> months = new ArrayList<>();
        YearMonth start = YearMonth.from(startDate);
        YearMonth end = YearMonth.from(endDate);

        YearMonth current = start;
        while (!current.isAfter(end)) {
            months.add(current);
            current = current.plusMonths(1);
        }

        return months;
    }

    /**
     * Calculate the number of business days between two dates
     */
    public static long getBusinessDaysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            return 0;
        }

        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        long weekends = 0;

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            if (current.getDayOfWeek().getValue() >= 6) { // Saturday = 6, Sunday = 7
                weekends++;
            }
            current = current.plusDays(1);
        }

        return totalDays - weekends;
    }

    /**
     * Check if a date is within a range
     */
    public static boolean isDateInRange(LocalDate date, LocalDate startDate, LocalDate endDate) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Get the quarter number for a given date
     */
    public static int getQuarter(LocalDate date) {
        return (date.getMonthValue() - 1) / 3 + 1;
    }

    /**
     * Get the fiscal year for a given date (assuming fiscal year starts in April)
     */
    public static int getFiscalYear(LocalDate date) {
        return date.getMonthValue() >= 4 ? date.getYear() : date.getYear() - 1;
    }

    /**
     * Format date for display
     */
    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DISPLAY_DATE_FORMAT) : "";
    }

    /**
     * Format datetime for display
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DISPLAY_DATETIME_FORMAT) : "";
    }

    /**
     * Parse date from string
     */
    public static LocalDate parseDate(String dateString) {
        return LocalDate.parse(dateString, DEFAULT_DATE_FORMAT);
    }

    /**
     * Parse datetime from string
     */
    public static LocalDateTime parseDateTime(String dateTimeString) {
        return LocalDateTime.parse(dateTimeString, DEFAULT_DATETIME_FORMAT);
    }

    /**
     * Get age in years from a birth date
     */
    public static int getAge(LocalDate birthDate) {
        return (int) ChronoUnit.YEARS.between(birthDate, LocalDate.now());
    }

    /**
     * Check if a year is a leap year
     */
    public static boolean isLeapYear(int year) {
        return ((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0);
    }

    /**
     * Get the number of days in a given month and year
     */
    public static int getDaysInMonth(int year, int month) {
        return YearMonth.of(year, month).lengthOfMonth();
    }

    /**
     * Get the last day of a given month and year
     */
    public static LocalDate getLastDayOfMonth(int year, int month) {
        return YearMonth.of(year, month).atEndOfMonth();
    }

    /**
     * Get the first day of a given month and year
     */
    public static LocalDate getFirstDayOfMonth(int year, int month) {
        return LocalDate.of(year, month, 1);
    }

    /**
     * Add business days to a date (excluding weekends)
     */
    public static LocalDate addBusinessDays(LocalDate date, int businessDays) {
        LocalDate result = date;
        int daysAdded = 0;

        while (daysAdded < businessDays) {
            result = result.plusDays(1);
            if (result.getDayOfWeek().getValue() < 6) { // Monday = 1, Friday = 5
                daysAdded++;
            }
        }

        return result;
    }

    /**
     * Check if a date falls on a weekend
     */
    public static boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek().getValue() >= 6; // Saturday = 6, Sunday = 7
    }

    /**
     * Check if a date falls on a weekday
     */
    public static boolean isWeekday(LocalDate date) {
        return !isWeekend(date);
    }

    /**
     * Get the start date of a week for a given date
     */
    public static LocalDate getStartOfWeek(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
    }

    /**
     * Get the end date of a week for a given date
     */
    public static LocalDate getEndOfWeek(LocalDate date) {
        return date.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));
    }
}
