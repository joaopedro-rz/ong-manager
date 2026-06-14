package com.ongmanager.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
@Data public class CampaignItemRequest {
    @NotBlank private String name;
    private String category;
    @NotNull @Positive private Integer quantityNeeded;
    private String unit;
}
