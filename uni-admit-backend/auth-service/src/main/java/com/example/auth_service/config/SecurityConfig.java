package com.example.auth_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF — stateless JWT, no session
                .csrf(AbstractHttpConfigurer::disable)

                // Stateless session — no HttpSession created
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // All /auth/** endpoints are public — no token needed to register/login
                        .requestMatchers("/auth/**").permitAll()
                        // Actuator health endpoint — public for Eureka health checks
                        .requestMatchers("/actuator/**").permitAll()
                        // Everything else requires authentication
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}

