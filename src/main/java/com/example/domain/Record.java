package com.example.domain;

import java.math.BigDecimal;

public class Record extends TempRecord {
    final Beneficiary beneficiary;
    final Integer feeRecord;
    final Integer isCounterTransferRecord;

    public Record(Integer feeRecord, String sign, Currency currency, BigDecimal amount,
                  Integer isCounterTransferRecord, Beneficiary beneficiary) {
        super(sign, currency, amount);
        this.feeRecord = feeRecord;
        this.isCounterTransferRecord = isCounterTransferRecord;
        this.beneficiary = beneficiary;
    }

    public boolean isFeeRecord() {
        return feeRecord == 1;
    }

    public boolean isCounterTransferRecord() {
        return isCounterTransferRecord == 1;
    }

    public Beneficiary getBeneficiary() {
        return beneficiary;
    }
}
