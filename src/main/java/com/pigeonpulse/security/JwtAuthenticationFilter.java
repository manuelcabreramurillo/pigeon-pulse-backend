package com.pigeonpulse.security;

import com.pigeonpulse.model.Palomar;
import com.pigeonpulse.service.PalomarService;
import com.pigeonpulse.service.UsuarioPalomarService;
import com.pigeonpulse.service.UsuarioService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PalomarService palomarService;

    @Autowired
    private UsuarioPalomarService usuarioPalomarService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String requestURI = request.getRequestURI();

        // Skip JWT processing for login/logout endpoints and public endpoints
        if ((requestURI.startsWith("/api/auth/login") || requestURI.startsWith("/api/auth/logout") ||
             requestURI.startsWith("/api/auth/users/search")) ||
            requestURI.startsWith("/swagger-ui/") ||
            requestURI.startsWith("/v3/api-docs/")) {
            chain.doFilter(request, response);
            return;
        }

        final String requestTokenHeader = request.getHeader("Authorization");

        String userId = null;
        String jwtToken = null;

        // Debug logging
        System.out.println("JwtAuthenticationFilter: Processing request to " + requestURI);
        System.out.println("JwtAuthenticationFilter: Authorization header: " + (requestTokenHeader != null ? "present" : "null"));

        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            System.out.println("JwtAuthenticationFilter: JWT token extracted, length: " + jwtToken.length());
            try {
                userId = jwtUtil.extractUserId(jwtToken);
                System.out.println("JwtAuthenticationFilter: Extracted userId: " + userId);
            } catch (Exception e) {
                System.out.println("JwtAuthenticationFilter: Unable to get JWT Token or JWT Token has expired: " + e.getMessage());
                logger.warn("Unable to get JWT Token or JWT Token has expired");
            }
        } else {
            System.out.println("JwtAuthenticationFilter: No Bearer token found in Authorization header");
        }

        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // Extract palomar context from JWT
                String palomarId = jwtUtil.extractPalomarId(jwtToken);
                String rol = jwtUtil.extractRol(jwtToken);

                System.out.println("JwtAuthenticationFilter: Extracted palomarId: " + palomarId);
                System.out.println("JwtAuthenticationFilter: Extracted rol: " + rol);

                if (palomarId != null && rol != null) {
                    System.out.println("JwtAuthenticationFilter: Validating user exists...");
                    // Validate user exists
                    var usuarioOpt = usuarioService.findById(userId);
                    if (usuarioOpt.isPresent()) {
                        var usuario = usuarioOpt.get();
                        System.out.println("JwtAuthenticationFilter: User found: " + usuario.getId());

                        boolean tokenValid = jwtUtil.validateToken(jwtToken, userId);
                        System.out.println("JwtAuthenticationFilter: Token valid: " + tokenValid);

                        if (tokenValid) {
                            System.out.println("JwtAuthenticationFilter: Getting palomar details...");
                            // Get palomar details (this is the user's default palomar from JWT, used for context)
                            var palomarOpt = palomarService.findById(palomarId);
                            if (palomarOpt.isPresent()) {
                                var palomar = palomarOpt.get();
                                System.out.println("JwtAuthenticationFilter: Palomar found: " + palomar.getId());

                                // Create palomar context with user's default palomar
                                // Note: Individual endpoints will validate specific palomar access as needed
                                PalomarContext palomarContext = new PalomarContext(usuario, palomar, rol);
                                System.out.println("JwtAuthenticationFilter: Setting authentication context");

                                UsernamePasswordAuthenticationToken authenticationToken =
                                        new UsernamePasswordAuthenticationToken(palomarContext, null, new ArrayList<>());
                                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                                System.out.println("JwtAuthenticationFilter: Authentication context set successfully");
                            } else {
                                System.out.println("JwtAuthenticationFilter: Palomar not found");
                            }
                        } else {
                            System.out.println("JwtAuthenticationFilter: Token invalid");
                        }
                    } else {
                        System.out.println("JwtAuthenticationFilter: User not found");
                    }
                } else {
                    System.out.println("JwtAuthenticationFilter: palomarId or rol is null");
                }
            } catch (Exception e) {
                System.out.println("JwtAuthenticationFilter: Error validating JWT token: " + e.getMessage());
                logger.error("Error validating JWT token", e);
            }
        } else {
            System.out.println("JwtAuthenticationFilter: userId is null or authentication already exists");
        }
        chain.doFilter(request, response);
    }
}