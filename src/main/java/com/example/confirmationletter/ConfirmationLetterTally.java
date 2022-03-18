package com.example.confirmationletter;

import com.example.dao.CurrencyDao;
import com.example.domain.*;
import com.example.service.impl.StringUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ConfirmationLetterTally {

    public Map<String, BigDecimal> calculateAmounts(
            Client client,
            List<Record> records,
            List<TempRecord> faultyAccountNumberRecordList,
            List<TempRecord> sansDuplicateFaultRecordsList,
            Map<Integer, BatchTotal> batchTotals
    ) {
        Map<String, BigDecimal> result = calculateRetrieveAmounts(
                client, records, faultyAccountNumberRecordList, sansDuplicateFaultRecordsList);
        result.put("CreditBatchTotal", batchTotalSum(
                batchTotals.values(), client.getAmountDivider(), BatchTotal::getCreditValue));
        result.put("DebitBatchTotal", batchTotalSum(
                batchTotals.values(), client.getAmountDivider(), BatchTotal::getCreditCounterValueForDebit));
        return result;
    }

    private Map<String, BigDecimal> calculateRetrieveAmounts(
            Client client,
            List<Record> records,
            List<TempRecord> faultyAccountNumberRecordList,
            List<TempRecord> sansDuplicateFaultRecordsList) {

        if (client.isCounterTransfer()) {
            return counterTransferAmounts(records);
        } else {
            fixCurrencyAndSign(faultyAccountNumberRecordList, client);
            fixCurrencyAndSign(sansDuplicateFaultRecordsList, client);
            return unbalancedAmounts(records, faultyAccountNumberRecordList, sansDuplicateFaultRecordsList);
        }
    }

    private Map<String, BigDecimal> counterTransferAmounts(List<Record> records) {
        TallyBuilder recordAmountTally = new TallyBuilder();
        for (Record rec : records) {
            if (rec.isFeeRecord()) {
                recordAmountTally.add(rec);
            }
        }
        return recordAmountTally.getDebitMap();
    }

    private Map<String, BigDecimal> unbalancedAmounts(List<Record> records,
                                                      List<TempRecord> faultyAccountNumberRecordList,
                                                      List<TempRecord> sansDuplicateFaultRecordsList) {
        TallyBuilder recordAmountTally = new TallyBuilder();
        for (Record rec : records) {
            if (!rec.isCounterTransferRecord() && !rec.isFeeRecord()) {
                recordAmountTally.add(rec);
            }
        }

        TallyBuilder sansDupRecTally = new TallyBuilder();
        for (TempRecord sansDupRec : sansDuplicateFaultRecordsList) {
            sansDupRecTally.add(sansDupRec);
        }

        TallyBuilder faultyAccountTally = new TallyBuilder();
        for (TempRecord faultyAccountNumberRecord : faultyAccountNumberRecordList) {
            faultyAccountTally.add(faultyAccountNumberRecord);
        }

        recordAmountTally.addTally(sansDupRecTally);
        recordAmountTally.subtractTally(faultyAccountTally);
        return recordAmountTally.getDebitVsCreditMap();
    }

    private void fixCurrencyAndSign(List<TempRecord> tempRecordList, Client client) {
        for (TempRecord tempRecord : tempRecordList) {
            if (tempRecord.getSign() == null || StringUtils.isBlank(tempRecord.getSign())) {
                tempRecord.setSign(client.getCreditDebit());
            }
            if (tempRecord.getCurrency().getCode() == null) {
                String currencyId = CurrencyDao.retrieveCurrencyDefault(client.getProfile());
                Currency currency = CurrencyDao.retrieveCurrencyOnId(Integer.valueOf(currencyId));
                tempRecord.setCurrencyCode(currency.getCode());
            }
        }
    }

    interface BatchValueAccessor {
        BigDecimal get(BatchTotal batchTotal);
    }

    BigDecimal batchTotalSum(Collection<BatchTotal> batchTotals, BigDecimal amountDivider,
                             BatchValueAccessor value) {
        BigDecimal sum = BigDecimal.ZERO;
        for (BatchTotal total : batchTotals) {
            sum = sum.add(value.get(total));
        }
        sum = sum.divide(amountDivider);
        return sum;
    }
}