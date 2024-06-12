package com.microsoft.openai.samples.assistant.business.models;


public record Payment(
    String description,
    String recipientName,
    String recipientBankCode,
    String accountId,
    String paymentMethodId,
    String paymentType,
    String amount,
    String timestamp
) {}