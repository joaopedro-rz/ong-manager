package com.ongmanager.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="ngo_categories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NgoCategory {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(unique=true, nullable=false) private String name;
}
