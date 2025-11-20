package com.alexandros.dailycompanion.config;

//import com.alexandros.dailycompanion.security.FirebaseTokenFilter;
import com.alexandros.dailycompanion.security.JwtRequestFilter;
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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtRequestFilter jwtRequestFilter;

    @Autowired
    public SecurityConfig(UserDetailsService userDetailsService, JwtRequestFilter jwtRequestFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers("/api/v1/auth/**").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/v1/firebase-auth/firebase-login").permitAll()
                                .requestMatchers("/privacy-policy.html").permitAll()
                                .requestMatchers("/images/**", "/css/**", "/js/**", "/webjars/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/v1/saint/today").permitAll()
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
                                .anyRequest().authenticated()
                )
                .sessionManagement(sessions ->
                        sessions.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
