package com.microsoft.openai.samples.assistant.business.models;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Payment(
    @JsonProperty("description") String description,
    @JsonProperty("recipientName") String recipientName,
    @JsonProperty("recipientBankCode") String recipientBankCode,
    @JsonProperty("accountId") String accountId,
    @JsonProperty("paymentMethodId") String paymentMethodId,
    @JsonProperty("paymentType") String paymentType,
    @JsonProperty("amount") String amount,
    @JsonProperty("timestamp") String timestamp
) {}