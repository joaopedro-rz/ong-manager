package com.ongmanager.service.interfaces;

import com.ongmanager.dto.request.*;
import com.ongmanager.dto.response.AuthResponse;
import com.ongmanager.dto.response.UserResponse;

public interface AuthService {
    UserResponse register(RegisterRequest req);
    AuthResponse login(LoginRequest req);
    AuthResponse refresh(RefreshTokenRequest req);
    void requestPasswordReset(PasswordResetRequest req);
    void confirmPasswordReset(PasswordResetConfirm req);
    void verifyEmail(String token);
    UserResponse updateProfile(UpdateProfileRequest req);
    void deactivateAccount();
}
