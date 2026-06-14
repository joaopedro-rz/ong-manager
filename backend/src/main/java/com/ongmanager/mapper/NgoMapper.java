package com.ongmanager.mapper;

import com.ongmanager.dto.response.NgoResponse;
import com.ongmanager.entity.Ngo;
import org.springframework.stereotype.Component;

@Component
public class NgoMapper {
    public NgoResponse toResponse(Ngo n) {
        return new NgoResponse(
            n.getId(), n.getName(), n.getCnpj(), n.getDescription(),
            n.getPhone(), n.getWebsite(), n.getSocialMedia(), n.getLogoUrl(),
            n.getStatus().name(),
            n.getCategory() != null ? n.getCategory().getName() : null,
            n.getAddress() != null ? n.getAddress().getCity() : null,
            n.getAddress() != null ? n.getAddress().getState() : null,
            n.getManager() != null ? n.getManager().getId() : null,
            n.getAllowVolunteers(),
            n.getVolunteerSlots()
        );
    }
}
