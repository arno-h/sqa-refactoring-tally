package com.example.confirmationletter;

import com.example.domain.Record;
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

    void add(TempRecord record) {
        if (record.getCurrencyCode().equals(Constants.FL_CURRENCY_CODE)
                || record.getCurrencyCode().equals(Constants.FL_CURRENCY_CODE_FOR_WEIRD_BANK)) {
            if (record.isDebit()) {
                debitFL = record.getAmount().add(debitFL);
            } else {
                creditFL = record.getAmount().add(creditFL);
            }
        } else if (record.getCurrencyCode().equals(Constants.USD_CURRENCY_CODE)) {
            if (record.isDebit()) {
                debitUSD = record.getAmount().add(debitUSD);
            } else {
                creditUSD = record.getAmount().add(creditUSD);
            }
        } else if (record.getCurrencyCode().equals(Constants.EUR_CURRENCY_CODE)) {
            if (record.isDebit()) {
                debitEUR = record.getAmount().add(debitEUR);
            } else {
                creditEUR = record.getAmount().add(creditEUR);
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
