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

    // Calculate sum amount from faultyAccountnumber list
    private Map<String, BigDecimal> calculateAmountsFaultyAccountNumber(
            List<TempRecord> faultyAccountNumberRecordList, Client client) {
        Map<String, BigDecimal> retrievedAmountsFaultyAccountNumber = new HashMap<String, BigDecimal>();

        BigDecimal faultyAccRecordAmountCreditFL = new BigDecimal(0);
        BigDecimal faultyAccRecordAmountCreditUSD = new BigDecimal(0);
        BigDecimal faultyAccRecordAmountCreditEUR = new BigDecimal(0);

        BigDecimal faultyAccRecordAmountDebitFL = new BigDecimal(0);
        BigDecimal faultyAccRecordAmountDebitUSD = new BigDecimal(0);
        BigDecimal faultyAccRecordAmountDebitEUR = new BigDecimal(0);

        for (TempRecord faultyAccountNumberRecord : faultyAccountNumberRecordList) {
            // FL
            if (StringUtils.isBlank(faultyAccountNumberRecord.getSign())) {
                faultyAccountNumberRecord.setSign(client.getCreditDebit());
            }

            if (faultyAccountNumberRecord.getCurrencyCode() == null) {
                String currencyId = currencyDao.retrieveCurrencyDefault(client.getProfile());
                Currency currency = currencyDao.retrieveCurrencyOnId(Integer.valueOf(currencyId));
                faultyAccountNumberRecord.setCurrencyCode(currency.getCode());
            }

            if (faultyAccountNumberRecord.getCurrencyCode().equals(Constants.FL_CURRENCY_CODE)
                    || faultyAccountNumberRecord.getCurrencyCode().equals(Constants.FL_CURRENCY_CODE_FOR_WEIRD_BANK)) {

                if (faultyAccountNumberRecord.isDebit()) {
                    faultyAccRecordAmountDebitFL = new BigDecimal(faultyAccountNumberRecord.getAmount())
                            .add(faultyAccRecordAmountDebitFL);
                } else {
                    faultyAccRecordAmountCreditFL = new BigDecimal(faultyAccountNumberRecord.getAmount())
                            .add(faultyAccRecordAmountCreditFL);
                }
            }
            if (faultyAccountNumberRecord.getCurrencyCode().equals(Constants.USD_CURRENCY_CODE)) {
                if (faultyAccountNumberRecord.isDebit()) {
                    faultyAccRecordAmountDebitUSD = new BigDecimal(faultyAccountNumberRecord.getAmount())
                            .add(faultyAccRecordAmountDebitUSD);
                } else {
                    faultyAccRecordAmountCreditUSD = new BigDecimal(faultyAccountNumberRecord.getAmount())
                            .add(faultyAccRecordAmountCreditUSD);
                }
            }
            if (faultyAccountNumberRecord.getCurrencyCode().equals(Constants.EUR_CURRENCY_CODE)) {
                if (faultyAccountNumberRecord.isDebit()) {
                    faultyAccRecordAmountDebitEUR = new BigDecimal(faultyAccountNumberRecord.getAmount())
                            .add(faultyAccRecordAmountDebitEUR);
                } else {
                    faultyAccRecordAmountCreditEUR = new BigDecimal(faultyAccountNumberRecord.getAmount())
                            .add(faultyAccRecordAmountCreditEUR);
                }
            }

            retrievedAmountsFaultyAccountNumber.put("FaultyAccDebitFL", faultyAccRecordAmountDebitFL);
            retrievedAmountsFaultyAccountNumber.put("FaultyAccDebitUSD", faultyAccRecordAmountDebitUSD);
            retrievedAmountsFaultyAccountNumber.put("FaultyAccDebitEUR", faultyAccRecordAmountDebitEUR);

            retrievedAmountsFaultyAccountNumber.put("FaultyAccCreditFL", faultyAccRecordAmountCreditFL);
            retrievedAmountsFaultyAccountNumber.put("FaultyAccCreditUSD", faultyAccRecordAmountCreditUSD);
            retrievedAmountsFaultyAccountNumber.put("FaultyAccCreditEUR", faultyAccRecordAmountCreditEUR);

        }
        return retrievedAmountsFaultyAccountNumber;
    }

    private Map<String, BigDecimal> calculateRetrieveAmounts(
            List<Record> records,
            Client client,
            List<TempRecord> faultyAccountNumberRecordList,
            List<TempRecord> sansDuplicateFaultRecordsList) {

        Map<String, BigDecimal> retrievedAmounts = new HashMap<String, BigDecimal>();

        BigDecimal recordAmountFL = new BigDecimal(0);
        BigDecimal recordAmountUSD = new BigDecimal(0);
        BigDecimal recordAmountEUR = new BigDecimal(0);

        BigDecimal recordAmountDebitFL = new BigDecimal(0);
        BigDecimal recordAmountDebitEUR = new BigDecimal(0);
        BigDecimal recordAmountDebitUSD = new BigDecimal(0);

        BigDecimal recordAmountCreditFL = new BigDecimal(0);
        BigDecimal recordAmountCreditEUR = new BigDecimal(0);
        BigDecimal recordAmountCreditUSD = new BigDecimal(0);

        BigDecimal amountSansDebitFL = new BigDecimal(0);
        BigDecimal amountSansDebitUSD = new BigDecimal(0);
        BigDecimal amountSansDebitEUR = new BigDecimal(0);

        BigDecimal amountSansCreditFL = new BigDecimal(0);
        BigDecimal amountSansCreditUSD = new BigDecimal(0);
        BigDecimal amountSansCreditEUR = new BigDecimal(0);

        BigDecimal totalDebitFL;
        BigDecimal totalDebitUSD;
        BigDecimal totalDebitEUR;

        BigDecimal totalCreditFL;
        BigDecimal totalCreditUSD;
        BigDecimal totalCreditEUR;

        if (client.getCounterTransfer().equalsIgnoreCase(Constants.TRUE)) {
            for (Record record : records) {
                if (record.getFeeRecord() != 1) {
                    if ((record.getCurrency().getCode().equals(Constants.FL_CURRENCY_CODE)
                            || record.getCurrency().getCode().equals(Constants.FL_CURRENCY_CODE_FOR_WEIRD_BANK))
                            && record.isDebit()) {
                        recordAmountFL = record.getAmount().add(recordAmountFL);
                    }
                    if (record.getCurrency().getCode().equals(Constants.EUR_CURRENCY_CODE)
                            && record.isDebit()) {
                        recordAmountEUR = record.getAmount().add(recordAmountEUR);
                    }
                    if (record.getCurrency().getCode().equals(Constants.USD_CURRENCY_CODE)
                            && record.isDebit()) {
                        recordAmountUSD = record.getAmount().add(recordAmountUSD);
                    }
                }
            }
            retrievedAmounts.put(Constants.CURRENCY_EURO, recordAmountEUR);
            retrievedAmounts.put(Constants.CURRENCY_USD, recordAmountUSD);
            retrievedAmounts.put(Constants.CURRENCY_FL, recordAmountFL);
        }
        // Not Balanced
        else {

            for (Record record : records) {
                if (record.getIsCounterTransferRecord().compareTo(0) == 0
                        && record.getFeeRecord().compareTo(0) == 0) {
                    if ((record.getCurrency().getCode().equals(Constants.FL_CURRENCY_CODE)
                            || record.getCurrency().getCode().equals(Constants.FL_CURRENCY_CODE_FOR_WEIRD_BANK))) {
                        if (record.isDebit()) {
                            recordAmountDebitFL = record.getAmount().add(recordAmountDebitFL);
                        } else {
                            recordAmountCreditFL = record.getAmount().add(recordAmountCreditFL);
                        }

                        if (record.getCurrency().getCode().equals(Constants.EUR_CURRENCY_CODE)) {
                            if (record.isDebit()) {
                                recordAmountDebitEUR = record.getAmount().add(recordAmountDebitEUR);
                            } else {
                                recordAmountCreditEUR = record.getAmount().add(recordAmountCreditEUR);
                            }
                        }
                    }
                }

                if (record.getCurrency().getCode().equals(Constants.USD_CURRENCY_CODE)) {
                    if (record.isDebit()) {
                        recordAmountDebitUSD = record.getAmount().add(recordAmountDebitUSD);
                    } else {
                        recordAmountCreditUSD = record.getAmount().add(recordAmountCreditUSD);
                    }
                }
            }

            // Sansduplicate
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

                    if (currencyCode.equals(Constants.FL_CURRENCY_CODE)
                            || currencyCode.equals(Constants.FL_CURRENCY_CODE_FOR_WEIRD_BANK)) {

                        if (sansDupRec.isDebit()) {
                            amountSansDebitFL = new BigDecimal(sansDupRec.getAmount()).add(amountSansDebitFL);
                        } else {
                            amountSansCreditFL = new BigDecimal(sansDupRec.getAmount()).add(amountSansCreditFL);
                        }
                    }
                    if (currencyCode.equals(Constants.USD_CURRENCY_CODE)) {
                        if (sansDupRec.isDebit()) {
                            amountSansDebitUSD = new BigDecimal(sansDupRec.getAmount()).add(amountSansDebitUSD);
                        } else {
                            amountSansCreditUSD = new BigDecimal(sansDupRec.getAmount()).add(amountSansCreditUSD);
                        }
                    }
                    if (currencyCode.equals(Constants.EUR_CURRENCY_CODE)) {
                        if (sansDupRec.isDebit()) {
                            amountSansDebitEUR = new BigDecimal(sansDupRec.getAmount()).add(amountSansDebitEUR);
                        } else {
                            amountSansCreditEUR = new BigDecimal(sansDupRec.getAmount()).add(amountSansCreditEUR);
                        }
                    }
                }

            }

            Map<String, BigDecimal> retrievedAccountNumberAmounts = calculateAmountsFaultyAccountNumber(
                    faultyAccountNumberRecordList, client);
            if (retrievedAccountNumberAmounts.get("FaultyAccDebitFL") != null) {
                totalDebitFL = recordAmountDebitFL
                        .add(amountSansDebitFL)
                        .subtract(retrievedAccountNumberAmounts.get("FaultyAccDebitFL"));
            } else {
                totalDebitFL = recordAmountDebitFL.add(amountSansDebitFL);
            }

            if (retrievedAccountNumberAmounts.get("FaultyAccCreditFL") != null) {
                totalCreditFL = recordAmountCreditFL
                        .add(amountSansCreditFL)
                        .subtract(retrievedAccountNumberAmounts.get("FaultyAccCreditFL"));
            } else {
                totalCreditFL = recordAmountCreditFL.add(amountSansCreditFL);
            }

            if (retrievedAccountNumberAmounts.get("FaultyAccDebitUSD") != null) {
                totalDebitUSD = recordAmountDebitUSD
                        .add(amountSansDebitUSD)
                        .subtract(retrievedAccountNumberAmounts.get("FaultyAccDebitUSD"));
            } else {
                totalDebitUSD = recordAmountDebitUSD.add(amountSansDebitUSD);
            }

            if (retrievedAccountNumberAmounts.get("FaultyAccCreditUSD") != null) {
                totalCreditUSD = recordAmountCreditUSD
                        .add(amountSansCreditUSD)
                        .subtract(retrievedAccountNumberAmounts.get("FaultyAccCreditUSD"));
            } else {
                totalCreditUSD = recordAmountCreditUSD.add(amountSansCreditUSD);
            }

            if (retrievedAccountNumberAmounts.get("FaultyAccDebitEUR") != null) {
                totalDebitEUR = recordAmountDebitEUR
                        .add(amountSansDebitEUR)
                        .subtract(retrievedAccountNumberAmounts.get("FaultyAccDebitEUR"));
            } else {
                totalDebitEUR = recordAmountDebitEUR.add(amountSansDebitEUR);
            }

            if (retrievedAccountNumberAmounts.get("FaultyAccCreditEUR") != null) {
                totalCreditEUR = recordAmountCreditEUR
                        .add(amountSansCreditEUR)
                        .subtract(retrievedAccountNumberAmounts.get("FaultyAccCreditEUR"));
            } else {
                totalCreditEUR = recordAmountCreditEUR.add(amountSansCreditEUR);
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