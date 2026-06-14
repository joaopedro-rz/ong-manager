package com.ongmanager.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="addresses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Address {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private String street;
    private String number;
    private String complement;
    private String district;
    private String city;
    private String state;
    @Column(name="zip_code") private String zipCode;
    @Builder.Default private String country = "Brasil";
}
