package com.company.erp.common.security;

import com.company.erp.common.constants.ApplicationConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = getJwtFromRequest(request);
            logger.debug("Processing request: {} with JWT: {}", request.getRequestURI(),
                    jwt != null ? "present" : "absent");

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                String username = tokenProvider.getUsernameFromToken(jwt);
                logger.debug("JWT is valid for user: {}", username);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                logger.debug("Loaded user details for: {}, authorities: {}",
                        username, userDetails.getAuthorities());

                if (userDetails != null && userDetails.isEnabled()) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    logger.debug("Set Security Context for user: {} with authorities: {}",
                            username, userDetails.getAuthorities());
                }
            } else {
                logger.debug("JWT validation failed or no JWT present for request: {}", request.getRequestURI());
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context for request: {}",
                    request.getRequestURI(), ex);
            // Clear the security context in case of any exception
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(ApplicationConstants.Security.AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) &&
                bearerToken.startsWith(ApplicationConstants.Security.BEARER_PREFIX)) {
            return bearerToken.substring(ApplicationConstants.Security.TOKEN_BEGIN_INDEX);
        }

        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // FIXED: Only skip authentication for login and public endpoints
        // Don't skip /auth/me, /auth/debug-authorities, etc.
        boolean shouldSkip = path.equals("/api/auth/login") ||
                path.equals("/auth/login") ||
                path.startsWith("/api/actuator/") ||
                path.startsWith("/actuator/") ||
                path.startsWith("/api/swagger-ui") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/api/v3/api-docs") ||
                path.startsWith("/v3/api-docs") ||
                path.equals("/api/") ||
                path.equals("/api") ||
                path.equals("/") ||
                path.equals("/error") ||
                path.equals("/health") ||
                path.equals("/api/health") ||
                path.equals("/api/auth/health") ||
                path.equals("/auth/health");

        logger.debug("Request path: {}, shouldSkip: {}", path, shouldSkip);
        return shouldSkip;
    }
}