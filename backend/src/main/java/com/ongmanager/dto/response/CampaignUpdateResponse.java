package com.ongmanager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CampaignUpdateResponse {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
}
