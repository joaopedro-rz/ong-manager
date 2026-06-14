package com.ongmanager.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class VolunteerOpportunityRequest {
    @NotNull private Long ngoId;
    @NotBlank private String title;
    private String description;
    @NotNull private Integer slots;
    private Integer workloadHours;
    private LocalDate startDate;
    private LocalDate endDate;
    private String requiredSkills;
}
