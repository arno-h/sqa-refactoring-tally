package com.example.confirmationletter;

import com.example.domain.Record;
import com.example.record.domain.TempRecord;
import com.example.record.service.impl.Constants;

import java.math.BigDecimal;

class TallyBuilder {
    BigDecimal creditFL = BigDecimal.ZERO;
    BigDecimal creditUSD = BigDecimal.ZERO;
    BigDecimal creditEUR = BigDecimal.ZERO;
    BigDecimal debitFL = BigDecimal.ZERO;
    BigDecimal debitUSD = BigDecimal.ZERO;
    BigDecimal debitEUR = BigDecimal.ZERO;

    void addRecord(Record record) {
        add(record.getCurrency().getCode(), record.getAmount(), record.isDebit());
    }

    void addTempRecord(TempRecord record) {
        add(record.getCurrencyCode(), new BigDecimal(record.getAmount()), record.isDebit());
    }

    void add(Integer currencyCode, BigDecimal amount, boolean isDebit) {
        if (currencyCode.equals(Constants.FL_CURRENCY_CODE)
                || currencyCode.equals(Constants.FL_CURRENCY_CODE_FOR_WEIRD_BANK)) {
            if (isDebit) {
                debitFL = amount.add(debitFL);
            } else {
                creditFL = amount.add(creditFL);
            }
        } else if (currencyCode.equals(Constants.USD_CURRENCY_CODE)) {
            if (isDebit) {
                debitUSD = amount.add(debitUSD);
            } else {
                creditUSD = amount.add(creditUSD);
            }
        } else if (currencyCode.equals(Constants.EUR_CURRENCY_CODE)) {
            if (isDebit) {
                debitEUR = amount.add(debitEUR);
            } else {
                creditEUR = amount.add(creditEUR);
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
