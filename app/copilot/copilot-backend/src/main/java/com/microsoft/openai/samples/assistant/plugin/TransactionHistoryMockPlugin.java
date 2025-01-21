package com.microsoft.openai.samples.assistant.plugin;

import com.microsoft.openai.samples.assistant.plugin.mock.TransactionServiceMock;
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionHistoryMockPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionHistoryMockPlugin.class);
    private final TransactionServiceMock transactionService;
    public TransactionHistoryMockPlugin(){
        this.transactionService = new TransactionServiceMock();
    }

    @DefineKernelFunction(name = "getTransactionsByRecepient", description = "Gets the last payment transactions based on the payee, recipient name")
    public String getTransactionsByRecepient(
            @KernelFunctionParameter(name = "accountId", description = "The banking account id of the user") String accountId,
            @KernelFunctionParameter(name = "recipientName", description = "Name of the payee, recipient") String recipientName) {
       String transactionsByRecipient = transactionService.getTransactionsByRecipientName(accountId,recipientName).toString();
       LOGGER.info("Transactions for [{}]:{} ",recipientName,transactionsByRecipient);
       return transactionsByRecipient;


    }


    @DefineKernelFunction(name = "getTransactions", description = "Gets the last payment transactions")
    public String getTransactions(
            @KernelFunctionParameter(name = "accountId", description = "The banking account id of the user") String accountId
    ) {
        String lastTransactions = transactionService.getlastTransactions(accountId).toString();
        LOGGER.info("Last transactions:{} ",lastTransactions);
        return lastTransactions;


        }


}

