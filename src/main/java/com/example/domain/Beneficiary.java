package com.example.domain;

public class Beneficiary {
    final Bank bank;
    final String accountNumber;
    final String name;

    public Beneficiary(Bank bank, String accountNumber, String name) {
        this.bank = bank;
        this.accountNumber = accountNumber;
        this.name = name;
    }

    public Bank getBank() {
        return bank;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getName() {
        return name;
    }
}
