package com.microsoft.openai.samples.assistant.plugin;

import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TransactionHistoryPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionHistoryPlugin.class);
    private final TransactionService transactionService;
    public TransactionHistoryPlugin(){
        this.transactionService = new TransactionService();
    }

    @DefineKernelFunction(name = "getTransactionsByRecepient", description = "Gets the last payment transactions based on the payee, recipient name")
    public String getTransactionsByRecepient(
            @KernelFunctionParameter(name = "recipientName", description = "Name of the payee, recipient") String recipientName) {
       String transactionsByRecipient = transactionService.getTransactionsByRecipientName(recipientName).toString();
       LOGGER.info("Transactions for [{}]:{} ",recipientName,transactionsByRecipient);
       return transactionsByRecipient;


    }


    @DefineKernelFunction(name = "getTransactions", description = "Gets the last payment transactions")
    public String getTransactions() {
        String lastTransactions = transactionService.getlastTransactions().toString();
        LOGGER.info("Last transactions:{} ",lastTransactions);
        return lastTransactions;


        }


}

