package com.chatup.backend.config;

import org.apache.catalina.filters.CorsFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity()
@EnableWebSecurity
public class SecurityConfig {
    private final JwtRequestFilter jwtRequestFilter;

    public SecurityConfig(JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(
                        cors -> {
                            CorsConfigurationSource source = request -> {
                                CorsConfiguration config = new CorsConfiguration();
                                config.setAllowedOrigins(List.of("*"));
                                config.setAllowedMethods(List.of("*"));
                                config.setAllowedHeaders(List.of("*"));
                                return config;
                            };
                            cors.configurationSource(source);
                        }
                )//AbstractHttpConfigurer::disable)
                .sessionManagement((sessionManagement) -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(
                                "/api/**",
                                "/ws-endpoint/**",
                                "/api/ws-endpoint/**",
                                "/ws/**",
                                "/api/ws/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api/v3/api-docs/**",
                                "/swagger-ui/index.html/**",
                                "/api/user/search",
                                "/user/search",
                                "/auth/**",
                                "/api/auth/**",
                                "/api/swagger-ui/**",
                                "/swagger-ui/index.html/**",
                                "/swagger-ui/favicon-32x32.png",
                                "/swagger-ui/favicon-16x16.png",
                                "/api/swagger-ui/swagger-ui-standalone-preset.js",
                                "/api/swagger-ui/swagger-ui-bundle.js",
                                "/swagger-ui/swagger-ui-bundle.js",
                                "/api/swagger-ui/swagger-ui.css",
                                "/swagger-ui/swagger-ui.css",
                                "/api/swagger-ui/index.css",
                                "/swagger-ui/index.css",
                                "/swagger-ui/swagger-ui-standalone-preset.js",
                                "/api/swagger-ui/swagger-initializer.js",
                                "/swagger-ui/swagger-initializer.js",
                                "/api/v3/api-docs/public-api/**",
                                "/v3/api-docs/public-api/**",
                                "/api/v3/api-docs/swagger-config",
                                "/v3/api-docs/swagger-config")
                        .permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, AuthenticationManagerBuilder auth) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class).build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
