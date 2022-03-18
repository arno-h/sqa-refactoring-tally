package com.example.confirmationletter;

import com.example.domain.TempRecord;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;

class TallyBuilder {
    private static class DecimalMap {
        final Map<String, BigDecimal> amountMap = new HashMap<>();

        void add(String currencyName, BigDecimal amount) {
            BigDecimal current = amountMap.getOrDefault(currencyName, BigDecimal.ZERO);
            amountMap.put(currencyName, current.add(amount));
        }

        void addAll(DecimalMap other) {
            opAll(BigDecimal::add, other);
        }

        void subtractAll(DecimalMap other) {
            opAll(BigDecimal::subtract, other);
        }

        void opAll(BinaryOperator<BigDecimal> op, DecimalMap other) {
            Set<String> currencyNames = new HashSet<>(amountMap.keySet());
            currencyNames.addAll(other.amountMap.keySet());
            for (String currencyName : currencyNames) {
                amountMap.put(currencyName, op.apply(amountMap.getOrDefault(currencyName, BigDecimal.ZERO),
                        other.amountMap.getOrDefault(currencyName, BigDecimal.ZERO)));
            }
        }
    }

    final DecimalMap debit = new DecimalMap();
    final DecimalMap credit = new DecimalMap();

    void add(TempRecord tempRecord) {
        String currencyType = tempRecord.getCurrency().getCurrencyType();
        if (tempRecord.isDebit()) {
            debit.add(currencyType, tempRecord.getAmount());
        } else {
            credit.add(currencyType, tempRecord.getAmount());
        }
    }

    void addTally(TallyBuilder other) {
        credit.addAll(other.credit);
        debit.addAll(other.debit);
    }

    void subtractTally(TallyBuilder other) {
        credit.subtractAll(other.credit);
        debit.subtractAll(other.debit);
    }

    Map<String, BigDecimal> getDebitVsCreditMap() {
        DecimalMap result = new DecimalMap();
        result.addAll(debit);
        result.subtractAll(credit);
        result.amountMap.replaceAll((type, amount) -> amount.abs());
        return result.amountMap;
    }

    Map<String, BigDecimal> getDebitMap() {
        return debit.amountMap;
    }
}
