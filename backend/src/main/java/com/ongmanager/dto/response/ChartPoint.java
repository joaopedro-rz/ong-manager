package com.ongmanager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ChartPoint {
    private String period;
    private BigDecimal amount;
    private Long count;
}

