package com.ongmanager.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CampaignRequest {
    @NotNull private Long ngoId;
    @NotBlank private String title;
    @NotBlank private String description;
    @NotNull @PositiveOrZero private BigDecimal financialGoal;
    @NotNull private LocalDate startDate;
    @NotNull private LocalDate endDate;
    private String coverImageUrl;
    @NotEmpty private String status;
    private Boolean urgent;
}
