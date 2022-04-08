package com.example.record.domain;

import com.example.domain.Currency;

import java.math.BigDecimal;

public interface RecordInterface {
    String getSign();

    Currency getCurrency();

    BigDecimal getAmount();
}
