package com.ongmanager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class DonorRank {
    private Long donorId;
    private String donorName;
    private BigDecimal totalAmount;
}

