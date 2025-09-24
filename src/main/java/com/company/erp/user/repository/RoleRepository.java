package com.company.erp.user.repository;

import com.company.erp.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    // Basic finder methods
    Optional<Role> findByName(String name);

    Optional<Role> findByNameAndActiveTrue(String name);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    // Find active roles
    List<Role> findByActiveTrue();

    // Find roles by name pattern
    @Query("SELECT r FROM Role r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :namePattern, '%')) AND r.active = true")
    List<Role> findByNameContainingIgnoreCase(@Param("namePattern") String namePattern);

    // Role statistics
    @Query("SELECT COUNT(ur) FROM Role r JOIN r.users ur WHERE r.id = :roleId AND ur.active = true")
    long countActiveUsersByRole(@Param("roleId") Long roleId);

    @Query("SELECT r.name, COUNT(ur) FROM Role r LEFT JOIN r.users ur WHERE ur.active = true OR ur IS NULL GROUP BY r.name, r.id")
    List<Object[]> countUsersByRoles();

    // Find roles with users
    @Query("SELECT DISTINCT r FROM Role r LEFT JOIN FETCH r.users WHERE r.active = true")
    List<Role> findAllActiveWithUsers();
}