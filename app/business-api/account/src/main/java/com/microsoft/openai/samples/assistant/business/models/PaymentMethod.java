package com.microsoft.openai.samples.assistant.business.models;


import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentMethod(
    String id,
    String type,
    String activationDate,
    String expirationDate,
    String availableBalance,
    // card number is valued only for credit card type
    String cardNumber
) {}

