package com.microsoft.openai.samples.assistant.business.service;

import com.microsoft.openai.samples.assistant.business.models.Account;
import com.microsoft.openai.samples.assistant.business.models.PaymentMethod;
import com.microsoft.openai.samples.assistant.business.models.PaymentMethodSummary;
import com.microsoft.openai.samples.assistant.business.models.Beneficiary;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AccountService {

    private final Map<String, Account> accounts;
    private final Map<String, PaymentMethod> paymentMethods;

    public AccountService() {
        this.accounts = new HashMap<>();
        this.paymentMethods = new HashMap<>();
        // Fill the map with dummy data
        this.accounts.put("1000", new Account(
                "1000",
                "alice.user@contoso.com",
                "Alice User",
                "USD",
                "2022-01-01",
                "5000",
                Arrays.asList(new PaymentMethodSummary("12345", "Visa", "2022-01-01", "2025-01-01"),
                              new PaymentMethodSummary("23456", "BankTransfer", "2022-01-01", "9999-01-01"))));
        this.accounts.put("1010", new Account(
                "1010",
                "bob.user@contoso.com",
                "Bob User",
                "EUR",
                "2022-01-01",
                "10000",
                Arrays.asList(new PaymentMethodSummary("345678", "BankTransfer", "2022-01-01", "9999-01-01"),
                              new PaymentMethodSummary("55555", "Visa", "2022-01-01", "2026-01-01"))));
        this.accounts.put("1020", new Account(
                "1020",
                "charlie.user@contoso.com",
                "Charlie User",
                "EUR",
                "2022-01-01",
                "3000",
                Arrays.asList(new PaymentMethodSummary("46748576", "DirectDebit", "2022-02-01", "9999-02-01"))));

        this.paymentMethods.put("12345", new PaymentMethod("12345", "Visa", "2022-01-01", "2025-01-01", "500.00", "1234567812345678"));
        this.paymentMethods.put("55555", new PaymentMethod("55555", "Visa", "2024-01-01", "2028-01-01", "350.00", "637362551913266"));
        this.paymentMethods.put("23456", new PaymentMethod("23456", "BankTransfer", "2022-01-01", "9999-01-01", "5000.00", null));
        this.paymentMethods.put("345678", new PaymentMethod("345678", "BankTransfer", "2022-01-01", "9999-01-01", "10000.00", null));
    }

    public Account getAccountDetails(String accountId) {
        if (accountId == null || accountId.isEmpty())
            throw new IllegalArgumentException("AccountId is empty or null");
        try {
            Integer.parseInt(accountId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("AccountId is not a valid number");
        }
        // Return account data from the map
        return this.accounts.get(accountId);
    }

    public PaymentMethod getPaymentMethodDetails(String paymentMethodId) {
        if (paymentMethodId == null || paymentMethodId.isEmpty())
            throw new IllegalArgumentException("AccountId is empty or null");
        try {
            Integer.parseInt(paymentMethodId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("AccountId is not a valid number");
        }
        // Return account data from the map
        return this.paymentMethods.get(paymentMethodId);
    }

    public List<Beneficiary> getRegisteredBeneficiary(String accountId) {
        if (accountId == null || accountId.isEmpty())
            throw new IllegalArgumentException("AccountId is empty or null");
        try {
            Integer.parseInt(accountId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("AccountId is not a valid number");
        }
        // Return dummy list of beneficiaries
        return Arrays.asList(
                new Beneficiary("1", "Mike ThePlumber", "123456789", "Intesa Sanpaolo"),
                new Beneficiary("2", "Jane TheElectrician", "987654321", "UBS")
        );
    }
}