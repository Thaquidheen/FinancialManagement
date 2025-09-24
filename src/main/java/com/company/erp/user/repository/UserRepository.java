package com.company.erp.user.repository;

import com.company.erp.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Basic finder methods
    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameAndActiveTrue(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndActiveTrue(String email);

    Optional<User> findByIdAndActiveTrue(Long id);

    List<User> findByActiveTrue();

    // Check existence methods
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsernameAndIdNot(String username, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);

    // Find by role
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.active = true")
    List<User> findByRoleName(@Param("roleName") String roleName);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.active = true")
    Page<User> findByRoleName(@Param("roleName") String roleName, Pageable pageable);

    // Find users by multiple criteria
    @Query("SELECT u FROM User u WHERE " +
            "(:fullName IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :fullName, '%'))) AND " +
            "(:department IS NULL OR LOWER(u.department) = LOWER(:department)) AND " +
            "(:active IS NULL OR u.active = :active)")
    Page<User> findBySearchCriteria(@Param("fullName") String fullName,
                                    @Param("department") String department,
                                    @Param("active") Boolean active,
                                    Pageable pageable);

    // Find project managers
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = 'PROJECT_MANAGER' AND u.active = true")
    List<User> findActiveProjectManagers();

    // Find account managers
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = 'ACCOUNT_MANAGER' AND u.active = true")
    List<User> findActiveAccountManagers();

    // Find users by manager
    List<User> findByManagerAndActiveTrue(User manager);

    // Find users by department
    List<User> findByDepartmentAndActiveTrue(String department);

    // Account security methods
    @Query("SELECT u FROM User u WHERE u.accountLocked = true AND u.active = true")
    List<User> findLockedAccounts();

    @Query("SELECT u FROM User u WHERE u.passwordExpired = true AND u.active = true")
    List<User> findUsersWithExpiredPasswords();

    @Query("SELECT u FROM User u WHERE u.lastLoginDate IS NULL AND u.active = true")
    List<User> findUsersWhoNeverLoggedIn();

    @Query("SELECT u FROM User u WHERE u.lastLoginDate < :cutoffDate AND u.active = true")
    List<User> findInactiveUsers(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Update methods
    @Modifying
    @Query("UPDATE User u SET u.lastLoginDate = :loginDate, u.failedLoginAttempts = 0 WHERE u.id = :userId")
    void updateLastLoginDate(@Param("userId") Long userId, @Param("loginDate") LocalDateTime loginDate);

    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = u.failedLoginAttempts + 1 WHERE u.id = :userId")
    void incrementFailedLoginAttempts(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE User u SET u.accountLocked = :locked WHERE u.id = :userId")
    void updateAccountLockStatus(@Param("userId") Long userId, @Param("locked") Boolean locked);

    @Modifying
    @Query("UPDATE User u SET u.passwordHash = :newPassword, u.passwordExpired = false WHERE u.id = :userId")
    void updatePassword(@Param("userId") Long userId, @Param("newPassword") String newPassword);

    @Modifying
    @Query("UPDATE User u SET u.active = false WHERE u.id = :userId")
    void deactivateUser(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE User u SET u.active = true WHERE u.id = :userId")
    void activateUser(@Param("userId") Long userId);

    // Statistics and counts
    @Query("SELECT COUNT(u) FROM User u WHERE u.active = true")
    long countActiveUsers();

    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.active = true")
    long countUsersByRole(@Param("roleName") String roleName);

    @Query("SELECT u.department, COUNT(u) FROM User u WHERE u.active = true AND u.department IS NOT NULL GROUP BY u.department")
    List<Object[]> countUsersByDepartment();

    // Complex queries for user management
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles LEFT JOIN FETCH u.bankDetails WHERE u.id = :id")
    Optional<User> findByIdWithRolesAndBankDetails(@Param("id") Long id);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :username AND u.active = true")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles WHERE u.active = true")
    List<User> findAllActiveWithRoles();

    // Users eligible for project management
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name IN ('PROJECT_MANAGER', 'SUPER_ADMIN') AND u.active = true")
    List<User> findEligibleProjectManagers();

    // Users eligible for approvals
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name IN ('ACCOUNT_MANAGER', 'SUPER_ADMIN') AND u.active = true")
    List<User> findEligibleApprovers();
}