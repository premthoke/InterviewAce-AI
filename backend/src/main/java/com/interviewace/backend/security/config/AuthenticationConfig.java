package com.interviewace.backend.security.config;

import com.interviewace.backend.security.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Centralizes Spring Security's authentication infrastructure.
 *
 * Exposes:
 *   - AuthenticationProvider (DaoAuthenticationProvider wired with UserDetailsService + BCrypt)
 *   - AuthenticationManager (delegated from Spring's AuthenticationConfiguration)
 *
 * The PasswordEncoder bean is defined in SecurityConfig and injected here to avoid duplication.
 */
@Configuration
public class AuthenticationConfig {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationConfig(CustomUserDetailsService userDetailsService,
                                PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}
