package com.ongmanager.mapper;

import com.ongmanager.dto.response.DonationResponse;
import com.ongmanager.entity.Donation;
import org.springframework.stereotype.Component;

@Component
public class DonationMapper {
    public DonationResponse toResponse(Donation d) {
        return new DonationResponse(
            d.getId(),
            d.getDonor().getId(), d.getDonor().getName(),
            d.getCampaign().getId(), d.getCampaign().getTitle(),
            d.getType().name(), d.getStatus().name(),
            d.getDonationDate(), d.getAmount(), d.getPaymentMethod(),
            d.getItemName(), d.getItemQuantity(), d.getItemUnit(), d.getDeliveryDate()
        );
    }
}
