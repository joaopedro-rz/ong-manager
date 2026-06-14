package com.ongmanager.dto.response;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDate;

@Getter @AllArgsConstructor
public class VolunteerOpportunityResponse {
    private Long id;
    private Long ngoId;
    private String ngoName;
    private String title;
    private String description;
    private Integer slots;
    private Integer workloadHours;
    private LocalDate startDate;
    private LocalDate endDate;
    private String requiredSkills;
    private String status;
}
