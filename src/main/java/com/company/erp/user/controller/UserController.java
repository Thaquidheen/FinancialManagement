package com.company.erp.user.controller;

import com.company.erp.user.dto.request.AssignRolesRequest;
import com.company.erp.user.dto.request.CreateUserRequest;
import com.company.erp.user.dto.request.UpdateUserRequest;
import com.company.erp.user.dto.response.UserResponse;
import com.company.erp.user.entity.Role;
import com.company.erp.user.entity.UserBankDetails;
import com.company.erp.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@Tag(name = "User Management", description = "User CRUD operations and management endpoints")
@Validated
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Operation(summary = "Create new user", description = "Create a new user with roles and optional bank details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or duplicate data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        logger.info("Creating new user: {}", request.getUsername());

        UserResponse userResponse = userService.createUser(request);

        logger.info("User created successfully with ID: {}", userResponse.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @Operation(summary = "Get user by ID", description = "Retrieve user details by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER') or @userService.isCurrentUser(#userId)")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "User ID") @PathVariable Long userId) {

        logger.debug("Fetching user with ID: {}", userId);
        UserResponse userResponse = userService.getUserById(userId);
        return ResponseEntity.ok(userResponse);
    }

    @Operation(summary = "Get available roles", description = "Get list of all available system roles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Roles retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAvailableRoles() {
        // You'll need to create this method in your UserService
        List<Role> roles = userService.getAllActiveRoles();

        List<Map<String, Object>> rolesList = roles.stream()
                .map(role -> {
                    Map<String, Object> roleMap = new HashMap<>();
                    roleMap.put("id", role.getId());
                    roleMap.put("name", role.getName());
                    roleMap.put("displayName", role.getName().replace("_", " "));
                    roleMap.put("description", role.getDescription());
                    roleMap.put("isActive", role.getActive());
                    return roleMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(rolesList);
    }



    @Operation(summary = "Get all users", description = "Retrieve paginated list of all users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER')")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "fullName") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Search users", description = "Search users by name, department, and status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER')")
    public ResponseEntity<Page<UserResponse>> searchUsers(
            @Parameter(description = "Full name search term") @RequestParam(required = false) String fullName,
            @Parameter(description = "Department filter") @RequestParam(required = false) String department,
            @Parameter(description = "Active status filter") @RequestParam(required = false) Boolean active,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("fullName"));
        Page<UserResponse> users = userService.searchUsers(fullName, department, active, pageable);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Update user", description = "Update user information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{userId}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or @userService.isCurrentUser(#userId)")
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest request) {

        logger.info("Updating user with ID: {}", userId);
        UserResponse userResponse = userService.updateUser(userId, request);
        return ResponseEntity.ok(userResponse);
    }

    @Operation(summary = "Update user roles", description = "Update roles assigned to a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Roles updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid roles"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{userId}/roles")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<UserResponse> updateUserRoles(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Valid @RequestBody AssignRolesRequest request) {

        logger.info("Updating roles for user ID: {}", userId);
        UserResponse userResponse = userService.updateUserRoles(userId, request.getRoles());
        return ResponseEntity.ok(userResponse);
    }

    @Operation(summary = "Update bank details", description = "Update user bank details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bank details updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{userId}/bank-details")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER') or @userService.isCurrentUser(#userId)")
    public ResponseEntity<UserResponse> updateBankDetails(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Valid @RequestBody UserBankDetails bankDetails) {

        logger.info("Updating bank details for user ID: {}", userId);
        UserResponse userResponse = userService.updateBankDetails(userId, bankDetails);
        return ResponseEntity.ok(userResponse);
    }

    @Operation(summary = "Deactivate user", description = "Deactivate a user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{userId}/deactivate")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> deactivateUser(
            @Parameter(description = "User ID") @PathVariable Long userId) {

        logger.info("Deactivating user with ID: {}", userId);
        userService.deactivateUser(userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User deactivated successfully");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Activate user", description = "Activate a user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User activated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{userId}/activate")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> activateUser(
            @Parameter(description = "User ID") @PathVariable Long userId) {

        logger.info("Activating user with ID: {}", userId);
        userService.activateUser(userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User activated successfully");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete user", description = "Soft delete a user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> deleteUser(
            @Parameter(description = "User ID") @PathVariable Long userId) {

        logger.info("Deleting user with ID: {}", userId);
        userService.deleteUser(userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User deleted successfully");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get project managers", description = "Get list of all project managers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project managers retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/project-managers")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER')")
    public ResponseEntity<List<UserResponse>> getProjectManagers() {
        List<UserResponse> projectManagers = userService.getProjectManagers();
        return ResponseEntity.ok(projectManagers);
    }

    @Operation(summary = "Get account managers", description = "Get list of all account managers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account managers retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/account-managers")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<List<UserResponse>> getAccountManagers() {
        List<UserResponse> accountManagers = userService.getAccountManagers();
        return ResponseEntity.ok(accountManagers);
    }

    @Operation(summary = "Get users by role", description = "Get users filtered by role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/by-role/{roleName}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER')")
    public ResponseEntity<List<UserResponse>> getUsersByRole(
            @Parameter(description = "Role name") @PathVariable String roleName) {

        List<UserResponse> users = userService.getUsersByRole(roleName);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get user statistics", description = "Get user statistics and metrics")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER')")
    public ResponseEntity<Map<String, Object>> getUserStatistics() {
        UserService.UserStatistics stats = userService.getUserStatistics();

        Map<String, Object> response = new HashMap<>();
        response.put("totalUsers", stats.getTotalUsers());
        response.put("projectManagers", stats.getProjectManagers());
        response.put("accountManagers", stats.getAccountManagers());
        response.put("employees", stats.getEmployees());
        response.put("departmentCounts", stats.getDepartmentCounts());

        return ResponseEntity.ok(response);
    }
}