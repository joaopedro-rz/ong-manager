package com.ongmanager.mapper;

import com.ongmanager.dto.response.CampaignResponse;
import com.ongmanager.dto.response.CampaignItemResponse;
import com.ongmanager.dto.response.CampaignUpdateResponse;
import com.ongmanager.entity.Campaign;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CampaignMapper {
    public CampaignResponse toResponse(Campaign c) {
        List<CampaignItemResponse> items = c.getItems().stream()
            .map(i -> new CampaignItemResponse(
                i.getId(), i.getName(), i.getCategory(),
                i.getQuantityNeeded(), i.getQuantityReceived(), i.getUnit()
            ))
            .toList();
        List<CampaignUpdateResponse> updates = c.getUpdates().stream()
            .map(u -> new CampaignUpdateResponse(
                u.getId(), u.getTitle(), u.getContent(), u.getCreatedAt()
            ))
            .toList();
        return new CampaignResponse(
            c.getId(), c.getNgo().getId(), c.getNgo().getName(),
            c.getTitle(), c.getDescription(), c.getFinancialGoal(), c.getRaisedAmount(),
            c.getStartDate(), c.getEndDate(), c.getCoverImageUrl(),
            c.getStatus().name(), c.getUrgent(),
            c.getNgo().getCategory() != null ? c.getNgo().getCategory().getName() : null,
            c.getNgo().getAddress() != null ? c.getNgo().getAddress().getCity() : null,
            c.getNgo().getAddress() != null ? c.getNgo().getAddress().getState() : null,
            items, updates
        );
    }
}
