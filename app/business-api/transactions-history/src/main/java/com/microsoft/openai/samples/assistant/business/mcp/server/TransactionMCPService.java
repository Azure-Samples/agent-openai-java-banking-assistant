package com.microsoft.openai.samples.assistant.business.mcp.server;

import com.microsoft.openai.samples.assistant.business.Transaction;
import com.microsoft.openai.samples.assistant.business.TransactionService;
import org.springframework.stereotype.Service;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;

@Service
public class TransactionMCPService {

    private final TransactionService transactionService;

    public TransactionMCPService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Tool(description = "Get transactions by recipient name")
    public List<Transaction> getTransactionsByRecipientName(
            @ToolParam(description = "The account ID") String accountId,
            @ToolParam(description = "The recipient's name") String recipientName) {
        return transactionService.getTransactionsByRecipientName(accountId, recipientName);
    }

    @Tool(description = "Get the last transactions for an account")
    public List<Transaction> getLastTransactions(
            @ToolParam(description = "The account ID") String accountId) {
        return transactionService.getlastTransactions(accountId);
    }
}