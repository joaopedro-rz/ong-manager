package com.ongmanager.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NgoRequest {
    @NotBlank @Size(max=200) private String name;
    @NotBlank @Size(max=20) private String cnpj;
    private String description;
    private String phone;
    private String website;
    private String socialMedia;
    private String logoUrl;
    private String certifications;
    private Long categoryId;
    private AddressRequest address;
    private Boolean allowVolunteers;
    private Integer volunteerSlots;
}
