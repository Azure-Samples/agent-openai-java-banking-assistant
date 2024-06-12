package com.microsoft.openai.samples.assistant.business;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/{accountId}")
    public List<Transaction> getTransactions(@PathVariable String accountId, @RequestParam(name = "recipient_name", required = false) String recipientName){
        logger.info("Received request to get transactions for accountid[{}]. Recipient filter is[{}]",accountId,recipientName);
        if(recipientName != null && !recipientName.isEmpty()){
            return transactionService.getTransactionsByRecipientName(accountId, recipientName);
        }
        else
            return transactionService.getlastTransactions(accountId);
    }

    @PostMapping("/{accountId}")
    public void notifyTransaction(@PathVariable String accountId, @RequestBody Transaction transaction){
        logger.info("Received request to notify transaction for accountid[{}]. {}", accountId,transaction);
        transactionService.notifyTransaction(accountId, transaction);
    }


}