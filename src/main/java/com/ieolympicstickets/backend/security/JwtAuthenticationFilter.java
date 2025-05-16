package com.ieolympicstickets.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT filter that authenticates requests based on Bearer tokens
 */

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // If header present and starts with Bearer, attempt token validation
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtService.validateToken(token)) {
                String email = jwtService.extractSubject(token);
                List<String> roles = jwtService.extractRoles(token);
                var authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
                var auth = new UsernamePasswordAuthenticationToken(email, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                logger.debug(">>> JwtAuthenticationFilter: Token validation failed");
            }
        }

        // Continue the filter chain
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        String method = request.getMethod();

        // Toujours autoriser le pré-vol CORS
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        // Ne PAS filtrer login & register (elles sont permitAll)
        if (   path.equals("/api/auth/login")
                || path.equals("/api/auth/register")) {
            return true;
        }

        // bypasser la lecture publique sur  entités events/offers
        if ("GET".equalsIgnoreCase(method) && (
                path.equals("/api/events") || path.startsWith("/api/events/")
                        || path.equals("/api/offers") || path.startsWith("/api/offers/")
        )) {
            return true;
        }

        return false;
    }
}

