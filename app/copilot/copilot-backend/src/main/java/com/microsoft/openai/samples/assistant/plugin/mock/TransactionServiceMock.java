package com.microsoft.openai.samples.assistant.plugin.mock;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransactionServiceMock {

    private Map<String,List<PaymentTransaction>> lastTransactions= new HashMap<>();
    private Map<String,List<PaymentTransaction>> allTransactions= new HashMap<>();

    public TransactionServiceMock(){

        lastTransactions.put("1010", Arrays.asList(
                new PaymentTransaction("11", "Payment of the bill 334398", "Mike ThePlumber", "0001", "1010", "BankTransfer", "100.00", "2024-4-01T12:00:00Z"),
                new PaymentTransaction("22", "Payment of the bill 4613", "Jane TheElectrician", "0002", "1010", "CreditCard", "200.00", "2024-3-02T12:00:00Z"),
                new PaymentTransaction("33", "Payment of the bill 724563", "Bob TheCarpenter", "0003", "1010", "BankTransfer", "300.00", "2023-10-03T12:00:00Z"),
                new PaymentTransaction("43", "Payment of the bill 8898943", "Alice ThePainter", "0004", "1010", "DirectDebit", "400.00", "2023-8-04T12:00:00Z"),
                new PaymentTransaction("53", "Payment of the bill 19dee", "Charlie TheMechanic", "0005", "1010", "BankTransfer", "500.00", "2023-4-05T12:00:00Z"))
        );


        allTransactions.put("1010",Arrays.asList(
                new PaymentTransaction("11", "payment of bill id with 0001", "Mike ThePlumber", "A012TABTYT156!", "1010", "BankTransfer", "100.00", "2024-4-01T12:00:00Z"),
                new PaymentTransaction("21", "Payment of the bill 4200", "Mike ThePlumber", "0002", "1010", "BankTransfer", "200.00", "2024-1-02T12:00:00Z"),
                new PaymentTransaction("31", "Payment of the bill 3743", "Mike ThePlumber", "0003", "1010", "DirectDebit", "300.00", "2023-10-03T12:00:00Z"),
                new PaymentTransaction("41", "Payment of the bill 8921", "Mike ThePlumber", "0004", "1010", "Transfer", "400.00", "2023-8-04T12:00:00Z"),
                new PaymentTransaction("51", "Payment of the bill 7666", "Mike ThePlumber", "0005", "1010", "CreditCard", "500.00", "2023-4-05T12:00:00Z"),

                new PaymentTransaction("12", "Payment of the bill 5517", "Jane TheElectrician", "0001", "1010", "CreditCard", "100.00", "2024-3-01T12:00:00Z"),
                new PaymentTransaction("22", "Payment of the bill 682222", "Jane TheElectrician", "0002", "1010", "CreditCard", "200.00", "2023-1-02T12:00:00Z"),
                new PaymentTransaction("32", "Payment of the bill 94112", "Jane TheElectrician", "0003", "1010", "Transfer", "300.00", "2022-10-03T12:00:00Z"),
                new PaymentTransaction("42", "Payment of the bill 23122", "Jane TheElectrician", "0004", "1010", "Transfer", "400.00", "2022-8-04T12:00:00Z"),
                new PaymentTransaction("52", "Payment of the bill 171443", "Jane TheElectrician", "0005", "1010", "Transfer", "500.00", "2020-4-05T12:00:00Z")
        ));



    }
    public List<PaymentTransaction> getTransactionsByRecipientName(String accountId, String name) {

        return allTransactions.get(accountId)
                .stream()
                .filter(transaction -> transaction.recipientName().contains(name))
                .collect(Collectors.toList());

    }

    public List<PaymentTransaction> getlastTransactions(String accountId) {

        return lastTransactions.get(accountId);
    }

}
