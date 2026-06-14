package com.ongmanager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity @Table(name="ngos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Ngo {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;

    @Column(nullable=false, length=200) private String name;
    @Column(nullable=false, unique=true, length=20) private String cnpj;
    @Column(columnDefinition="TEXT") private String description;
    private String phone;
    private String website;
    @Column(name="social_media") private String socialMedia;
    @Column(name="logo_url") private String logoUrl;
    @Column(columnDefinition="TEXT") private String certifications;

    @Builder.Default
    @Column(name="allow_volunteers", nullable=false)
    private Boolean allowVolunteers = false;

    @Builder.Default
    @Column(name="volunteer_slots", nullable=false)
    private Integer volunteerSlots = 0;

    @Enumerated(EnumType.STRING) @Builder.Default
    @Column(nullable=false, length=20)
    private NgoStatus status = NgoStatus.PENDING;

    @OneToOne(cascade=CascadeType.ALL) @JoinColumn(name="address_id")
    private Address address;

    @ManyToOne @JoinColumn(name="category_id")
    private NgoCategory category;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="manager_id", nullable=false)
    private User manager;

    @CreationTimestamp @Column(name="created_at", updatable=false) private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name="updated_at") private LocalDateTime updatedAt;
}
