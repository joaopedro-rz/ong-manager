package com.ongmanager.dto.response;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @AllArgsConstructor
public class DonationResponse {
    private Long id;
    private Long donorId;
    private String donorName;
    private Long campaignId;
    private String campaignTitle;
    private String type;
    private String status;
    private LocalDateTime donationDate;
    private BigDecimal amount;
    private String paymentMethod;
    private String itemName;
    private Integer itemQuantity;
    private String itemUnit;
    private LocalDate deliveryDate;
}
