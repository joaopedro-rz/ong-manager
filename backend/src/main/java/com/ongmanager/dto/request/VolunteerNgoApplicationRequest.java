package com.ongmanager.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VolunteerNgoApplicationRequest {
    @NotBlank private String name;
    @NotBlank private String email;
    @NotBlank private String phone;
    private String message;
}

