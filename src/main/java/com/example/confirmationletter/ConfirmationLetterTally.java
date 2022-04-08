package com.example.confirmationletter;

import com.example.dao.CurrencyDao;
import com.example.domain.BatchTotal;
import com.example.domain.Client;
import com.example.domain.Currency;
import com.example.domain.Record;
import com.example.record.domain.RecordInterface;
import com.example.record.domain.TempRecord;
import com.example.record.service.impl.Constants;
import com.example.record.service.impl.StringUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ConfirmationLetterTally {

    private CurrencyDao currencyDao;

    public Map<String, BigDecimal> calculateAmounts(
            Client client,
            List<Record> records,
            CurrencyDao currencyDao,
            List<com.example.record.domain.FaultRecord> faultyRecords,
            List<TempRecord> faultyAccountNumberRecordList,
            List<TempRecord> sansDuplicateFaultRecordsList,
            Map<Integer, BatchTotal> batchTotals
    ) {
        Map<String, BigDecimal> result = calculateRetrieveAmounts(records, faultyRecords,
                client, faultyAccountNumberRecordList, sansDuplicateFaultRecordsList);
        result.put("CreditBatchTotal", creditBatchTotal(
                batchTotals.values(),
                client.getAmountDivider(),
                BatchTotal::getCreditValue));
        result.put("DebitBatchTotal", creditBatchTotal(
                batchTotals.values(),
                client.getAmountDivider(),
                BatchTotal::getCreditCounterValueForDebit));
        return result;
    }

    class Tally {
        BigDecimal creditFL = new BigDecimal(0);
        BigDecimal creditUSD = new BigDecimal(0);
        BigDecimal creditEUR = new BigDecimal(0);

        BigDecimal debitFL = new BigDecimal(0);
        BigDecimal debitUSD = new BigDecimal(0);
        BigDecimal debitEUR = new BigDecimal(0);

        void addRecord(RecordInterface record) {
            if (record.getCurrency().getCode().equals(Constants.FL_CURRENCY_CODE)
                    || record.getCurrency().getCode().equals(Constants.FL_CURRENCY_CODE_FOR_WEIRD_BANK)) {
                if (record.getSign().equalsIgnoreCase(Constants.DEBIT)) {
                    debitFL = record.getAmount().add(debitFL);
                } else {
                    creditFL = record.getAmount().add(creditFL);
                }
            } else if (record.getCurrency().getCode().equals(Constants.USD_CURRENCY_CODE)) {
                if (record.getSign().equalsIgnoreCase(Constants.DEBIT)) {
                    debitUSD = record.getAmount().add(debitUSD);
                } else {
                    creditUSD = record.getAmount().add(creditUSD);
                }
            } else if (record.getCurrency().getCode().equals(Constants.EUR_CURRENCY_CODE)) {
                if (record.getSign().equalsIgnoreCase(Constants.DEBIT)) {
                    debitEUR = record.getAmount().add(debitEUR);
                } else {
                    creditEUR = record.getAmount().add(creditEUR);
                }
            }
        }
    }


    // Calculate sum amount from faultyAccountnumber list
    private Map<String, BigDecimal> calculateAmountsFaultyAccountNumber(
            List<TempRecord> faultyAccountNumberRecordList, Client client) {

        Tally tally = new Tally();
        for (TempRecord faultyAccountNumberRecord : faultyAccountNumberRecordList) {
            fixSignAndCurrencyCode(client, faultyAccountNumberRecord);
            tally.addRecord(faultyAccountNumberRecord);
        }

        Map<String, BigDecimal> result = new HashMap<>();
        result.put("FaultyAccDebitFL", tally.debitFL);
        result.put("FaultyAccDebitUSD", tally.debitUSD);
        result.put("FaultyAccDebitEUR", tally.debitEUR);
        result.put("FaultyAccCreditFL", tally.creditFL);
        result.put("FaultyAccCreditUSD", tally.creditUSD);
        result.put("FaultyAccCreditEUR", tally.creditEUR);

        return result;
    }

    private void fixSignAndCurrencyCode(Client client, TempRecord tempRecord) {
        String sign = tempRecord.getSign();
        if (sign == null || StringUtils.isBlank(sign)) {
            tempRecord.setSign(client.getCreditDebit());
        }

        if (tempRecord.getCurrencyCode() == null) {
            String currencyId = currencyDao.retrieveCurrencyDefault(client.getProfile());
            Currency currency = currencyDao.retrieveCurrencyOnId(new Integer(currencyId));
            tempRecord.setCurrencyCode(currency.getCode());
        }
    }

    private Map<String, BigDecimal> calculateRetrieveAmounts(
            List<Record> records,
            List<com.example.record.domain.FaultRecord> faultyRecords,
            Client client,
            List<TempRecord> faultyAccountNumberRecordList,
            List<TempRecord> sansDuplicateFaultRecordsList) {

        Map<String, BigDecimal> retrievedAmounts = new HashMap<String, BigDecimal>();

        BigDecimal recordAmountFL = new BigDecimal(0);
        BigDecimal recordAmountUSD = new BigDecimal(0);
        BigDecimal recordAmountEUR = new BigDecimal(0);

        Tally recordAmountTally = new Tally();
        Tally amountSansTally = new Tally();

        BigDecimal totalDebitFL = new BigDecimal(0);
        BigDecimal totalDebitUSD = new BigDecimal(0);
        BigDecimal totalDebitEUR = new BigDecimal(0);

        BigDecimal totalCreditFL = new BigDecimal(0);
        BigDecimal totalCreditUSD = new BigDecimal(0);
        BigDecimal totalCreditEUR = new BigDecimal(0);

        if (client.getCounterTransfer().equalsIgnoreCase(Constants.TRUE)) {
            Tally tally = new Tally();
            for (Record record : records) {
                if (record.getFeeRecord() != 1) {
                    tally.addRecord(record);
                }
            }
            retrievedAmounts.put(Constants.CURRENCY_EURO, tally.debitEUR);
            retrievedAmounts.put(Constants.CURRENCY_USD, tally.debitUSD);
            retrievedAmounts.put(Constants.CURRENCY_FL, tally.debitFL);
        }
        // Not Balanced
        else {

            for (Record record : records) {
                if (record.getIsCounterTransferRecord().compareTo(new Integer(0)) == 0
                        && record.getFeeRecord().compareTo(new Integer(0)) == 0) {
                    recordAmountTally.addRecord(record);
                }
            }
            // Sansduplicate
            for (TempRecord sansDupRec : sansDuplicateFaultRecordsList) {
                fixSignAndCurrencyCode(client, sansDupRec);
                amountSansTally.addRecord(sansDupRec);
            }

            Map<String, BigDecimal> retrievedAccountNumberAmounts = calculateAmountsFaultyAccountNumber(
                    faultyAccountNumberRecordList, client);
            if (retrievedAccountNumberAmounts.get("FaultyAccDebitFL") != null) {
                totalDebitFL = recordAmountTally.debitFL.add(amountSansTally.debitFL)
                        .subtract(retrievedAccountNumberAmounts.get("FaultyAccDebitFL"));
            } else {
                totalDebitFL = recordAmountTally.debitFL.add(amountSansTally.debitFL);
            }

            if (retrievedAccountNumberAmounts.get("FaultyAccCreditFL") != null) {
                totalCreditFL = recordAmountTally.creditFL.add(amountSansTally.creditFL)
                        .subtract(retrievedAccountNumberAmounts.get("FaultyAccCreditFL"));
            } else {
                totalCreditFL = recordAmountTally.creditFL.add(amountSansTally.creditFL);
            }

            if (retrievedAccountNumberAmounts.get("FaultyAccDebitUSD") != null) {
                totalDebitUSD = recordAmountTally.debitUSD.add(amountSansTally.debitUSD)
                        .subtract(retrievedAccountNumberAmounts.get("FaultyAccDebitUSD"));
            } else {
                totalDebitUSD = recordAmountTally.debitUSD.add(amountSansTally.debitUSD);
            }

            if (retrievedAccountNumberAmounts.get("FaultyAccCreditUSD") != null) {
                totalCreditUSD = recordAmountTally.creditUSD.add(amountSansTally.creditUSD)
                        .subtract(retrievedAccountNumberAmounts.get("FaultyAccCreditUSD"));
            } else {
                totalCreditUSD = recordAmountTally.creditUSD.add(amountSansTally.creditUSD);
            }

            if (retrievedAccountNumberAmounts.get("FaultyAccDebitEUR") != null) {
                totalDebitEUR = recordAmountTally.debitEUR.add(amountSansTally.debitEUR)
                        .subtract(retrievedAccountNumberAmounts.get("FaultyAccDebitEUR"));
            } else {
                totalDebitEUR = recordAmountTally.debitEUR.add(amountSansTally.debitEUR);
            }

            if (retrievedAccountNumberAmounts.get("FaultyAccCreditEUR") != null) {
                totalCreditEUR = recordAmountTally.creditEUR.add(amountSansTally.creditEUR)
                        .subtract(retrievedAccountNumberAmounts.get("FaultyAccCreditEUR"));
            } else {
                totalCreditEUR = recordAmountTally.creditEUR.add(amountSansTally.creditEUR);
            }

            recordAmountFL = totalDebitFL.subtract(totalCreditFL).abs();
            recordAmountUSD = totalDebitUSD.subtract(totalCreditUSD).abs();
            recordAmountEUR = totalDebitEUR.subtract(totalCreditEUR).abs();

            retrievedAmounts.put(Constants.CURRENCY_EURO, recordAmountEUR);
            retrievedAmounts.put(Constants.CURRENCY_USD, recordAmountUSD);
            retrievedAmounts.put(Constants.CURRENCY_FL, recordAmountFL);

        }

        return retrievedAmounts;
    }

    BigDecimal creditBatchTotal(Collection<BatchTotal> batchTotals, BigDecimal amountDivider,
                                Function<BatchTotal, BigDecimal> value) {
        BigDecimal sum = BigDecimal.ZERO;
        for (BatchTotal total : batchTotals) {
            sum = sum.add(value.apply(total));
        }
        sum = sum.divide(amountDivider);
        return sum;
    }
}