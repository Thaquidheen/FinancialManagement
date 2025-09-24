package com.company.erp.common.config;

import com.company.erp.common.security.CustomUserDetailsService;
import com.company.erp.common.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF as we use JWT tokens
                .csrf(AbstractHttpConfigurer::disable)

                // Enable CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Set session management to stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Set permissions on endpoints
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - ONLY login should be public
                        .requestMatchers("/api/auth/login", "/auth/login").permitAll()
                        .requestMatchers("/api/auth/health", "/auth/health").permitAll()
                        .requestMatchers("/api/actuator/**", "/actuator/**").permitAll()
                        .requestMatchers("/api/swagger-ui/**", "/swagger-ui/**").permitAll()
                        .requestMatchers("/api/v3/api-docs/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/swagger-resources/**", "/swagger-resources/**").permitAll()
                        .requestMatchers("/api/webjars/**", "/webjars/**").permitAll()
                        .requestMatchers("/api/", "/", "/error").permitAll()
                        .requestMatchers("/health", "/api/health").permitAll()

                        // Admin only endpoints
                        .requestMatchers(HttpMethod.POST, "/api/users").hasAuthority("SUPER_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/users/*/roles").hasAuthority("SUPER_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/*").hasAuthority("SUPER_ADMIN")
                        .requestMatchers("/api/admin/**").hasAuthority("SUPER_ADMIN")

                        // Project management endpoints
                        .requestMatchers(HttpMethod.POST, "/api/projects").hasAuthority("SUPER_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/projects/*/assign").hasAuthority("SUPER_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/projects").hasAnyAuthority("SUPER_ADMIN", "PROJECT_MANAGER", "ACCOUNT_MANAGER")

                        // Auth endpoints that require authentication
                        .requestMatchers("/api/auth/me", "/auth/me").authenticated()
                        .requestMatchers("/api/auth/debug**", "/auth/debug**").authenticated()
                        .requestMatchers("/api/auth/**", "/auth/**").authenticated()

                        // All other requests need authentication
                        .anyRequest().authenticated()
                )

                // Set authentication provider
                .authenticationProvider(authenticationProvider())

                // Add JWT filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow specific origins (configure based on environment)
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:5173",
                "http://127.0.0.1:3000",
                "http://127.0.0.1:5173",
                "https://erp.yourcompany.com"
        ));

        // Allow specific HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // Allow specific headers
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        // Allow credentials
        configuration.setAllowCredentials(true);

        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}