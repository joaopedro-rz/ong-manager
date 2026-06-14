package com.ongmanager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity @Table(name="campaign_updates")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CampaignUpdate {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="campaign_id", nullable=false)
    private Campaign campaign;
    @Column(nullable=false) private String title;
    @Column(nullable=false, columnDefinition="TEXT") private String content;
    @CreationTimestamp @Column(name="created_at", updatable=false) private LocalDateTime createdAt;
}
