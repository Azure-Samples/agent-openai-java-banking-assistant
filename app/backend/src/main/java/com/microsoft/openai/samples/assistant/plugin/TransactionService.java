package com.microsoft.openai.samples.assistant.plugin;

import java.util.ArrayList;
import java.util.List;

public class TransactionService {

    private List<PaymentTransaction> plumberTransactions = new ArrayList<>();

    private List<PaymentTransaction> electricianTransactions = new ArrayList<>();

    private List<PaymentTransaction> lastTransactions = new ArrayList<>();

     public TransactionService(){

         plumberTransactions.add(new PaymentTransaction("11", "0001", "Bill ThePlumber", "100.00", "2024-4-01T12:00:00Z"));
         plumberTransactions.add(new PaymentTransaction("21", "0002", "Bill ThePlumber", "200.00", "2024-1-02T12:00:00Z"));
         plumberTransactions.add(new PaymentTransaction("31", "0003", "Bill ThePlumber", "300.00", "2023-10-03T12:00:00Z"));
         plumberTransactions.add(new PaymentTransaction("41", "0004", "Bill ThePlumber", "400.00", "2023-8-04T12:00:00Z"));
         plumberTransactions.add(new PaymentTransaction("51", "0005", "Bill ThePlumber", "500.00", "2023-4-05T12:00:00Z"));


         electricianTransactions.add(new PaymentTransaction("12", "0001", "Jane TheElectrician", "100.00", "2024-3-01T12:00:00Z"));
         electricianTransactions.add(new PaymentTransaction("22", "0002", "Jane TheElectrician", "200.00", "2023-1-02T12:00:00Z"));
         electricianTransactions.add(new PaymentTransaction("32", "0003", "Jane TheElectrician", "300.00", "2022-10-03T12:00:00Z"));
         electricianTransactions.add(new PaymentTransaction("42", "0004", "Jane TheElectrician", "400.00", "2022-8-04T12:00:00Z"));
         electricianTransactions.add(new PaymentTransaction("52", "0005", "Jane TheElectrician", "500.00", "2020-4-05T12:00:00Z"));


         lastTransactions.add(new PaymentTransaction("11", "0001", "Bill ThePlumber", "100.00", "2024-4-01T12:00:00Z"));
         lastTransactions.add(new PaymentTransaction("22", "0002", "Jane TheElectrician", "200.00", "2024-3-02T12:00:00Z"));
         lastTransactions.add(new PaymentTransaction("33", "0003", "Bob TheCarpenter", "300.00", "2023-10-03T12:00:00Z"));
         lastTransactions.add(new PaymentTransaction("43", "0004", "Alice ThePainter", "400.00", "2023-8-04T12:00:00Z"));
         lastTransactions.add(new PaymentTransaction("53", "0005", "Charlie TheMechanic", "500.00", "2023-4-05T12:00:00Z"));
     }
    public List<PaymentTransaction> getTransactionsByRecipientName(String name) {
        switch (name){
            case "Bill ThePlumber":
                return plumberTransactions;
            case "Jane TheElectrician":
                return electricianTransactions;
            default:
                return lastTransactions;
        }
    }

    public List<PaymentTransaction> getlastTransactions() {
        return lastTransactions;
    }

}
