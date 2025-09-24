package com.company.erp.common.security;

import com.company.erp.common.exception.ResourceNotFoundException;
import com.company.erp.user.entity.User;
import com.company.erp.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Loading user by username: {}", username);

        // Find user with roles - must be active
        User user = userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("User not found with username: %s", username)));

        // Check if user is active
        if (!user.getActive()) {
            logger.warn("User {} is not active", username);
            throw new UsernameNotFoundException("User account is inactive");
        }

        logger.debug("Found user: {} with {} roles", user.getUsername(), user.getRoles().size());

        // Debug: Print the actual roles
        user.getRoles().forEach(role ->
                logger.debug("User {} has role: {}", username, role.getName()));

        UserPrincipal userPrincipal = UserPrincipal.create(user);

        logger.debug("Created UserPrincipal with authorities: {}", userPrincipal.getAuthorities());

        return userPrincipal;
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        logger.debug("Loading user by ID: {}", id);

        User user = userRepository.findByIdWithRolesAndBankDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (!user.getActive()) {
            throw new ResourceNotFoundException("Active user", "id", id);
        }

        logger.debug("Found user: {} with ID: {}", user.getUsername(), id);

        return UserPrincipal.create(user);
    }
}