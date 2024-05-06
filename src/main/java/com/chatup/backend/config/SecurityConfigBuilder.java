package com.chatup.backend.config;

public class SecurityConfigBuilder {
    private JwtRequestFilter jwtRequestFilter;

    public SecurityConfigBuilder setJwtRequestFilter(JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
        return this;
    }

    public SecurityConfig createSecurityConfig() {
        return new SecurityConfig(jwtRequestFilter);
    }
}
