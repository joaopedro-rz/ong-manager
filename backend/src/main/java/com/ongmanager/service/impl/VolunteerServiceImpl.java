package com.ongmanager.service.impl;

import com.ongmanager.dto.request.VolunteerApplicationRequest;
import com.ongmanager.dto.request.VolunteerOpportunityRequest;
import com.ongmanager.dto.request.VolunteerScheduleRequest;
import com.ongmanager.dto.request.VolunteerNgoApplicationRequest;
import com.ongmanager.dto.response.VolunteerApplicationResponse;
import com.ongmanager.dto.response.VolunteerOpportunityResponse;
import com.ongmanager.dto.response.VolunteerScheduleResponse;
import com.ongmanager.dto.response.VolunteerContactResponse;
import com.ongmanager.entity.*;
import com.ongmanager.exception.AppException;
import com.ongmanager.exception.ErrorCode;
import com.ongmanager.mapper.VolunteerMapper;
import com.ongmanager.repository.*;
import com.ongmanager.security.SecurityUtils;
import com.ongmanager.service.interfaces.VolunteerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VolunteerServiceImpl implements VolunteerService {

    private final VolunteerOpportunityRepository opportunityRepo;
    private final VolunteerApplicationRepository applicationRepo;
    private final VolunteerScheduleRepository scheduleRepo;
    private final NgoRepository ngoRepo;
    private final VolunteerMapper mapper;

    @Override
    public VolunteerOpportunityResponse createOpportunity(VolunteerOpportunityRequest req) {
        Ngo ngo = ngoRepo.findById(req.getNgoId())
            .orElseThrow(() -> AppException.notFound("ONG nao encontrada"));
        ensureManager(ngo);
        VolunteerOpportunity o = VolunteerOpportunity.builder()
            .ngo(ngo).title(req.getTitle()).description(req.getDescription())
            .slots(req.getSlots()).workloadHours(req.getWorkloadHours())
            .startDate(req.getStartDate()).endDate(req.getEndDate())
            .requiredSkills(req.getRequiredSkills())
            .status(OpportunityStatus.OPEN).build();
        return mapper.toResponse(opportunityRepo.save(o));
    }

    @Override
    public Page<VolunteerOpportunityResponse> listOpportunities(Long ngoId, Pageable pageable) {
        Page<VolunteerOpportunity> page = (ngoId != null)
            ? opportunityRepo.findByNgo_Id(ngoId, pageable)
            : opportunityRepo.findAll(pageable);
        return page.map(mapper::toResponse);
    }

    @Override
    public VolunteerApplicationResponse apply(VolunteerApplicationRequest req) {
        VolunteerOpportunity o = opportunityRepo.findById(req.getOpportunityId())
            .orElseThrow(() -> AppException.notFound("Vaga nao encontrada"));
        Long uid = SecurityUtils.currentUser().getId();
        if (applicationRepo.existsByOpportunity_IdAndVolunteer_Id(o.getId(), uid)) {
            throw AppException.conflict("Voce ja se inscreveu nesta vaga");
        }
        VolunteerApplication a = VolunteerApplication.builder()
            .opportunity(o).volunteer(SecurityUtils.currentUser())
            .motivation(req.getMotivation()).status(ApplicationStatus.PENDING).build();
        return mapper.toResponse(applicationRepo.save(a));
    }

    @Override
    public VolunteerApplicationResponse review(Long applicationId, String status) {
        VolunteerApplication a = applicationRepo.findById(applicationId)
            .orElseThrow(() -> AppException.notFound("Inscricao nao encontrada"));
        ensureManager(a.getOpportunity().getNgo());
        a.setStatus(ApplicationStatus.valueOf(status));
        a.setReviewedAt(LocalDateTime.now());
        return mapper.toResponse(applicationRepo.save(a));
    }

    @Override
    public VolunteerApplicationResponse validateSkills(Long applicationId, boolean validated, String notes) {
        VolunteerApplication a = applicationRepo.findById(applicationId)
            .orElseThrow(() -> AppException.notFound("Inscricao nao encontrada"));
        ensureManager(a.getOpportunity().getNgo());
        a.setSkillsValidated(validated);
        a.setSkillsValidatedAt(LocalDateTime.now());
        a.setSkillsNotes(notes);
        return mapper.toResponse(applicationRepo.save(a));
    }

    @Override
    public Page<VolunteerApplicationResponse> myApplications(Pageable pageable) {
        Long uid = SecurityUtils.currentUser().getId();
        return applicationRepo.findByVolunteer_Id(uid, pageable).map(mapper::toResponse);
    }

    @Override
    public Page<VolunteerApplicationResponse> myHistory(Pageable pageable) {
        Long uid = SecurityUtils.currentUser().getId();
        return applicationRepo.findByVolunteer_IdAndStatus(uid, ApplicationStatus.APPROVED, pageable)
            .map(mapper::toResponse);
    }

    @Override
    public Page<VolunteerApplicationResponse> applicationsByOpportunity(Long opportunityId, Pageable pageable) {
        return applicationRepo.findByOpportunity_Id(opportunityId, pageable).map(mapper::toResponse);
    }

    @Override
    public VolunteerScheduleResponse createSchedule(VolunteerScheduleRequest req) {
        VolunteerApplication a = applicationRepo.findById(req.getApplicationId())
            .orElseThrow(() -> AppException.notFound("Inscricao nao encontrada"));
        ensureManager(a.getOpportunity().getNgo());
        if (a.getStatus() != ApplicationStatus.APPROVED) {
            throw AppException.badRequest(ErrorCode.VALIDATION_ERROR, "Inscricao precisa estar aprovada");
        }
        VolunteerSchedule schedule = VolunteerSchedule.builder()
            .application(a)
            .scheduledAt(req.getScheduledAt())
            .durationHours(req.getDurationHours())
            .notes(req.getNotes())
            .build();
        return mapper.toResponse(scheduleRepo.save(schedule));
    }

    @Override
    public Page<VolunteerScheduleResponse> scheduleByOpportunity(Long opportunityId, Pageable pageable) {
        return scheduleRepo.findByApplication_Opportunity_Id(opportunityId, pageable)
            .map(mapper::toResponse);
    }

    @Override
    public Page<VolunteerScheduleResponse> mySchedule(Pageable pageable) {
        Long uid = SecurityUtils.currentUser().getId();
        return scheduleRepo.findByApplication_Volunteer_Id(uid, pageable)
            .map(mapper::toResponse);
    }

    @Override
    public VolunteerApplicationResponse applyToNgo(Long ngoId, VolunteerNgoApplicationRequest req) {
        Ngo ngo = ngoRepo.findById(ngoId)
            .orElseThrow(() -> AppException.notFound("ONG nao encontrada"));
        if (!Boolean.TRUE.equals(ngo.getAllowVolunteers())) {
            throw AppException.badRequest(ErrorCode.VALIDATION_ERROR, "ONG nao aceita voluntarios");
        }
        VolunteerOpportunity opportunity = opportunityRepo
            .findFirstByNgo_IdAndStatus(ngoId, OpportunityStatus.OPEN)
            .orElseGet(() -> opportunityRepo.save(VolunteerOpportunity.builder()
                .ngo(ngo)
                .title("Voluntariado geral")
                .description("Inscricoes gerais para voluntariado")
                .slots(ngo.getVolunteerSlots() != null ? ngo.getVolunteerSlots() : 1)
                .status(OpportunityStatus.OPEN)
                .build()));
        Long uid = SecurityUtils.currentUser().getId();
        if (applicationRepo.existsByOpportunity_IdAndVolunteer_Id(opportunity.getId(), uid)) {
            throw AppException.conflict("Voce ja se inscreveu nesta ONG");
        }
        VolunteerApplication a = VolunteerApplication.builder()
            .opportunity(opportunity)
            .volunteer(SecurityUtils.currentUser())
            .motivation(req.getMessage())
            .status(ApplicationStatus.PENDING)
            .build();
        return mapper.toResponse(applicationRepo.save(a));
    }

    @Override
    public List<VolunteerContactResponse> listNgoVolunteers(Long ngoId) {
        List<User> volunteers = applicationRepo.findVolunteersByNgoIdAndStatus(ngoId, ApplicationStatus.APPROVED);
        return volunteers.stream()
            .map(v -> new VolunteerContactResponse(v.getId(), v.getName(), v.getEmail(), v.getPhone()))
            .toList();
    }

    @Override
    public List<VolunteerApplicationResponse> listNgoApplications(Long ngoId, String status) {
        Ngo ngo = ngoRepo.findById(ngoId)
            .orElseThrow(() -> AppException.notFound("ONG nao encontrada"));
        ensureManager(ngo);
        List<VolunteerApplication> apps = (status == null || status.isBlank())
            ? applicationRepo.findByOpportunity_Ngo_Id(ngoId)
            : applicationRepo.findByOpportunity_Ngo_IdAndStatus(ngoId, ApplicationStatus.valueOf(status));
        return apps.stream().map(mapper::toResponse).toList();
    }

    private void ensureManager(Ngo ngo) {
        Long uid = SecurityUtils.currentUser().getId();
        if (!SecurityUtils.hasRole("ADMIN") && !ngo.getManager().getId().equals(uid)) {
            throw AppException.forbidden("Sem permissao sobre esta ONG");
        }
    }
}
