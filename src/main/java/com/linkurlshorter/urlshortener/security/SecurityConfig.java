package com.linkurlshorter.urlshortener.security;

import com.linkurlshorter.urlshortener.jwt.JwtRequestFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration class for Spring Security.
 *
 * <p>This class configures security settings such as authentication, authorization,
 * and session management for the application.
 *
 * @author Egor Sivenko
 * @see org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
 * @see org.springframework.context.annotation.Configuration
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * Custom implementation of UserDetailsService for loading user details during authentication.
     */
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Filter class for JWT authentication.
     */
    private final JwtRequestFilter jwtRequestFilter;

    /**
     * Configures the security filter chain for the application.
     *
     * @param http HttpSecurity object to configure security settings
     * @return SecurityFilterChain configured with specified security settings
     * @throws Exception if an error occurs while configuring security settings
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/V1/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/V1/user/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/*").permitAll()
                        .requestMatchers("/api/V1/link/all-links").permitAll()
                        .requestMatchers("/api/V1/link/**").authenticated()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        .anyRequest().denyAll()
                )
                .userDetailsService(customUserDetailsService)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * Configures the authentication manager bean.
     *
     * @param authenticationConfiguration the AuthenticationConfiguration to obtain the authentication manager
     * @return the configured AuthenticationManager bean
     * @throws Exception if an error occurs while configuring the authentication manager
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Configures the password encoder bean.
     *
     * <p>This method creates and configures a {@link org.springframework.security.crypto.password.PasswordEncoder}
     * bean using the {@link org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder} implementation.
     * The BCryptPasswordEncoder is a strong password hashing function that incorporates salt and cost parameters
     * to generate secure password hashes, enhancing the security of user authentication processes.
     *
     * @return the configured PasswordEncoder bean
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
