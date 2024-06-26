package com.chatup.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
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
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
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
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:4200",
                "http://localhost:4200/chat",
                "http://localhost:4200/chat/**",
                "https://effortless-cucurucho-949d34.netlify.app" ));
        //configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedMethods(List.of("*"));
        //configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement((sessionManagement) -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(
                                "/api/**",
                                "api/chat/**",
                                "/chat/**",
                                "/ws/**",
                                "/api/ws/**",
                                "/api/ws/info/**",
                                "/ws/info/**",
                                "/error",
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
