package com.company.erp.user.service;

import com.company.erp.common.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("userPermissionService")
public class UserPermissionService {

    @Autowired
    private UserService userService;

    /**
     * Check if current user is the same as the given user ID
     */
    public boolean isCurrentUser(Long userId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return false;
            }

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            return userPrincipal.getId().equals(userId);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if current user has specific role
     */
    public boolean hasRole(String role) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return false;
            }

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            return userPrincipal.hasRole(role);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if current user can manage the given user
     */
    public boolean canManageUser(Long userId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return false;
            }

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            // Super admin can manage all users
            if (userPrincipal.hasRole("SUPER_ADMIN")) {
                return true;
            }

            // Users can manage themselves
            if (userPrincipal.getId().equals(userId)) {
                return true;
            }

            // Account managers can view/edit basic user info (not roles)
            if (userPrincipal.hasRole("ACCOUNT_MANAGER")) {
                return true;
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }
}