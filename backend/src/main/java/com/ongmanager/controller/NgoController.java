package com.ongmanager.controller;

import com.ongmanager.dto.request.NgoRequest;
import com.ongmanager.dto.request.VolunteerNgoApplicationRequest;
import com.ongmanager.dto.response.ApiResponse;
import com.ongmanager.dto.response.NgoCategoryResponse;
import com.ongmanager.dto.response.NgoResponse;
import com.ongmanager.dto.response.VolunteerContactResponse;
import com.ongmanager.entity.NgoStatus;
import com.ongmanager.service.interfaces.NgoService;
import com.ongmanager.service.interfaces.VolunteerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ngos")
@RequiredArgsConstructor
public class NgoController {

    private final NgoService ngoService;
    private final VolunteerService volunteerService;

    @GetMapping("/public")
    public ResponseEntity<ApiResponse<Page<NgoResponse>>> publicList(
        @RequestParam(required=false) String q,
        @RequestParam(required=false) Long categoryId,
        Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(ngoService.searchPublic(q, categoryId, pageable)));
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<ApiResponse<NgoResponse>> publicGet(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(ngoService.getPublicActive(id)));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<NgoCategoryResponse>>> categories() {
        return ResponseEntity.ok(ApiResponse.ok(ngoService.categories()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ONG_MANAGER')")
    public ResponseEntity<ApiResponse<NgoResponse>> create(@Valid @RequestBody NgoRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(ngoService.create(req), "ONG criada"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ONG_MANAGER')")
    public ResponseEntity<ApiResponse<NgoResponse>> update(@PathVariable Long id, @Valid @RequestBody NgoRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(ngoService.update(id, req)));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN','ONG_MANAGER')")
    public ResponseEntity<ApiResponse<Page<NgoResponse>>> myNgos(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(ngoService.myNgos(pageable)));
    }

    @GetMapping("/moderation")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<NgoResponse>>> moderationList(
        @RequestParam(required=false) String q,
        @RequestParam(required=false) Long categoryId,
        @RequestParam(required=false) NgoStatus status,
        Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(ngoService.moderationList(q, categoryId, status, pageable)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NgoResponse>> changeStatus(
        @PathVariable Long id, @RequestParam NgoStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(ngoService.changeStatus(id, status)));
    }

    @PostMapping("/{id}/logo")
    @PreAuthorize("hasAnyRole('ADMIN','ONG_MANAGER')")
    public ResponseEntity<ApiResponse<NgoResponse>> uploadLogo(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.ok(ngoService.uploadLogo(id, file), "Logo atualizada"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ONG_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        ngoService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Excluido"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NgoResponse>>> list(
        @RequestParam(required=false) Boolean allowVolunteers,
        @RequestParam(required=false) String search,
        @RequestParam(required=false) String city,
        @RequestParam(required=false) String state,
        @RequestParam(required=false) Long categoryId,
        @RequestParam(required=false) NgoStatus status,
        Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
            ngoService.searchPublicWithFilters(status, allowVolunteers, search, city, state, categoryId, pageable)
        ));
    }

    @GetMapping("/{id}/volunteers")
    public ResponseEntity<ApiResponse<java.util.List<VolunteerContactResponse>>> volunteers(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(volunteerService.listNgoVolunteers(id)));
    }

    @GetMapping("/{id}/volunteers/applications")
    @PreAuthorize("hasAnyRole('ADMIN','ONG_MANAGER')")
    public ResponseEntity<ApiResponse<java.util.List<com.ongmanager.dto.response.VolunteerApplicationResponse>>> volunteerApplications(
        @PathVariable Long id,
        @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.ok(volunteerService.listNgoApplications(id, status)));
    }

    @PatchMapping("/{id}/volunteers/{applicationId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','ONG_MANAGER')")
    public ResponseEntity<ApiResponse<?>> approveVolunteer(
        @PathVariable Long id,
        @PathVariable Long applicationId) {
        return ResponseEntity.ok(ApiResponse.ok(volunteerService.review(applicationId, "APPROVED"), "Aprovado"));
    }

    @PatchMapping("/{id}/volunteers/{applicationId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','ONG_MANAGER')")
    public ResponseEntity<ApiResponse<?>> rejectVolunteer(
        @PathVariable Long id,
        @PathVariable Long applicationId) {
        return ResponseEntity.ok(ApiResponse.ok(volunteerService.review(applicationId, "REJECTED"), "Rejeitado"));
    }

    @PostMapping("/{id}/volunteers")
    @PreAuthorize("hasAnyRole('VOLUNTEER','ADMIN')")
    public ResponseEntity<ApiResponse<?>> applyVolunteer(@PathVariable Long id, @Valid @RequestBody VolunteerNgoApplicationRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(volunteerService.applyToNgo(id, req), "Inscricao enviada"));
    }
}
