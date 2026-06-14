package com.ongmanager.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="roles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Role {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(unique=true, nullable=false, length=40)
    private String name;
}
