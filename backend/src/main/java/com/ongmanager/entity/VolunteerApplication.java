package com.ongmanager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity @Table(name="volunteer_applications",
    uniqueConstraints=@UniqueConstraint(columnNames={"opportunity_id","volunteer_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VolunteerApplication {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="opportunity_id", nullable=false)
    private VolunteerOpportunity opportunity;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="volunteer_id", nullable=false)
    private User volunteer;
    @Enumerated(EnumType.STRING) @Builder.Default
    @Column(nullable=false, length=20)
    private ApplicationStatus status = ApplicationStatus.PENDING;
    @Column(columnDefinition="TEXT") private String motivation;
    @Builder.Default
    @Column(name="skills_validated", nullable=false)
    private Boolean skillsValidated = false;
    @Column(name="skills_validated_at")
    private LocalDateTime skillsValidatedAt;
    @Column(name="skills_notes", length=255)
    private String skillsNotes;
    @CreationTimestamp @Column(name="applied_at", updatable=false) private LocalDateTime appliedAt;
    @Column(name="reviewed_at") private LocalDateTime reviewedAt;
}
