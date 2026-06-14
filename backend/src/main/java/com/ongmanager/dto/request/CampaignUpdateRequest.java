package com.ongmanager.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CampaignUpdateRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String content;
}
