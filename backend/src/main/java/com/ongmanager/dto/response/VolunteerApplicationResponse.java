package com.ongmanager.dto.response;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter @AllArgsConstructor
public class VolunteerApplicationResponse {
    private Long id;
    private Long opportunityId;
    private String opportunityTitle;
    private Long volunteerId;
    private String volunteerName;
    private String volunteerEmail;
    private String volunteerPhone;
    private String status;
    private String motivation;
    private LocalDateTime appliedAt;
    private Boolean skillsValidated;
    private LocalDateTime skillsValidatedAt;
    private String skillsNotes;
}
