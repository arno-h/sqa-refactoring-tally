package com.example.service.impl;

public class Constants {
    public static final Integer FL_CURRENCY_CODE = 1;
    public static final Integer FL_CURRENCY_CODE_FOR_WEIRD_BANK = 2;
    public static final Integer USD_CURRENCY_CODE = 3;
    public static final Integer EUR_CURRENCY_CODE = 4;

    public static final String CURRENCY_FL = "FLD";
    public static final String CURRENCY_USD = "USD";
    public static final String CURRENCY_EURO = "EUR";

    public static final String DEBIT = "-";
    public static final String CREDIT = "+";
    public static final String TRUE = "true";

    private Constants() {
        throw new IllegalStateException("Utility class");
    }
}