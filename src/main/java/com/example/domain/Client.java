package com.example.domain;

import com.example.service.impl.Constants;

import java.math.BigDecimal;
import java.util.Map;

public class Client {
    final Map<String, String> profile;
    final String creditDebit;
    final String counterTransfer;
    final String amountDivider;

    public Client(Map<String, String> profile, String creditDebit,
                  String counterTransfer, String amountDivider) {
        this.profile = profile;
        this.creditDebit = creditDebit;
        this.counterTransfer = counterTransfer;
        this.amountDivider = amountDivider;
    }

    public Map<String, String> getProfile() {
        return profile;
    }

    public String getCreditDebit() {
        return creditDebit;
    }

    public boolean isCounterTransfer() {
        return counterTransfer.equalsIgnoreCase(Constants.TRUE);
    }

    public BigDecimal getAmountDivider() {
        return new BigDecimal(amountDivider);
    }
}
