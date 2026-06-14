package com.ongmanager.dto.response;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.Set;

@Getter @AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private UserResponse user;
}
