// AuditLogRepository.java
package com.company.erp.common.repository;

import com.company.erp.common.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByProjectIdOrderByCreatedDateDesc(Long projectId, Pageable pageable);

    Page<AuditLog> findByUserIdOrderByCreatedDateDesc(Long userId, Pageable pageable);

    Page<AuditLog> findByEntityTypeOrderByCreatedDateDesc(String entityType, Pageable pageable);

    Page<AuditLog> findByActionTypeOrderByCreatedDateDesc(String actionType, Pageable pageable);

    Page<AuditLog> findByCategoryOrderByCreatedDateDesc(String category, Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.createdDate BETWEEN :startDate AND :endDate ORDER BY a.createdDate DESC")
    Page<AuditLog> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate,
                                   Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE " +
            "(:projectIds IS NULL OR a.projectId IN :projectIds) AND " +
            "(:actionTypes IS NULL OR a.actionType IN :actionTypes) AND " +
            "(:userIds IS NULL OR a.user.id IN :userIds) AND " +
            "(:startDate IS NULL OR a.createdDate >= :startDate) AND " +
            "(:endDate IS NULL OR a.createdDate <= :endDate) " +
            "ORDER BY a.createdDate DESC")
    Page<AuditLog> findByFilters(@Param("projectIds") List<Long> projectIds,
                                 @Param("actionTypes") List<String> actionTypes,
                                 @Param("userIds") List<Long> userIds,
                                 @Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate,
                                 Pageable pageable);

    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.severity = :severity AND a.createdDate >= :since")
    long countBySeverityAndCreatedDateAfter(@Param("severity") String severity,
                                            @Param("since") LocalDateTime since);

    @Query("SELECT a.actionType, COUNT(a) FROM AuditLog a WHERE a.createdDate >= :since GROUP BY a.actionType")
    List<Object[]> getActionTypeStatistics(@Param("since") LocalDateTime since);

    @Query("SELECT a.category, COUNT(a) FROM AuditLog a WHERE a.createdDate >= :since GROUP BY a.category")
    List<Object[]> getCategoryStatistics(@Param("since") LocalDateTime since);
}