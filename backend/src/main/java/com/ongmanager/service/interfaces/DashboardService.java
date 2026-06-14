package com.ongmanager.service.interfaces;

import com.ongmanager.dto.response.DashboardSummary;
import com.ongmanager.dto.response.DonorDashboardSummary;
import com.ongmanager.dto.response.NgoDashboardSummary;
import java.time.LocalDate;

public interface DashboardService {
    DashboardSummary adminSummary(LocalDate from, LocalDate to);
    NgoDashboardSummary ngoSummary(Long ngoId, LocalDate from, LocalDate to);
    DonorDashboardSummary donorSummary(LocalDate from, LocalDate to);
    byte[] adminReportPdf(LocalDate from, LocalDate to);
    byte[] ngoReportPdf(Long ngoId, LocalDate from, LocalDate to);
    byte[] ngoReportCsv(Long ngoId, LocalDate from, LocalDate to);
}
