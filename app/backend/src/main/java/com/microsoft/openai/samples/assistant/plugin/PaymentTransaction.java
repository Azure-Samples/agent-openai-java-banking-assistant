package com.microsoft.openai.samples.assistant.plugin;

public record PaymentTransaction(String transactionId, String documentId, String recipientName, String amount, String transactionDatetime) {}

