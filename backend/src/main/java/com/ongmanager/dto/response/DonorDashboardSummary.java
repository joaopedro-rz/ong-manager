package com.ongmanager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class DonorDashboardSummary {
    private Long donorId;
    private String donorName;
    private long totalConfirmedDonations;
    private BigDecimal totalDonated;
    private long supportedNgos;
    private List<ChartPoint> monthlyDonations;
}

