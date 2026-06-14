package com.ongmanager.service.impl;

import com.ongmanager.dto.request.*;
import com.ongmanager.dto.response.AuthResponse;
import com.ongmanager.dto.response.UserResponse;
import com.ongmanager.entity.*;
import com.ongmanager.exception.AppException;
import com.ongmanager.exception.ErrorCode;
import com.ongmanager.mapper.UserMapper;
import com.ongmanager.repository.*;
import com.ongmanager.security.JwtService;
import com.ongmanager.security.SecurityUtils;
import com.ongmanager.service.interfaces.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final EmailVerificationTokenRepository emailVerificationRepo;
    private final PasswordResetTokenRepository passwordResetRepo;
    private final RefreshTokenRepository refreshTokenRepo;

    @Override
    public UserResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw AppException.conflict("E-mail ja cadastrado");
        }
        Set<Role> roles = resolveRoles(req.getRoles());
        User user = User.builder()
            .name(req.getName())
            .email(req.getEmail())
            .password(passwordEncoder.encode(req.getPassword()))
            .phone(req.getPhone())
            .enabled(true).active(true)
            .roles(roles).build();
        return userMapper.toResponse(userRepository.save(user));
    }

    private Set<Role> resolveRoles(Set<String> roleNames) {
        Set<String> requested = (roleNames == null || roleNames.isEmpty())
            ? Set.of("VOLUNTEER") : roleNames;
        if (requested.contains("ADMIN") || requested.contains("DONOR")) {
            throw AppException.forbidden("Role nao pode ser atribuida no cadastro");
        }
        return requested.stream()
            .map(n -> roleRepository.findByName(n)
                .orElseThrow(() -> AppException.badRequest(ErrorCode.VALIDATION_ERROR, "Role invalida: " + n)))
            .collect(Collectors.toSet());
    }

    @Override
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
            .orElseThrow(() -> AppException.unauthorized("Credenciais invalidas"));
        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new AppException(ErrorCode.EMAIL_NOT_VERIFIED, HttpStatus.FORBIDDEN, "Confirme seu e-mail antes de entrar");
        }
        authManager.authenticate(new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        Map<String,Object> claims = new HashMap<>();
        claims.put("uid", user.getId());
        claims.put("roles", user.getRoles().stream().map(Role::getName).toList());
        String access = jwtService.generateAccessToken(user.getEmail(), claims);
        String refresh = jwtService.generateRefreshToken(user.getEmail());
        refreshTokenRepo.save(RefreshToken.builder().user(user).token(refresh)
            .expiresAt(LocalDateTime.now().plusDays(7)).revoked(false).build());
        return new AuthResponse(access, refresh, "Bearer", jwtService.getExpirationMs() / 1000,
            userMapper.toResponse(user));
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest req) {
        RefreshToken rt = refreshTokenRepo.findByToken(req.getRefreshToken())
            .orElseThrow(() -> AppException.unauthorized("Refresh token invalido"));
        if (rt.getRevoked() || rt.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw AppException.unauthorized("Refresh token expirado");
        }
        rt.setRevoked(true);
        refreshTokenRepo.save(rt);
        return buildAuthResponse(rt.getUser());
    }

    @Override
    public void requestPasswordReset(PasswordResetRequest req) {
        userRepository.findByEmail(req.getEmail()).ifPresent(user -> {
            PasswordResetToken token = PasswordResetToken.builder()
                .user(user).token(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusHours(1)).used(false).build();
            passwordResetRepo.save(token);
            emailService.sendPasswordReset(user.getEmail(), token.getToken());
        });
    }

    @Override
    public void confirmPasswordReset(PasswordResetConfirm req) {
        PasswordResetToken token = passwordResetRepo.findByToken(req.getToken())
            .orElseThrow(() -> AppException.badRequest(ErrorCode.TOKEN_INVALID, "Token invalido"));
        if (token.getUsed() || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw AppException.badRequest(ErrorCode.TOKEN_EXPIRED, "Token expirado");
        }
        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        token.setUsed(true);
        passwordResetRepo.save(token);
    }

    @Override
    public void verifyEmail(String token) {
        EmailVerificationToken t = emailVerificationRepo.findByToken(token)
            .orElseThrow(() -> AppException.badRequest(ErrorCode.TOKEN_INVALID, "Token invalido"));
        if (t.getUsed() || t.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw AppException.badRequest(ErrorCode.TOKEN_EXPIRED, "Token expirado");
        }
        User user = t.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        t.setUsed(true);
        emailVerificationRepo.save(t);
    }

    @Override
    public UserResponse updateProfile(UpdateProfileRequest req) {
        User user = SecurityUtils.currentUser();
        if (req.getName() != null) user.setName(req.getName());
        if (req.getPhone() != null) user.setPhone(req.getPhone());
        if (req.getProfileImageUrl() != null) user.setProfileImageUrl(req.getProfileImageUrl());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public void deactivateAccount() {
        User user = SecurityUtils.currentUser();
        user.setActive(false);
        userRepository.save(user);
    }
}
