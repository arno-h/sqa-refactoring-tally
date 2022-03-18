package com.example.domain;

import com.example.service.impl.Constants;

import java.math.BigDecimal;

public class TempRecord {
    final Currency currency;
    String sign;
    BigDecimal amount;

    public TempRecord(String sign, Currency currency, BigDecimal amount) {
        this.sign = sign;
        this.currency = currency;
        this.amount = amount;
    }

    public String getSign() {
        return sign;
    }

    public boolean isDebit() {
        return sign.equalsIgnoreCase(Constants.DEBIT);
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrencyCode(Integer currencyCode) {
        this.currency.setCode(currencyCode);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
