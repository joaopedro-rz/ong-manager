package com.ongmanager.dto.response;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class NgoResponse {
    private Long id;
    private String name;
    private String cnpj;
    private String description;
    private String phone;
    private String website;
    private String socialMedia;
    private String logoUrl;
    private String status;
    private String categoryName;
    private String city;
    private String state;
    private Long managerId;
    private Boolean allowVolunteers;
    private Integer volunteerSlots;
}
