/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.config;

import com.alexandros.dailycompanion.security.JwtAuthenticationEntryPoint;
import com.alexandros.dailycompanion.security.JwtRequestFilter;
import com.alexandros.dailycompanion.security.JwtUtil;
import com.alexandros.dailycompanion.security.RateLimitFilter;
import com.alexandros.dailycompanion.service.ServiceHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Central security configuration for the application.
 * <p>
 * This configuration defines authentication and authorization rules,
 * configures JWT-based stateless security, and restricts access to
 * protected API endpoints based on user roles.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    public SecurityConfig(UserDetailsService userDetailsService, JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    /**
     * Configures the HTTP security filter chain.
     * <p>
     * Defines:
     * <ul>
     *     <li>Public and protected endpoints</li>
     *     <li>Role-based access control</li>
     *     <li>JWT authentication filter integration</li>
     *     <li>Stateless session management</li>
     * </ul>
     *
     * @param http HTTP security configuration
     * @return configured {@link SecurityFilterChain}
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtRequestFilter jwtRequestFilter,
                                                   RateLimitFilter rateLimitFilter) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers("/api/v1/auth/**").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/v1/firebase-auth/firebase-login").permitAll()
                                .requestMatchers(
                                        "/privacy-policy.html",
                                        "/terms-of-service.html",
                                        "/static/**",
                                        "/images/**",
                                        "/css/**",
                                        "/js/**",
                                        "/webjars/**"
                                ).permitAll()
                                .requestMatchers("/api/v1/app/version").permitAll()
                                .requestMatchers(HttpMethod.GET,"/api/v1/saint/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")
                                .requestMatchers(HttpMethod.GET,"/api/v1/saint/feast/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")
                                .requestMatchers(HttpMethod.GET,"/api/v1/saint/month/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")
                                .requestMatchers(HttpMethod.PUT,"/api/v1/saint/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.POST,"/api/v1/saint/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.DELETE,"/api/v1/saint/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers("/api/v1/journal-entry/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")
                                .requestMatchers(HttpMethod.GET,"/api/v1/journal-entry/dates/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")
                                .requestMatchers(HttpMethod.GET, "/api/v1/user").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.GET, "/api/v1/user/*").hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")
                                .requestMatchers(HttpMethod.PUT, "/api/v1/user/*").hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")
                                .requestMatchers(HttpMethod.DELETE, "/api/v1/user/*").hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")
                                .requestMatchers(HttpMethod.POST, "/api/v1/user").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.POST, "/api/v1/feedback").hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")
                                .requestMatchers(HttpMethod.GET, "/api/v1/feedback").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/v1/feedback/*").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.GET, "/api/v1/admin/*").hasAuthority("ROLE_ADMIN")
                                .anyRequest().authenticated()
                )
                .sessionManagement(sessions ->
                        sessions.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * Provides the authentication manager used by Spring Security.
     *
     * @param config authentication configuration
     * @return authentication manager
     * @throws Exception if initialization fails
     */
    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Password encoder used for hashing and verifying user passwords.
     * <p>
     * Uses BCrypt for strong, adaptive hashing.
     *
     * @return password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtRequestFilter jwtRequestFilter(JwtUtil jwtUtil) {
        return new JwtRequestFilter(jwtUtil);
    }

    @Bean
    public RateLimitFilter rateLimitFilter(JwtUtil jwtUtil, ServiceHelper serviceHelper) {
        return new RateLimitFilter(jwtUtil, serviceHelper);
    }

}
