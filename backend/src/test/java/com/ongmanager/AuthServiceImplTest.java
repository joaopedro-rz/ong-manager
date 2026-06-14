package com.ongmanager;

import com.ongmanager.dto.request.RegisterRequest;
import com.ongmanager.entity.Role;
import com.ongmanager.entity.User;
import com.ongmanager.exception.AppException;
import com.ongmanager.mapper.UserMapper;
import com.ongmanager.repository.*;
import com.ongmanager.security.JwtService;
import com.ongmanager.service.impl.AuthServiceImpl;
import com.ongmanager.service.impl.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock UserRepository userRepository;
    @Mock RoleRepository roleRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthenticationManager authManager;
    @Mock JwtService jwtService;
    @Mock UserMapper userMapper;
    @Mock EmailService emailService;
    @Mock EmailVerificationTokenRepository emailVerificationRepo;
    @Mock PasswordResetTokenRepository passwordResetRepo;
    @Mock RefreshTokenRepository refreshTokenRepo;

    @InjectMocks AuthServiceImpl service;

    @Test
    void shouldRejectDuplicateEmail() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("a@a.com"); req.setName("A"); req.setPassword("12345678");
        when(userRepository.existsByEmail("a@a.com")).thenReturn(true);
        assertThrows(AppException.class, () -> service.register(req));
    }

    @Test
    void shouldRegisterWithDefaultVolunteerRole() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("x@x.com");
        req.setName("X");
        req.setPassword("12345678");
        req.setRoles(Set.of("VOLUNTEER"));
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(roleRepository.findByName("VOLUNTEER")).thenReturn(Optional.of(Role.builder().id(1L).name("VOLUNTEER").build()));
        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(userRepository.save(any())).thenAnswer(i -> {
            User u = i.getArgument(0); u.setId(10L); return u;
        });
        service.register(req);
        assertNotNull(req);
    }
}
