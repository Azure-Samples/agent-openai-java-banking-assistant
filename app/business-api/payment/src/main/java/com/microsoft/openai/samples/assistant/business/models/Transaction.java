package com.microsoft.openai.samples.assistant.business.models;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Transaction(
        @JsonProperty("id") String id,
        @JsonProperty("description") String description,
        //income/outcome
        @JsonProperty("type") String type,
        @JsonProperty("recipientName") String recipientName,
        @JsonProperty("recipientBankReference") String recipientBankReference,
        @JsonProperty("accountId") String accountId,
        @JsonProperty("paymentType") String paymentType,
        @JsonProperty("amount") String amount,
        @JsonProperty("timestamp") String timestamp
) {}