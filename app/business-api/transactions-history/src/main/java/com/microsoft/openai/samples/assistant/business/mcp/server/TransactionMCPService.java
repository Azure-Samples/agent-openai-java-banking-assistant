package com.microsoft.openai.samples.assistant.business.mcp.server;

import com.microsoft.openai.samples.assistant.business.Transaction;
import com.microsoft.openai.samples.assistant.business.TransactionService;
import org.springframework.stereotype.Service;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TransactionMCPService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionMCPService.class);

    private final TransactionService transactionService;

    public TransactionMCPService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Tool(description = "Get transactions by recipient name")
    public List<Transaction> getTransactionsByRecipientName(
            @ToolParam(description = "The account ID") String accountId,
            @ToolParam(description = "The recipient's name") String recipientName) {
        logger.info("getTransactionsByRecipientName called with accountId={}, recipientName={}", accountId, recipientName);
        return transactionService.getTransactionsByRecipientName(accountId, recipientName);
    }

    @Tool(description = "Get the last transactions for an account")
    public List<Transaction> getLastTransactions(
            @ToolParam(description = "The account ID") String accountId) {
        logger.info("getLastTransactions called with accountId={}", accountId);
        return transactionService.getlastTransactions(accountId);
    }
}