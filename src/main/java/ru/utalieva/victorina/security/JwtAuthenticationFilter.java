package ru.utalieva.victorina.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtTokenUtil jwtTokenUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtTokenUtil jwtTokenUtil,
            @Lazy UserDetailsService userDetailsService
    ) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain chain) throws ServletException, IOException {
        try {
            // Логируем детали запроса
            logger.debug("Processing request: {} {}", request.getMethod(), request.getRequestURI());
            logger.debug("Headers: {}", request.getHeaderNames());
            
            final String authorizationHeader = request.getHeader("Authorization");
            logger.debug("Authorization header: {}", authorizationHeader);

            String username = null;
            String jwt = null;

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwt = authorizationHeader.substring(7);
                logger.debug("Extracted JWT token: {}", jwt);
                
                try {
                    username = jwtTokenUtil.extractUsername(jwt);
                    logger.debug("Extracted username from JWT: {}", username);
                } catch (Exception e) {
                    logger.error("Failed to extract username from JWT: {}", e.getMessage());
                }
            } else {
                logger.debug("No Bearer token found in request");
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                    logger.debug("Loaded user details for {}: {}", username, userDetails);

                    if (jwtTokenUtil.validateToken(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        logger.debug("Authentication successful for user: {}", username);
                    } else {
                        logger.warn("Token validation failed for user: {}", username);
                    }
                } catch (Exception e) {
                    logger.error("Error loading user details: {}", e.getMessage());
                }
            } else {
                logger.debug("No authentication needed or already authenticated");
            }
        } catch (Exception e) {
            logger.error("Error processing JWT token: {}", e.getMessage(), e);
        }

        chain.doFilter(request, response);
    }
}