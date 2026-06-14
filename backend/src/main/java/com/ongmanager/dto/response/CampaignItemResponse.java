package com.ongmanager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CampaignItemResponse {
    private Long id;
    private String name;
    private String category;
    private Integer quantityNeeded;
    private Integer quantityReceived;
    private String unit;
}
