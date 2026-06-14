package com.ongmanager.dto.request;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DonationRequest {
    @NotNull private Long campaignId;
    @NotNull private String type; // FINANCIAL | MATERIAL
    private BigDecimal amount;
    private String paymentMethod;
    private String receiptUrl;
    private String itemName;
    private Integer itemQuantity;
    private String itemUnit;
    private LocalDate deliveryDate;
    private String notes;
    private Boolean demoConfirm;
}
