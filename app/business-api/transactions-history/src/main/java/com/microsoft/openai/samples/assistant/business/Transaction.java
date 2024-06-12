package com.microsoft.openai.samples.assistant.business;


public record Transaction(
    String id,
    String description,
    //income/outcome
    String type,

    String recipientName,
    String recipientBankReference,
    String accountId,
    String paymentType,
    String amount,
    String timestamp
) {}