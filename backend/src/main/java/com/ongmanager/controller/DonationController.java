package com.ongmanager.controller;

import com.ongmanager.dto.request.DonationRequest;
import com.ongmanager.dto.response.ApiResponse;
import com.ongmanager.dto.response.DonationResponse;
import com.ongmanager.service.interfaces.DonationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/donations")
@RequiredArgsConstructor
public class DonationController {

    private final DonationService service;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DonationResponse>> create(@Valid @RequestBody DonationRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(service.create(req), "Doacao registrada"));
    }

    @PostMapping("/financial")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<DonationResponse>> createFinancial(@RequestBody DonationRequest req) {
        req.setType("FINANCIAL");
        return ResponseEntity.ok(ApiResponse.ok(service.create(req), "Doacao registrada"));
    }

    @PostMapping("/material")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DonationResponse>> createMaterial(@RequestBody DonationRequest req) {
        req.setType("MATERIAL");
        return ResponseEntity.ok(ApiResponse.ok(service.create(req), "Doacao registrada"));
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN','ONG_MANAGER')")
    public ResponseEntity<ApiResponse<DonationResponse>> confirm(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(service.confirm(id)));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','ONG_MANAGER')")
    public ResponseEntity<ApiResponse<DonationResponse>> reject(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(service.reject(id)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DonationResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(service.get(id)));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<DonationResponse>>> mine(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(service.myDonations(pageable)));
    }

    @GetMapping("/by-campaign/{campaignId}")
    @PreAuthorize("hasAnyRole('ADMIN','ONG_MANAGER')")
    public ResponseEntity<ApiResponse<Page<DonationResponse>>> byCampaign(@PathVariable Long campaignId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(service.byCampaign(campaignId, pageable)));
    }

    @GetMapping("/by-ngo/{ngoId}")
    @PreAuthorize("hasAnyRole('ADMIN','ONG_MANAGER')")
    public ResponseEntity<ApiResponse<Page<DonationResponse>>> byNgo(@PathVariable Long ngoId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(service.byNgo(ngoId, pageable)));
    }

    @GetMapping(value="/{id}/receipt", produces=MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> receipt(@PathVariable Long id) {
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=recibo-" + id + ".pdf")
            .body(service.receiptPdf(id));
    }
}
