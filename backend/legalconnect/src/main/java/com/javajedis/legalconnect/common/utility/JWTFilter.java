package com.javajedis.legalconnect.common.utility;

import java.io.IOException;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JWTFilter extends OncePerRequestFilter {
    final UserDetailsService userDetailsService;
    final JWTUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    /**
     * @param userDetailsService the service to load user details
     * @param jwtUtil            the utility to handle JWT operations
     * @param redisTemplate      the Redis template for blacklist checking
     */
    public JWTFilter(UserDetailsService userDetailsService, JWTUtil jwtUtil, StringRedisTemplate redisTemplate) {
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/v1/public/") || path.startsWith("/public/");
    }

    /**
     * Filters incoming requests and validates the JWT token.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @param chain    the filter chain
     * @throws ServletException if an error occurs during filtering
     * @throws IOException      if an I/O error occurs during filtering
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            username = jwtUtil.extractUsername(jwt);
            // Check if token is blacklisted in Redis
            String blacklistKey = "blacklist:jwt:" + jwt;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
                // Optionally, you can log or set a response header here
                chain.doFilter(request, response);
                return;
            }
        }
        if (username != null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            boolean isValidToken = jwtUtil.validateToken(jwt);
            if (isValidToken) {
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, jwt, userDetails.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(request, response);
    }

    public UserDetailsService getUserDetailsService() {
        return userDetailsService;
    }

    public JWTUtil getJwtUtil() {
        return jwtUtil;
    }
}
