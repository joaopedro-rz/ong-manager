package com.ongmanager.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VolunteerScheduleRequest {
    @NotNull
    private Long applicationId;
    @NotNull
    private LocalDateTime scheduledAt;
    @NotNull
    private Integer durationHours;
    private String notes;
}

