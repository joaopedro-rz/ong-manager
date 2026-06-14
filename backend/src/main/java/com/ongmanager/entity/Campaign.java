package com.ongmanager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name="campaigns")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Campaign {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="ngo_id", nullable=false)
    private Ngo ngo;

    @Column(nullable=false, length=200) private String title;
    @Column(columnDefinition="TEXT") private String description;
    @Column(name="financial_goal", precision=14, scale=2) private BigDecimal financialGoal;
    @Builder.Default
    @Column(name="raised_amount", precision=14, scale=2, nullable=false)
    private BigDecimal raisedAmount = BigDecimal.ZERO;

    @Column(name="start_date", nullable=false) private LocalDate startDate;
    @Column(name="end_date") private LocalDate endDate;
    @Column(name="cover_image_url") private String coverImageUrl;

    @Enumerated(EnumType.STRING) @Builder.Default
    @Column(nullable=false, length=20)
    private CampaignStatus status = CampaignStatus.DRAFT;

    @Builder.Default private Boolean urgent = false;

    @OneToMany(mappedBy="campaign", cascade=CascadeType.ALL, orphanRemoval=true)
    @Builder.Default private List<CampaignItem> items = new ArrayList<>();

    @OneToMany(mappedBy="campaign", cascade=CascadeType.ALL, orphanRemoval=true)
    @Builder.Default private List<CampaignUpdate> updates = new ArrayList<>();

    @CreationTimestamp @Column(name="created_at", updatable=false) private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name="updated_at") private LocalDateTime updatedAt;
}
