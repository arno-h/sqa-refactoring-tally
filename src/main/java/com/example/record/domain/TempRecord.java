package com.example.record.domain;

import com.example.domain.Currency;

import java.math.BigDecimal;

public class TempRecord implements RecordInterface {
    String sign;
    Currency currency;
    BigDecimal amount;

    public TempRecord(String sign, Currency currency, BigDecimal amount) {
        this.sign = sign;
        this.currency = currency;
        this.amount = amount;
    }

    public String getSign() {
        return sign;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public Integer getCurrencyCode() {
        return currency.getCode();
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
