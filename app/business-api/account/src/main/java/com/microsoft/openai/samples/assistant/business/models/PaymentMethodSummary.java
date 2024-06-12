package com.microsoft.openai.samples.assistant.business.models;


public record PaymentMethodSummary(
    String id,
    String type,
    String activationDate,
    String expirationDate
) {}

