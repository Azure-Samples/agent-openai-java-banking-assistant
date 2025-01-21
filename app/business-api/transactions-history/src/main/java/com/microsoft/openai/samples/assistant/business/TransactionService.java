package com.microsoft.openai.samples.assistant.business;

import com.microsoft.openai.samples.assistant.business.Transaction;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private Map<String,List<Transaction>> lastTransactions= new HashMap<>();
    private Map<String,List<Transaction>> allTransactions= new HashMap<>();

     public TransactionService(){

         lastTransactions.put("1010",new ArrayList<> (Arrays.asList(
                new Transaction("11", "Payment of the bill 334398", "outcome","acme", "0001", "1010", "BankTransfer", "100.00", "2024-4-01T12:00:00Z"),
                new Transaction("22", "Payment of the bill 4613","outcome", "contoso", "0002", "1010", "CreditCard", "200.00", "2024-3-02T12:00:00Z"),
                new Transaction("33", "Payment of the bill 724563","outcome", "duff", "0003", "1010", "BankTransfer", "300.00", "2023-10-03T12:00:00Z"),
                new Transaction("43", "Payment of the bill 8898943","outcome", "wayne enterprises", "0004", "1010", "DirectDebit", "400.00", "2023-8-04T12:00:00Z"),
                new Transaction("53", "Payment of the bill 19dee","outcome", "oscorp", "0005", "1010", "BankTransfer", "500.00", "2023-4-05T12:00:00Z"))
         ));


         allTransactions.put("1010",new ArrayList<>(Arrays.asList(
                new Transaction("11", "payment of bill id with 0001","outcome", "acme", "A012TABTYT156!", "1010", "BankTransfer", "100.00", "2024-4-01T12:00:00Z"),
                new Transaction("21", "Payment of the bill 4200","outcome", "acme", "0002", "1010", "BankTransfer", "200.00", "2024-1-02T12:00:00Z"),
                new Transaction("31", "Payment of the bill 3743","outcome", "acme", "0003", "1010", "DirectDebit", "300.00", "2023-10-03T12:00:00Z"),
                new Transaction("41", "Payment of the bill 8921","outcome", "acme", "0004", "1010", "Transfer", "400.00", "2023-8-04T12:00:00Z"),
                new Transaction("51", "Payment of the bill 7666","outcome", "acme", "0005", "1010", "CreditCard", "500.00", "2023-4-05T12:00:00Z"),

                new Transaction("12", "Payment of the bill 5517","outcome", "contoso", "0001", "1010", "CreditCard", "100.00", "2024-3-01T12:00:00Z"),
                new Transaction("22", "Payment of the bill 682222","outcome", "contoso", "0002", "1010", "CreditCard", "200.00", "2023-1-02T12:00:00Z"),
                new Transaction("32", "Payment of the bill 94112","outcome", "contoso", "0003", "1010", "Transfer", "300.00", "2022-10-03T12:00:00Z"),
                new Transaction("42", "Payment of the bill 23122","outcome", "contoso", "0004", "1010", "Transfer", "400.00", "2022-8-04T12:00:00Z"),
                new Transaction("52", "Payment of the bill 171443","outcome", "contoso", "0005", "1010", "Transfer", "500.00", "2020-4-05T12:00:00Z")
         )));



     }
    public List<Transaction> getTransactionsByRecipientName(String accountId, String name) {

        if (accountId == null || accountId.isEmpty())
            throw new IllegalArgumentException("AccountId is empty or null");
        try {
            Integer.parseInt(accountId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("AccountId is not a valid number");
        }

    if ( allTransactions.get(accountId) == null) return new ArrayList<>();
        else
      return  allTransactions.get(accountId).stream()
                .filter(transaction -> transaction.recipientName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());

    }

    public List<Transaction> getlastTransactions(String accountId) {
        if (accountId == null || accountId.isEmpty())
            throw new IllegalArgumentException("AccountId is empty or null");
        try {
            Integer.parseInt(accountId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("AccountId is not a valid number");
        }

        if ( lastTransactions.get(accountId) == null) return new ArrayList<>();
        else
        return lastTransactions.get(accountId);
    }

    public void notifyTransaction(String accountId,Transaction transaction){
        if (accountId == null || accountId.isEmpty())
            throw new IllegalArgumentException("AccountId is empty or null");
        try {
            Integer.parseInt(accountId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("AccountId is not a valid number");
        }

         var transactionsList = allTransactions.get(accountId);
         if ( transactionsList == null)
             throw new RuntimeException("Cannot find all transactions for account id: "+accountId);
        transactionsList.add(transaction);

        var lastTransactionsList = lastTransactions.get(accountId);
        if ( lastTransactionsList == null)
            throw new RuntimeException("Cannot find last transactions for account id: "+accountId);
        lastTransactionsList.add(transaction);


    }
}
