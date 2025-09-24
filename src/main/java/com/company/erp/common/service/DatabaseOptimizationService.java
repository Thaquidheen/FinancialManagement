package com.company.erp.common.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Service
public class DatabaseOptimizationService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseOptimizationService.class);

    @Autowired
    private DataSource dataSource;

    @Scheduled(cron = "0 0 2 * * SUN") // Every Sunday at 2 AM
    public void performWeeklyMaintenance() {
        logger.info("Starting weekly database maintenance");

        analyzeAndOptimizeTables();
        updateTableStatistics();
        cleanupOldAuditLogs();

        logger.info("Weekly database maintenance completed");
    }

    private void analyzeAndOptimizeTables() {
        String[] tables = {
                "projects", "quotations", "quotation_items", "documents",
                "budget_tracking", "notifications", "audit_logs", "users"
        };

        try (Connection conn = dataSource.getConnection()) {
            for (String table : tables) {
                // Analyze table
                try (PreparedStatement stmt = conn.prepareStatement("ANALYZE " + table)) {
                    stmt.execute();
                    logger.debug("Analyzed table: {}", table);
                } catch (SQLException e) {
                    logger.warn("Failed to analyze table {}: {}", table, e.getMessage());
                }
            }
        } catch (SQLException e) {
            logger.error("Database maintenance failed: {}", e.getMessage(), e);
        }
    }

    private void updateTableStatistics() {
        try (Connection conn = dataSource.getConnection()) {
            // Update statistics for query optimization
            try (PreparedStatement stmt = conn.prepareStatement("UPDATE pg_stat_user_tables SET n_tup_ins = n_tup_ins")) {
                stmt.execute();
                logger.debug("Updated table statistics");
            }
        } catch (SQLException e) {
            logger.error("Failed to update table statistics: {}", e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 1 * * ?") // Daily at 1 AM
    public void cleanupOldAuditLogs() {
        try (Connection conn = dataSource.getConnection()) {
            // Delete audit logs older than 2 years
            String sql = "DELETE FROM audit_logs WHERE created_date < NOW() - INTERVAL '2 years'";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int deleted = stmt.executeUpdate();
                if (deleted > 0) {
                    logger.info("Cleaned up {} old audit log entries", deleted);
                }
            }

            // Delete old notification logs (older than 6 months)
            sql = "DELETE FROM notifications WHERE created_date < NOW() - INTERVAL '6 months' AND read = true";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int deleted = stmt.executeUpdate();
                if (deleted > 0) {
                    logger.info("Cleaned up {} old notification entries", deleted);
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to cleanup old records: {}", e.getMessage(), e);
        }
    }

    public Map<String, Object> getDatabaseMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        try (Connection conn = dataSource.getConnection()) {
            // Table sizes
            String sql = "SELECT schemaname, tablename, pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size " +
                    "FROM pg_tables WHERE schemaname = 'public' ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC LIMIT 10";

            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                Map<String, String> tableSizes = new HashMap<>();
                while (rs.next()) {
                    tableSizes.put(rs.getString("tablename"), rs.getString("size"));
                }
                metrics.put("tableSizes", tableSizes);
            }

            // Active connections
            sql = "SELECT count(*) as active_connections FROM pg_stat_activity WHERE state = 'active'";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    metrics.put("activeConnections", rs.getInt("active_connections"));
                }
            }

            // Slow queries (if pg_stat_statements is enabled)
            sql = "SELECT query, calls, mean_time, total_time " +
                    "FROM pg_stat_statements " +
                    "WHERE mean_time > 1000 " +
                    "ORDER BY mean_time DESC LIMIT 5";

            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                Map<String, Object> slowQueries = new HashMap<>();
                int count = 0;
                while (rs.next() && count < 5) {
                    Map<String, Object> queryInfo = new HashMap<>();
                    queryInfo.put("calls", rs.getLong("calls"));
                    queryInfo.put("meanTime", rs.getDouble("mean_time"));
                    queryInfo.put("totalTime", rs.getDouble("total_time"));

                    slowQueries.put("query_" + count, queryInfo);
                    count++;
                }
                metrics.put("slowQueries", slowQueries);

            } catch (SQLException e) {
                // pg_stat_statements might not be enabled
                logger.debug("pg_stat_statements not available: {}", e.getMessage());
            }

        } catch (SQLException e) {
            logger.error("Failed to get database metrics: {}", e.getMessage(), e);
        }

        return metrics;
    }
}