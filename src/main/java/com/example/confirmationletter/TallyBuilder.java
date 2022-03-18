package com.example.confirmationletter;

import com.example.domain.TempRecord;
import com.example.service.impl.Constants;

import java.math.BigDecimal;

class TallyBuilder {
    BigDecimal creditFL = BigDecimal.ZERO;
    BigDecimal creditUSD = BigDecimal.ZERO;
    BigDecimal creditEUR = BigDecimal.ZERO;
    BigDecimal debitFL = BigDecimal.ZERO;
    BigDecimal debitUSD = BigDecimal.ZERO;
    BigDecimal debitEUR = BigDecimal.ZERO;

    void add(TempRecord tempRecord) {
        if (tempRecord.getCurrencyCode().equals(Constants.FL_CURRENCY_CODE)
                || tempRecord.getCurrencyCode().equals(Constants.FL_CURRENCY_CODE_FOR_WEIRD_BANK)) {
            if (tempRecord.isDebit()) {
                debitFL = tempRecord.getAmount().add(debitFL);
            } else {
                creditFL = tempRecord.getAmount().add(creditFL);
            }
        } else if (tempRecord.getCurrencyCode().equals(Constants.USD_CURRENCY_CODE)) {
            if (tempRecord.isDebit()) {
                debitUSD = tempRecord.getAmount().add(debitUSD);
            } else {
                creditUSD = tempRecord.getAmount().add(creditUSD);
            }
        } else if (tempRecord.getCurrencyCode().equals(Constants.EUR_CURRENCY_CODE)) {
            if (tempRecord.isDebit()) {
                debitEUR = tempRecord.getAmount().add(debitEUR);
            } else {
                creditEUR = tempRecord.getAmount().add(creditEUR);
            }
        }
    }

    void addTally(TallyBuilder other) {
        creditFL = creditFL.add(other.creditFL);
        creditUSD = creditUSD.add(other.creditUSD);
        creditEUR = creditEUR.add(other.creditEUR);
        debitFL = debitFL.add(other.debitFL);
        debitUSD = debitUSD.add(other.debitUSD);
        debitEUR = debitEUR.add(other.debitEUR);
    }

    void subtractTally(TallyBuilder other) {
        creditFL = creditFL.subtract(other.creditFL);
        creditUSD = creditUSD.subtract(other.creditUSD);
        creditEUR = creditEUR.subtract(other.creditEUR);
        debitFL = debitFL.subtract(other.debitFL);
        debitUSD = debitUSD.subtract(other.debitUSD);
        debitEUR = debitEUR.subtract(other.debitEUR);
    }
}
