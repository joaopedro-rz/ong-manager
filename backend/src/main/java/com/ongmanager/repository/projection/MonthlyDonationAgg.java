package com.ongmanager.repository.projection;

import java.math.BigDecimal;

public interface MonthlyDonationAgg {
    String getPeriod();
    BigDecimal getAmount();
    Long getCount();
}

