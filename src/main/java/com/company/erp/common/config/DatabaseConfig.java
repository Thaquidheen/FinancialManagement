package com.company.erp.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Optional;

@Configuration
@EnableJpaRepositories(basePackages = "com.company.erp")
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@EnableTransactionManagement
public class DatabaseConfig {

    @Bean
    public AuditorAware<Long> auditorProvider() {
        return new AuditorAwareImpl();
    }

    public static class AuditorAwareImpl implements AuditorAware<Long> {
        @Override
        public Optional<Long> getCurrentAuditor() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.of(1L); // System user ID
            }

            // Extract user ID from authentication principal
            if (authentication.getPrincipal() instanceof com.company.erp.common.security.UserPrincipal) {
                com.company.erp.common.security.UserPrincipal userPrincipal = 
                    (com.company.erp.common.security.UserPrincipal) authentication.getPrincipal();
                return Optional.of(userPrincipal.getId());
            } else if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
                // If using UserDetails, you might need to fetch user ID from database
                // For now, return a default user ID
                return Optional.of(1L);
            }

            return Optional.of(1L); // Default fallback
        }
    }
}