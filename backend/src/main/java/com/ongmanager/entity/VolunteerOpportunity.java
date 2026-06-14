package com.ongmanager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name="volunteer_opportunities")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VolunteerOpportunity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="ngo_id", nullable=false)
    private Ngo ngo;
    @Column(nullable=false) private String title;
    @Column(columnDefinition="TEXT") private String description;
    @Builder.Default @Column(nullable=false) private Integer slots = 1;
    @Column(name="workload_hours") private Integer workloadHours;
    @Column(name="start_date") private LocalDate startDate;
    @Column(name="end_date") private LocalDate endDate;
    @Column(name="required_skills", columnDefinition="TEXT") private String requiredSkills;
    @Enumerated(EnumType.STRING) @Builder.Default
    @Column(nullable=false, length=20)
    private OpportunityStatus status = OpportunityStatus.OPEN;
    @CreationTimestamp @Column(name="created_at", updatable=false) private LocalDateTime createdAt;
}
