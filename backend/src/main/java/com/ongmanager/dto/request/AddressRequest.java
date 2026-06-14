package com.ongmanager.dto.request;
import lombok.Data;
@Data public class AddressRequest {
    private String street;
    private String number;
    private String complement;
    private String district;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}
