package com.microsoft.openai.samples.assistant.business.models;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentMethod(
        @JsonProperty("id") @JsonPropertyDescription("The payment method id") String id,
        @JsonProperty("type") @JsonPropertyDescription("The payment method type - Visa or Bank Transfer") String type,
        @JsonProperty("activationDate") String activationDate,
        @JsonProperty("expirationDate") String expirationDate,
        @JsonProperty("availableBalance") String availableBalance,
    // card number is valued only for credit card type
        @JsonProperty("cardNumber") String cardNumber
) {}

