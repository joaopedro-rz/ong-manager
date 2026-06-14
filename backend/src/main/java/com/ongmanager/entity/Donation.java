package com.ongmanager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name="donations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Donation {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="donor_id", nullable=false)
    private User donor;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="campaign_id", nullable=false)
    private Campaign campaign;

    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20)
    private DonationType type;

    @Enumerated(EnumType.STRING) @Builder.Default
    @Column(nullable=false, length=20)
    private DonationStatus status = DonationStatus.PENDING;

    @Builder.Default
    @Column(name="donation_date", nullable=false)
    private LocalDateTime donationDate = LocalDateTime.now();

    @Column(precision=14, scale=2) private BigDecimal amount;
    @Column(name="payment_method") private String paymentMethod;
    @Column(name="receipt_url") private String receiptUrl;

    @Column(name="item_name") private String itemName;
    @Column(name="item_quantity") private Integer itemQuantity;
    @Column(name="item_unit") private String itemUnit;
    @Column(name="delivery_date") private LocalDate deliveryDate;

    @Column(name="confirmed_at") private LocalDateTime confirmedAt;
    @Column(columnDefinition="TEXT") private String notes;
}
