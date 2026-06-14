package com.ongmanager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class NgoDashboardSummary {
    private Long ngoId;
    private String ngoName;
    private long totalCampaigns;
    private long activeCampaigns;
    private long totalConfirmedDonations;
    private long totalMaterialDonations;
    private BigDecimal totalRaised;
    private long activeVolunteers;
    private List<ChartPoint> monthlyDonations;
    private List<DonorRank> topDonors;
}

