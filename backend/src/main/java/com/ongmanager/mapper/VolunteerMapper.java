package com.ongmanager.mapper;

import com.ongmanager.dto.response.VolunteerApplicationResponse;
import com.ongmanager.dto.response.VolunteerOpportunityResponse;
import com.ongmanager.dto.response.VolunteerScheduleResponse;
import com.ongmanager.entity.VolunteerApplication;
import com.ongmanager.entity.VolunteerOpportunity;
import com.ongmanager.entity.VolunteerSchedule;
import org.springframework.stereotype.Component;

@Component
public class VolunteerMapper {
    public VolunteerOpportunityResponse toResponse(VolunteerOpportunity o) {
        return new VolunteerOpportunityResponse(
            o.getId(), o.getNgo().getId(), o.getNgo().getName(),
            o.getTitle(), o.getDescription(), o.getSlots(), o.getWorkloadHours(),
            o.getStartDate(), o.getEndDate(), o.getRequiredSkills(), o.getStatus().name()
        );
    }

    public VolunteerApplicationResponse toResponse(VolunteerApplication a) {
        return new VolunteerApplicationResponse(
            a.getId(), a.getOpportunity().getId(), a.getOpportunity().getTitle(),
            a.getVolunteer().getId(), a.getVolunteer().getName(),
            a.getVolunteer().getEmail(), a.getVolunteer().getPhone(),
            a.getStatus().name(), a.getMotivation(), a.getAppliedAt(),
            a.getSkillsValidated(), a.getSkillsValidatedAt(), a.getSkillsNotes()
        );
    }

    public VolunteerScheduleResponse toResponse(VolunteerSchedule s) {
        return new VolunteerScheduleResponse(
            s.getId(), s.getApplication().getId(), s.getApplication().getOpportunity().getId(),
            s.getApplication().getVolunteer().getId(), s.getScheduledAt(), s.getDurationHours(), s.getNotes()
        );
    }
}
