package com.ongmanager.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Data public class PasswordResetConfirm {
    @NotBlank private String token;
    @NotBlank @Size(min=8) private String newPassword;
}
