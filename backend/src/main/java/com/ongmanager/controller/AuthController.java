package com.ongmanager.controller;

import com.ongmanager.dto.request.*;
import com.ongmanager.dto.response.ApiResponse;
import com.ongmanager.dto.response.AuthResponse;
import com.ongmanager.dto.response.UserResponse;
import com.ongmanager.service.interfaces.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(authService.register(req), "Usuario criado com sucesso."));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(req)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(authService.refresh(req)));
    }

    @PostMapping("/password/reset-request")
    public ResponseEntity<ApiResponse<Void>> resetRequest(@Valid @RequestBody PasswordResetRequest req) {
        authService.requestPasswordReset(req);
        return ResponseEntity.ok(ApiResponse.ok(null, "Se o e-mail existir, um link sera enviado."));
    }

    @PostMapping("/password/reset-confirm")
    public ResponseEntity<ApiResponse<Void>> resetConfirm(@Valid @RequestBody PasswordResetConfirm req) {
        authService.confirmPasswordReset(req);
        return ResponseEntity.ok(ApiResponse.ok(null, "Senha redefinida"));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.ok(null, "E-mail verificado com sucesso"));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateMe(@RequestBody UpdateProfileRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(authService.updateProfile(req)));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deactivate() {
        authService.deactivateAccount();
        return ResponseEntity.ok(ApiResponse.ok(null, "Conta desativada"));
    }
}
