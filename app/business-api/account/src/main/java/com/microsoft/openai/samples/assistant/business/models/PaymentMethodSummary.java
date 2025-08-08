package com.microsoft.openai.samples.assistant.business.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentMethodSummary(
        @JsonProperty("id") @JsonPropertyDescription("The payment method id") String id,
        @JsonProperty("type") @JsonPropertyDescription("The payment method type - Visa or Bank Transfer") String type,
        @JsonProperty("activationDate") String activationDate,
        @JsonProperty("expirationDate") String expirationDate
) {}

