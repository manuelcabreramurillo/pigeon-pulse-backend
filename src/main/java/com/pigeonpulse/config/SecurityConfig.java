package com.pigeonpulse.config;

import com.pigeonpulse.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/login", "/api/auth/logout", "/api/auth/users/search", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Read allowed origins from environment variable, fallback to development defaults
        String corsOrigins = System.getenv("CORS_ALLOWED_ORIGINS");
        System.out.println("🔒 CORS_ALLOWED_ORIGINS env var: '" + corsOrigins + "'");

        if (corsOrigins != null && !corsOrigins.trim().isEmpty()) {
            String[] origins = corsOrigins.split(",");
            // Trim whitespace from each origin
            for (int i = 0; i < origins.length; i++) {
                origins[i] = origins[i].trim();
            }
            configuration.setAllowedOrigins(Arrays.asList(origins));
            System.out.println("🔒 CORS Origins configurados: " + Arrays.toString(origins));
        } else {
            // Development defaults
            configuration.setAllowedOrigins(Arrays.asList("http://localhost:8080", "http://localhost:5173", "http://localhost:3000"));
            System.out.println("🔒 CORS usando valores por defecto de desarrollo");
        }

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}