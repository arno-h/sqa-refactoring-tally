package com.example.domain;

import java.math.BigDecimal;

public class Record extends TempRecord {
    Bank bank;
    String beneficiaryAccountNumber;
    String beneficiaryName;
    Integer feeRecord;
    Integer isCounterTransferRecord;

    public Record(Integer feeRecord, String sign, Currency currency, BigDecimal amount,
                  Integer isCounterTransferRecord, String beneficiaryAccountNumber,
                  String beneficiaryName, Bank bank) {
        super(sign, currency, amount);
        this.feeRecord = feeRecord;
        this.isCounterTransferRecord = isCounterTransferRecord;
        this.beneficiaryAccountNumber = beneficiaryAccountNumber;
        this.beneficiaryName = beneficiaryName;
        this.bank = bank;
    }

    public Integer getFeeRecord() {
        return feeRecord;
    }

    public Integer getIsCounterTransferRecord() {
        return isCounterTransferRecord;
    }

    public String getBeneficiaryAccountNumber() {
        return beneficiaryAccountNumber;
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public Bank getBank() {
        return bank;
    }
}
