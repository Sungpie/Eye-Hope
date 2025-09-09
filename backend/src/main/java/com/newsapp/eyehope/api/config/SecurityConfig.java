package com.newsapp.eyehope.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final Sha256PasswordEncoder passwordEncoder;

    public SecurityConfig(CustomUserDetailsService userDetailsService, Sha256PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Configure CSRF protection - enable for web pages, disable for API endpoints
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**", "/swagger-ui/**", "/v3/api-docs/**"))

            // Configure authorization
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/users").permitAll()
                .requestMatchers("/api/users/password").permitAll()
                .requestMatchers("/api/news/**").permitAll() // We handle admin authorization in the controller
                .requestMatchers("/api/admin/**").permitAll() // We handle admin authorization in the controller
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/swagger-ui/index.html", "/v3/api-docs/**").permitAll()
                .requestMatchers("/api/health").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/favicon.ico").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                // Allow all requests without authentication - we handle admin authorization in controllers
                .anyRequest().permitAll()
            )

            // Disable HTTP Basic authentication for Swagger UI
            .httpBasic(httpBasic -> httpBasic.disable())

            // Configure session management
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Configure exception handling
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    // Let the GlobalExceptionHandler handle it
                    throw authException;
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    // Let the GlobalExceptionHandler handle it
                    throw accessDeniedException;
                })
            )

            // Set authentication provider
            .authenticationProvider(authenticationProvider());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return passwordEncoder;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // This bean is no longer needed as we're allowing all requests without authentication
    // @Bean
    // public WebSecurityCustomizer webSecurityCustomizer() {
    //     return (web) -> web.ignoring()
    //             .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**");
    // }
}
