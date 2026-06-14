package com.ongmanager.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class RegisterRequest {
    @NotBlank @Size(max=150) private String name;
    @NotBlank @Email @Size(max=180) private String email;
    @NotBlank @Size(min=8, max=100) private String password;
    private String phone;
    private Set<String> roles;
}
