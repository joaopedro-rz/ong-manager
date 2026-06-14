package com.ongmanager.dto.response;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter @AllArgsConstructor
public class CampaignResponse {
    private Long id;
    private Long ngoId;
    private String ngoName;
    private String title;
    private String description;
    private BigDecimal financialGoal;
    private BigDecimal raisedAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private String coverImageUrl;
    private String status;
    private Boolean urgent;
    private String categoryName;
    private String ngoCity;
    private String ngoState;
    private List<CampaignItemResponse> items;
    private List<CampaignUpdateResponse> updates;
}
