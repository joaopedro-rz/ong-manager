package com.ongmanager.controller;

import com.ongmanager.dto.request.VolunteerApplicationRequest;
import com.ongmanager.dto.request.VolunteerOpportunityRequest;
import com.ongmanager.dto.request.VolunteerScheduleRequest;
import com.ongmanager.dto.response.ApiResponse;
import com.ongmanager.dto.response.VolunteerApplicationResponse;
import com.ongmanager.dto.response.VolunteerOpportunityResponse;
import com.ongmanager.dto.response.VolunteerScheduleResponse;
import com.ongmanager.service.interfaces.VolunteerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/volunteer-opportunities")
@RequiredArgsConstructor
public class VolunteerController {

    private final VolunteerService service;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<VolunteerOpportunityResponse>>> list(
        @RequestParam(required=false) Long ngoId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(service.listOpportunities(ngoId, pageable)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ONG_MANAGER')")
    public ResponseEntity<ApiResponse<VolunteerOpportunityResponse>> create(@Valid @RequestBody VolunteerOpportunityRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(service.createOpportunity(req), "Vaga criada"));
    }

    @PostMapping("/applications")
    @PreAuthorize("hasAnyRole('VOLUNTEER','ADMIN')")
    public ResponseEntity<ApiResponse<VolunteerApplicationResponse>> apply(@Valid @RequestBody VolunteerApplicationRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(service.apply(req), "Inscricao enviada"));
    }

    @PatchMapping("/applications/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ONG_MANAGER')")
    public ResponseEntity<ApiResponse<VolunteerApplicationResponse>> review(
        @PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.ok(service.review(id, status)));
    }

    @PatchMapping("/applications/{id}/skills")
    @PreAuthorize("hasAnyRole('ADMIN','ONG_MANAGER')")
    public ResponseEntity<ApiResponse<VolunteerApplicationResponse>> validateSkills(
        @PathVariable Long id,
        @RequestParam boolean validated,
        @RequestParam(required=false) String notes) {
        return ResponseEntity.ok(ApiResponse.ok(service.validateSkills(id, validated, notes)));
    }

    @GetMapping("/applications/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<VolunteerApplicationResponse>>> mine(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(service.myApplications(pageable)));
    }

    @GetMapping("/applications/history/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<VolunteerApplicationResponse>>> myHistory(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(service.myHistory(pageable)));
    }

    @GetMapping("/{id}/applications")
    @PreAuthorize("hasAnyRole('ADMIN','ONG_MANAGER')")
    public ResponseEntity<ApiResponse<Page<VolunteerApplicationResponse>>> byOpportunity(
        @PathVariable Long id, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(service.applicationsByOpportunity(id, pageable)));
    }

    @PostMapping("/schedule")
    @PreAuthorize("hasAnyRole('ADMIN','ONG_MANAGER')")
    public ResponseEntity<ApiResponse<VolunteerScheduleResponse>> createSchedule(
        @Valid @RequestBody VolunteerScheduleRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(service.createSchedule(req), "Agenda criada"));
    }

    @GetMapping("/schedule/opportunity/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ONG_MANAGER')")
    public ResponseEntity<ApiResponse<Page<VolunteerScheduleResponse>>> scheduleByOpportunity(
        @PathVariable Long id, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(service.scheduleByOpportunity(id, pageable)));
    }

    @GetMapping("/schedule/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<VolunteerScheduleResponse>>> mySchedule(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(service.mySchedule(pageable)));
    }
}
