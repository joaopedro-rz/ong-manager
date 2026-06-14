package com.ongmanager.dto.request;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data public class VolunteerApplicationRequest {
    @NotNull private Long opportunityId;
    private String motivation;
}
