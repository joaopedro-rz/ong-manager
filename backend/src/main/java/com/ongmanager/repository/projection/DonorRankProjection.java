package com.ongmanager.repository.projection;

import java.math.BigDecimal;

public interface DonorRankProjection {
    Long getDonorId();
    String getDonorName();
    BigDecimal getTotalAmount();
}

