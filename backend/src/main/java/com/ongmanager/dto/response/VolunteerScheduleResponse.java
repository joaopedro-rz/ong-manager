package com.ongmanager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class VolunteerScheduleResponse {
    private Long id;
    private Long applicationId;
    private Long opportunityId;
    private Long volunteerId;
    private LocalDateTime scheduledAt;
    private Integer durationHours;
    private String notes;
}

