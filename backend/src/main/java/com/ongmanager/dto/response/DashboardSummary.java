package com.ongmanager.dto.response;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.math.BigDecimal;
import java.util.List;

@Getter @AllArgsConstructor
public class DashboardSummary {
    private long totalNgos;
    private long totalActiveCampaigns;
    private long totalConfirmedDonations;
    private BigDecimal totalRaised;
    private long totalVolunteers;
    private List<ChartPoint> monthlyDonations;
}
