package com.ongmanager.controller;

import com.ongmanager.dto.response.ApiResponse;
import com.ongmanager.dto.response.DashboardSummary;
import com.ongmanager.dto.response.DonorDashboardSummary;
import com.ongmanager.dto.response.NgoDashboardSummary;
import com.ongmanager.service.interfaces.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {
    private final DashboardService service;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DashboardSummary>> admin(
        @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(service.adminSummary(from, to)));
        } catch (Exception e) {
            log.error("Erro no dashboard admin", e);
            throw e;
        }
    }

    @GetMapping("/ngo/{ngoId}")
    @PreAuthorize("hasAnyRole('ADMIN','ONG_MANAGER')")
    public ResponseEntity<ApiResponse<NgoDashboardSummary>> ngo(
        @PathVariable Long ngoId,
        @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.ok(service.ngoSummary(ngoId, from, to)));
    }

    @GetMapping("/donor")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DonorDashboardSummary>> donor(
        @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.ok(service.donorSummary(from, to)));
    }

    @GetMapping(value = "/ngo/{ngoId}/report/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','ONG_MANAGER')")
    public ResponseEntity<byte[]> ngoReportPdf(
        @PathVariable Long ngoId,
        @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=relatorio-ong-" + ngoId + ".pdf")
            .body(service.ngoReportPdf(ngoId, from, to));
    }

    @GetMapping(value = "/ngo/{ngoId}/report/csv", produces = "text/csv")
    @PreAuthorize("hasAnyRole('ADMIN','ONG_MANAGER')")
    public ResponseEntity<byte[]> ngoReportCsv(
        @PathVariable Long ngoId,
        @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=relatorio-ong-" + ngoId + ".csv")
            .body(service.ngoReportCsv(ngoId, from, to));
    }

    @GetMapping(value = "/admin/report/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> adminReportPdf(
        @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=relatorio-dashboard-admin.pdf")
            .body(service.adminReportPdf(from, to));
    }
}
