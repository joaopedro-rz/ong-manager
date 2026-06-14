package com.ongmanager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity @Table(name="users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=150) private String name;
    @Column(nullable=false, unique=true, length=180) private String email;
    @Column(nullable=false) private String password;
    private String phone;
    @Column(name="profile_image_url") private String profileImageUrl;

    @Builder.Default private Boolean enabled = false;
    @Builder.Default private Boolean active = true;

    @CreationTimestamp @Column(name="created_at", updatable=false)
    private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name="updated_at")
    private LocalDateTime updatedAt;

    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(name="user_roles",
        joinColumns=@JoinColumn(name="user_id"),
        inverseJoinColumns=@JoinColumn(name="role_id"))
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
}
