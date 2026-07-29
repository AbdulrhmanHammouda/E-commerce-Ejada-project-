package com.example.apigateway.filter;

import com.example.apigateway.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String path = request.getRequestURI();

        // Bypass security for public endpoints (like login/register)
        if (path.startsWith("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        // For all other requests, verify the Authorization header
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                // Validate the token (this will throw an exception if invalid/expired)
                jwtUtil.validateToken(token);
                
                // Extract role and userId
                String role = jwtUtil.extractRole(token);
                if (role == null) role = "USER";
                Long userId = jwtUtil.extractUserId(token);
                String finalRole = role;
                String finalUserId = userId != null ? String.valueOf(userId) : null;

                // Wrap request to inject X-User-Role and X-User-Id headers
                HttpServletRequest wrappedRequest = new jakarta.servlet.http.HttpServletRequestWrapper(request) {
                    @Override
                    public String getHeader(String name) {
                        if ("X-User-Role".equalsIgnoreCase(name)) return finalRole;
                        if ("X-User-Id".equalsIgnoreCase(name) && finalUserId != null) return finalUserId;
                        return super.getHeader(name);
                    }
                    @Override
                    public java.util.Enumeration<String> getHeaders(String name) {
                        if ("X-User-Role".equalsIgnoreCase(name)) {
                            return java.util.Collections.enumeration(java.util.Collections.singletonList(finalRole));
                        }
                        if ("X-User-Id".equalsIgnoreCase(name) && finalUserId != null) {
                            return java.util.Collections.enumeration(java.util.Collections.singletonList(finalUserId));
                        }
                        return super.getHeaders(name);
                    }
                    @Override
                    public java.util.Enumeration<String> getHeaderNames() {
                        java.util.List<String> names = java.util.Collections.list(super.getHeaderNames());
                        names.add("X-User-Role");
                        if (finalUserId != null) {
                            names.add("X-User-Id");
                        }
                        return java.util.Collections.enumeration(names);
                    }
                };
                
                // Token is valid! Pass the wrapped request along to the microservice
                filterChain.doFilter(wrappedRequest, response);
            } catch (Exception e) {
                // Token is invalid
                System.out.println("Invalid JWT Token: " + e.getMessage());
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid or expired JWT token");
            }
        } else {
            // Missing Authorization header
            System.out.println("Missing Authorization Header!");
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Missing Authorization Header");
        }
    }
}
