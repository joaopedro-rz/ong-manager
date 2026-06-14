package com.ongmanager.service.interfaces;

import com.ongmanager.dto.request.VolunteerApplicationRequest;
import com.ongmanager.dto.request.VolunteerOpportunityRequest;
import com.ongmanager.dto.request.VolunteerScheduleRequest;
import com.ongmanager.dto.request.VolunteerNgoApplicationRequest;
import com.ongmanager.dto.response.VolunteerApplicationResponse;
import com.ongmanager.dto.response.VolunteerOpportunityResponse;
import com.ongmanager.dto.response.VolunteerScheduleResponse;
import com.ongmanager.dto.response.VolunteerContactResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VolunteerService {
    VolunteerOpportunityResponse createOpportunity(VolunteerOpportunityRequest req);
    Page<VolunteerOpportunityResponse> listOpportunities(Long ngoId, Pageable pageable);
    VolunteerApplicationResponse apply(VolunteerApplicationRequest req);
    VolunteerApplicationResponse review(Long applicationId, String status);
    VolunteerApplicationResponse validateSkills(Long applicationId, boolean validated, String notes);
    Page<VolunteerApplicationResponse> myApplications(Pageable pageable);
    Page<VolunteerApplicationResponse> myHistory(Pageable pageable);
    Page<VolunteerApplicationResponse> applicationsByOpportunity(Long opportunityId, Pageable pageable);
    VolunteerScheduleResponse createSchedule(VolunteerScheduleRequest req);
    Page<VolunteerScheduleResponse> scheduleByOpportunity(Long opportunityId, Pageable pageable);
    Page<VolunteerScheduleResponse> mySchedule(Pageable pageable);
    VolunteerApplicationResponse applyToNgo(Long ngoId, VolunteerNgoApplicationRequest req);
    java.util.List<VolunteerContactResponse> listNgoVolunteers(Long ngoId);
    java.util.List<VolunteerApplicationResponse> listNgoApplications(Long ngoId, String status);
}
