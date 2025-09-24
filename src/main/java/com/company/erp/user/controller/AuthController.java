package com.company.erp.user.controller;

import com.company.erp.common.security.UserPrincipal;
import com.company.erp.user.dto.request.ChangePasswordRequest;
import com.company.erp.user.dto.request.LoginRequest;
import com.company.erp.user.dto.response.LoginResponse;
import com.company.erp.user.dto.response.UserInfoResponse;
import com.company.erp.user.entity.User;
import com.company.erp.user.repository.UserRepository;
import com.company.erp.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication and user management endpoints")
@Validated
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Login attempt for user: {}", loginRequest.getUsername());

        LoginResponse loginResponse = authService.authenticateUser(loginRequest);

        logger.info("Login successful for user: {}", loginRequest.getUsername());
        return ResponseEntity.ok(loginResponse);
    }

    @Operation(summary = "User logout", description = "Logout current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful")
    })
    @PostMapping("/logout")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('PROJECT_MANAGER') or hasAuthority('ACCOUNT_MANAGER') or hasAuthority('EMPLOYEE')")
    public ResponseEntity<Map<String, String>> logout() {
        authService.logout();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Logout successful");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Refresh token", description = "Refresh JWT token for current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/refresh")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('PROJECT_MANAGER') or hasAuthority('ACCOUNT_MANAGER') or hasAuthority('EMPLOYEE')")
    public ResponseEntity<LoginResponse> refreshToken() {
        LoginResponse loginResponse = authService.refreshToken();
        return ResponseEntity.ok(loginResponse);
    }

    @Operation(summary = "Get current user info", description = "Get information about currently authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User info retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('PROJECT_MANAGER') or hasAuthority('ACCOUNT_MANAGER') or hasAuthority('EMPLOYEE')")
    public ResponseEntity<UserInfoResponse> getCurrentUser() {
        UserPrincipal currentUser = authService.getCurrentUser();

        User user = userRepository.findByIdWithRolesAndBankDetails(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserInfoResponse userInfo = new UserInfoResponse();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setFullName(user.getFullName());
        userInfo.setEmail(user.getEmail());
        userInfo.setPhoneNumber(user.getPhoneNumber());
        userInfo.setDepartment(user.getDepartment());
        userInfo.setPosition(user.getPosition());
        userInfo.setRoles(user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet()));
        userInfo.setActive(user.getActive());

        return ResponseEntity.ok(userInfo);
    }

    @Operation(summary = "Change password", description = "Change password for current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid current password or validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/change-password")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('PROJECT_MANAGER') or hasAuthority('ACCOUNT_MANAGER') or hasAuthority('EMPLOYEE')")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        // Validate passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "New password and confirm password do not match");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        authService.changePassword(request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Password changed successfully");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Check authentication", description = "Check if user is authenticated and return basic info")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User is authenticated"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated")
    })
    @GetMapping("/check")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('PROJECT_MANAGER') or hasAuthority('ACCOUNT_MANAGER') or hasAuthority('EMPLOYEE')")
    public ResponseEntity<Map<String, Object>> checkAuth() {
        UserPrincipal currentUser = authService.getCurrentUser();

        Map<String, Object> response = new HashMap<>();
        response.put("authenticated", true);
        response.put("username", currentUser.getUsername());
        response.put("fullName", currentUser.getFullName());
        response.put("roles", currentUser.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toSet()));

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Check user roles", description = "Check if current user has specific roles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role check completed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('PROJECT_MANAGER') or hasAuthority('ACCOUNT_MANAGER') or hasAuthority('EMPLOYEE')")
    public ResponseEntity<Map<String, Object>> checkRoles(@RequestParam(required = false) String role) {
        Map<String, Object> response = new HashMap<>();

        if (role != null && !role.trim().isEmpty()) {
            response.put("hasRole", authService.hasRole(role));
            response.put("role", role);
        } else {
            UserPrincipal currentUser = authService.getCurrentUser();
            response.put("roles", currentUser.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .collect(Collectors.toSet()));
        }

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Health check", description = "Simple health check endpoint")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service is healthy")
    })
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Authentication Service");

        return ResponseEntity.ok(response);
    }

    // Debug endpoint to check authorities
    @GetMapping("/debug-authorities")
    public ResponseEntity<Map<String, Object>> debugAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> debug = new HashMap<>();
        debug.put("authenticated", authentication.isAuthenticated());
        debug.put("principal", authentication.getPrincipal().getClass().getSimpleName());
        debug.put("authorities", authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        if (authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
            debug.put("userId", user.getId());
            debug.put("username", user.getUsername());
        }

        return ResponseEntity.ok(debug);
    }
}