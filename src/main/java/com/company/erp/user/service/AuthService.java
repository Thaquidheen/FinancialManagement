package com.company.erp.user.service;

import com.company.erp.common.exception.BusinessException;
import com.company.erp.common.exception.ResourceNotFoundException;
import com.company.erp.common.security.JwtTokenProvider;
import com.company.erp.common.security.UserPrincipal;
import com.company.erp.user.dto.request.LoginRequest;
import com.company.erp.user.dto.request.ChangePasswordRequest;
import com.company.erp.user.dto.response.LoginResponse;
import com.company.erp.user.entity.User;
import com.company.erp.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    /**
     * Authenticate user and return JWT token
     */
    @Transactional
    public LoginResponse authenticateUser(LoginRequest loginRequest) {
        logger.info("Attempting to authenticate user: {}", loginRequest.getUsername());

        try {
            // Find user to check account status before authentication
            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

            // Check if account is locked
            if (user.getAccountLocked()) {
                logger.warn("Login attempt for locked account: {}", loginRequest.getUsername());
                throw new BusinessException("ACCOUNT_LOCKED", "Account is locked. Please contact administrator.");
            }

            // Check if account is active
            if (!user.getActive()) {
                logger.warn("Login attempt for inactive account: {}", loginRequest.getUsername());
                throw new BusinessException("ACCOUNT_INACTIVE", "Account is inactive. Please contact administrator.");
            }

            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // Authentication successful - update user login info
            user.resetFailedLoginAttempts();
            userRepository.updateLastLoginDate(user.getId(), LocalDateTime.now());

            // Generate JWT token
            String jwt = tokenProvider.generateToken(authentication);

            // Get user roles for response
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            logger.info("User {} authenticated successfully", loginRequest.getUsername());

            return LoginResponse.builder()
                    .token(jwt)
                    .tokenType("Bearer")
                    .username(user.getUsername())
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                    .roles(user.getRoles().stream()
                            .map(role -> role.getName())
                            .collect(Collectors.toSet()))
                    .expiresAt(tokenProvider.getExpirationDateFromToken(jwt))
                    .build();

        } catch (BadCredentialsException e) {
            // Handle failed authentication
            logger.warn("Failed authentication attempt for user: {}", loginRequest.getUsername());

            // Increment failed login attempts if user exists
            userRepository.findByUsername(loginRequest.getUsername())
                    .ifPresent(user -> {
                        user.incrementFailedLoginAttempts();
                        userRepository.save(user);

                        if (user.getAccountLocked()) {
                            logger.warn("Account locked for user: {} due to too many failed attempts",
                                    loginRequest.getUsername());
                        }
                    });

            throw new BadCredentialsException("Invalid username or password");
        }
    }

    /**
     * Change user password
     */
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        UserPrincipal currentUser = getCurrentUser();
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException("INVALID_CURRENT_PASSWORD", "Current password is incorrect");
        }

        // Validate new password is different
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BusinessException("SAME_PASSWORD", "New password must be different from current password");
        }

        // Update password
        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        userRepository.updatePassword(user.getId(), encodedNewPassword);

        logger.info("Password changed successfully for user: {}", user.getUsername());
    }

    /**
     * Reset password (Admin function)
     */
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        String encodedPassword = passwordEncoder.encode(newPassword);
        userRepository.updatePassword(userId, encodedPassword);

        // Mark password as expired to force user to change it
        user.setPasswordExpired(true);
        userRepository.save(user);

        logger.info("Password reset for user: {} by admin", user.getUsername());
    }

    /**
     * Unlock user account
     */
    @Transactional
    public void unlockAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.unlockAccount();
        userRepository.save(user);

        logger.info("Account unlocked for user: {}", user.getUsername());
    }

    /**
     * Refresh JWT token
     */
    public LoginResponse refreshToken() {
        UserPrincipal currentUser = getCurrentUser();
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        // Generate new token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String jwt = tokenProvider.generateToken(authentication);

        return LoginResponse.builder()
                .token(jwt)
                .tokenType("Bearer")
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName())
                        .collect(Collectors.toSet()))
                .expiresAt(tokenProvider.getExpirationDateFromToken(jwt))
                .build();
    }

    /**
     * Get current authenticated user
     */
    public UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            throw new BusinessException("NO_AUTHENTICATED_USER", "No authenticated user found");
        }

        return (UserPrincipal) authentication.getPrincipal();
    }

    /**
     * Check if current user has specific role
     */
    public boolean hasRole(String role) {
        try {
            UserPrincipal currentUser = getCurrentUser();
            return currentUser.hasRole(role);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if current user has any of the specified roles
     */
    public boolean hasAnyRole(String... roles) {
        try {
            UserPrincipal currentUser = getCurrentUser();
            return currentUser.hasAnyRole(roles);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Logout user (invalidate token on client side)
     */
    public void logout() {
        // JWT tokens are stateless, so logout is handled on client side
        // We just log the event here
        try {
            UserPrincipal currentUser = getCurrentUser();
            logger.info("User {} logged out", currentUser.getUsername());
        } catch (Exception e) {
            logger.debug("Logout attempt without valid authentication");
        }

        // Clear security context
        SecurityContextHolder.clearContext();
    }
}