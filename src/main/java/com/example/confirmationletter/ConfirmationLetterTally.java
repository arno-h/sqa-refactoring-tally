package com.example.confirmationletter;

import com.example.dao.CurrencyDao;
import com.example.domain.*;
import com.example.service.impl.Constants;
import com.example.service.impl.StringUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
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
                records, client, faultyAccountNumberRecordList, sansDuplicateFaultRecordsList);
        result.put("CreditBatchTotal", batchTotalSum(
                batchTotals.values(), client.getAmountDivider(), BatchTotal::getCreditValue));
        result.put("DebitBatchTotal", batchTotalSum(
                batchTotals.values(), client.getAmountDivider(), BatchTotal::getCreditCounterValueForDebit));
        return result;
    }

    private Map<String, BigDecimal> calculateRetrieveAmounts(
            List<Record> records,
            Client client,
            List<TempRecord> faultyAccountNumberRecordList,
            List<TempRecord> sansDuplicateFaultRecordsList) {

        if (client.getCounterTransfer().equalsIgnoreCase(Constants.TRUE)) {
            return counterTransferAmounts(records);
        } else {
            fixCurrencyAndSign(faultyAccountNumberRecordList, client);
            fixCurrencyAndSign(sansDuplicateFaultRecordsList, client);
            return unbalancedAmounts(records, faultyAccountNumberRecordList, sansDuplicateFaultRecordsList);
        }
    }

    private Map<String, BigDecimal> counterTransferAmounts(List<Record> records) {
        TallyBuilder recordAmountTally = new TallyBuilder();
        for (Record record : records) {
            if (record.getFeeRecord() != 1) {
                recordAmountTally.add(record);
            }
        }

        Map<String, BigDecimal> retrievedAmounts = new HashMap<>();
        retrievedAmounts.put(Constants.CURRENCY_EURO, recordAmountTally.debitEUR);
        retrievedAmounts.put(Constants.CURRENCY_USD, recordAmountTally.debitUSD);
        retrievedAmounts.put(Constants.CURRENCY_FL, recordAmountTally.debitFL);

        return retrievedAmounts;
    }

    private Map<String, BigDecimal> unbalancedAmounts(List<Record> records,
                                                      List<TempRecord> faultyAccountNumberRecordList,
                                                      List<TempRecord> sansDuplicateFaultRecordsList) {

        Map<String, BigDecimal> retrievedAmounts = new HashMap<>();
        TallyBuilder recordAmountTally = new TallyBuilder();

        for (Record record : records) {
            if (record.getIsCounterTransferRecord().compareTo(0) == 0 && record.getFeeRecord().compareTo(0) == 0) {
                recordAmountTally.add(record);
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

        BigDecimal recordAmountFL = recordAmountTally.debitFL.subtract(recordAmountTally.creditFL).abs();
        BigDecimal recordAmountUSD = recordAmountTally.debitUSD.subtract(recordAmountTally.creditUSD).abs();
        BigDecimal recordAmountEUR = recordAmountTally.debitEUR.subtract(recordAmountTally.creditEUR).abs();

        retrievedAmounts.put(Constants.CURRENCY_EURO, recordAmountEUR);
        retrievedAmounts.put(Constants.CURRENCY_USD, recordAmountUSD);
        retrievedAmounts.put(Constants.CURRENCY_FL, recordAmountFL);

        return retrievedAmounts;
    }

    private void fixCurrencyAndSign(List<TempRecord> tempRecordList, Client client) {
        for (TempRecord tempRecord : tempRecordList) {
            if (tempRecord.getSign() == null || StringUtils.isBlank(tempRecord.getSign())) {
                tempRecord.setSign(client.getCreditDebit());
            }
            if (tempRecord.getCurrencyCode() == null) {
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