package com.ongmanager.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="campaign_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CampaignItem {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="campaign_id", nullable=false)
    private Campaign campaign;
    @Column(nullable=false) private String name;
    private String category;
    @Column(name="quantity_needed", nullable=false) private Integer quantityNeeded;
    @Builder.Default
    @Column(name="quantity_received", nullable=false) private Integer quantityReceived = 0;
    @Builder.Default @Column(nullable=false) private String unit = "un";
}
