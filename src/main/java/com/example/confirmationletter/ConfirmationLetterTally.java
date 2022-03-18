package com.example.confirmationletter;

import com.example.dao.CurrencyDao;
import com.example.domain.Currency;
import com.example.domain.*;
import com.example.record.domain.TempRecord;
import com.example.record.service.impl.Constants;
import com.example.record.service.impl.StringUtils;

import java.math.BigDecimal;
import java.util.*;

public class ConfirmationLetterTally {

    private CurrencyDao currencyDao;

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
                batchTotals.values(),
                client.getAmountDivider(),
                BatchTotal::getCreditValue));
        result.put("DebitBatchTotal", batchTotalSum(
                batchTotals.values(),
                client.getAmountDivider(),
                BatchTotal::getCreditCounterValueForDebit));
        return result;
    }

    private static class TallyBuilder {
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
            }
            else if (currencyCode.equals(Constants.USD_CURRENCY_CODE)) {
                if (isDebit) {
                    debitUSD = amount.add(debitUSD);
                } else {
                    creditUSD = amount.add(creditUSD);
                }
            }
            else if (currencyCode.equals(Constants.EUR_CURRENCY_CODE)) {
                if (isDebit) {
                    debitEUR = amount.add(debitEUR);
                } else {
                    creditEUR = amount.add(creditEUR);
                }
            }
        }
    }

    private TallyBuilder calculateAmountsFaultyAccountNumber(
            List<TempRecord> faultyAccountNumberRecordList, Client client) {

        TallyBuilder faultyAccountTally = new TallyBuilder();
        for (TempRecord faultyAccountNumberRecord : faultyAccountNumberRecordList) {
            if (StringUtils.isBlank(faultyAccountNumberRecord.getSign())) {
                faultyAccountNumberRecord.setSign(client.getCreditDebit());
            }

            if (faultyAccountNumberRecord.getCurrencyCode() == null) {
                String currencyId = currencyDao.retrieveCurrencyDefault(client.getProfile());
                Currency currency = currencyDao.retrieveCurrencyOnId(Integer.valueOf(currencyId));
                faultyAccountNumberRecord.setCurrencyCode(currency.getCode());
            }

            faultyAccountTally.addTempRecord(faultyAccountNumberRecord);
        }

        return faultyAccountTally;
    }

    private Map<String, BigDecimal> calculateRetrieveAmounts(
            List<Record> records,
            Client client,
            List<TempRecord> faultyAccountNumberRecordList,
            List<TempRecord> sansDuplicateFaultRecordsList) {

        Map<String, BigDecimal> retrievedAmounts = new HashMap<String, BigDecimal>();

        if (client.getCounterTransfer().equalsIgnoreCase(Constants.TRUE)) {
            TallyBuilder recordAmountTally = new TallyBuilder();
            for (Record record : records) {
                if (record.getFeeRecord() != 1) {
                    recordAmountTally.addRecord(record);
                }
            }
            retrievedAmounts.put(Constants.CURRENCY_EURO, recordAmountTally.debitEUR);
            retrievedAmounts.put(Constants.CURRENCY_USD, recordAmountTally.debitUSD);
            retrievedAmounts.put(Constants.CURRENCY_FL, recordAmountTally.debitFL);
        }
        // Not Balanced
        else {
            TallyBuilder recordAmountTally = new TallyBuilder();

            for (Record record : records) {
                if (record.getIsCounterTransferRecord().compareTo(0) == 0
                        && record.getFeeRecord().compareTo(0) == 0) {
                    recordAmountTally.addRecord(record);
                }
            }

            // Sansduplicate
            TallyBuilder sansDupRecTally = new TallyBuilder();
            for (TempRecord sansDupRec : sansDuplicateFaultRecordsList) {
                Integer currencyCode = sansDupRec.getCurrencyCode();
                if (sansDupRec.getSign() == null) {
                    String sign = client.getCreditDebit();
                    sansDupRec.setSign(sign);
                }
                if (currencyCode == null) {
                    String currencyId = currencyDao.retrieveCurrencyDefault(client.getProfile());
                    Currency currency = currencyDao.retrieveCurrencyOnId(Integer.valueOf(currencyId));
                    sansDupRec.setCurrencyCode(currency.getCode());
                } else {
                    sansDupRecTally.addTempRecord(sansDupRec);
                }
            }

            TallyBuilder retrievedFaultyAmounts = calculateAmountsFaultyAccountNumber(
                    faultyAccountNumberRecordList, client);
            BigDecimal totalDebitFL = recordAmountTally.debitFL.add(sansDupRecTally.debitFL);
            totalDebitFL = totalDebitFL.subtract(retrievedFaultyAmounts.debitFL);
            BigDecimal totalCreditFL = recordAmountTally.creditFL.add(sansDupRecTally.creditFL);
            totalCreditFL = totalCreditFL.subtract(retrievedFaultyAmounts.creditFL);

            BigDecimal totalDebitUSD = recordAmountTally.debitUSD.add(sansDupRecTally.debitUSD);
            totalDebitUSD = totalDebitUSD.subtract(retrievedFaultyAmounts.debitUSD);
            BigDecimal totalCreditUSD = recordAmountTally.creditUSD.add(sansDupRecTally.creditUSD);
            totalCreditUSD = totalCreditUSD.subtract(retrievedFaultyAmounts.creditUSD);

            BigDecimal totalDebitEUR = recordAmountTally.debitEUR.add(sansDupRecTally.debitEUR);
            totalDebitEUR = totalDebitEUR.subtract(retrievedFaultyAmounts.debitEUR);
            BigDecimal totalCreditEUR = recordAmountTally.creditEUR.add(sansDupRecTally.creditEUR);
            totalCreditEUR = totalCreditEUR.subtract(retrievedFaultyAmounts.creditEUR);

            BigDecimal recordAmountFL = totalDebitFL.subtract(totalCreditFL).abs();
            BigDecimal recordAmountUSD = totalDebitUSD.subtract(totalCreditUSD).abs();
            BigDecimal recordAmountEUR = totalDebitEUR.subtract(totalCreditEUR).abs();

            retrievedAmounts.put(Constants.CURRENCY_EURO, recordAmountEUR);
            retrievedAmounts.put(Constants.CURRENCY_USD, recordAmountUSD);
            retrievedAmounts.put(Constants.CURRENCY_FL, recordAmountFL);
        }

        return retrievedAmounts;
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