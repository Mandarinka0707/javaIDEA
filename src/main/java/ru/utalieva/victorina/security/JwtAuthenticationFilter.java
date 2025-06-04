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
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.utalieva.victorina.service.impl.UserServiceImpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtTokenUtil jwtTokenUtil;
    private final UserServiceImpl userService;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(
            JwtTokenUtil jwtTokenUtil,
            @Lazy UserServiceImpl userService,
            ObjectMapper objectMapper
    ) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain chain) throws ServletException, IOException {
        try {
            if (shouldSkipFilter(request)) {
                chain.doFilter(request, response);
                return;
            }

            String authHeader = request.getHeader("Authorization");
            String username = null;
            String jwt = null;

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);
                username = jwtTokenUtil.extractUsername(jwt);
                logger.debug("JWT token found. Username from token: {}", username);
            } else {
                logger.debug("No JWT token found in request");
                handleAuthenticationError(response, "Токен не найден");
                return;
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userService.loadUserByUsername(username);
                logger.debug("Loaded UserDetails for username: {}", username);
                
                if (jwtTokenUtil.validateToken(jwt, userDetails)) {
                    Long userId = jwtTokenUtil.extractUserId(jwt);
                    logger.debug("Valid JWT token. User ID: {}, Username: {}", userId, username);
                    
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("Authentication set in SecurityContext. User: {}, Authorities: {}", 
                            username, userDetails.getAuthorities());
                    chain.doFilter(request, response);
                } else {
                    logger.warn("Invalid JWT token for user: {}", username);
                    handleAuthenticationError(response, "Недействительный токен");
                }
            } else {
                logger.warn("No username in token or authentication already exists");
                handleAuthenticationError(response, "Ошибка аутентификации");
            }
        } catch (Exception e) {
            logger.error("Error processing JWT token", e);
            handleAuthenticationError(response, "Внутренняя ошибка сервера");
        }
    }

    private boolean shouldSkipFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/api/auth/login") || 
               path.equals("/api/auth/register") || 
               path.equals("/error") || 
               (path.startsWith("/api/quizzes") && request.getMethod().equals("GET") && !path.contains("/my"));
    }

    private void handleAuthenticationError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Map<String, String> error = new HashMap<>();
        error.put("error", "Unauthorized");
        error.put("message", message);
        
        objectMapper.writeValue(response.getOutputStream(), error);
    }
}