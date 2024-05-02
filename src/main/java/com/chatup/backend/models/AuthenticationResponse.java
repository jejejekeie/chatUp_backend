package com.chatup.backend.models;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuthenticationResponse {
    private final String jwt;
    private final String userId;
    public AuthenticationResponse(String jwt, String userId) {
        this.jwt = jwt;
        this.userId = userId;
    }
}
