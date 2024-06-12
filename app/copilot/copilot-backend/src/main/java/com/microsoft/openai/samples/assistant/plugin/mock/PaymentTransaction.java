package com.microsoft.openai.samples.assistant.plugin.mock;

public record PaymentTransaction(
        String id,
        String description,
        String recipientName,
        String recipientBankReference,
        String accountId,
        String paymentType,
        String amount,
        String timestamp
) {}

