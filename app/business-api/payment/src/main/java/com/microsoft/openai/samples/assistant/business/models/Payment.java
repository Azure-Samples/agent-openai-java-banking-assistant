package com.microsoft.openai.samples.assistant.business.models;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Payment(
    @JsonProperty("description") String description,
    @JsonProperty("recipientName") String recipientName,
    @JsonProperty("recipientBankCode") String recipientBankCode,
    @JsonProperty("accountId") String accountId,
    @JsonProperty("paymentMethodId") @JsonPropertyDescription("The payment method id") String paymentMethodId,
    @JsonProperty("paymentType") @JsonPropertyDescription("The payment method type - Visa or Bank Transfer") String paymentType,
    @JsonProperty("amount") String amount,
    @JsonProperty("timestamp") String timestamp
) {}