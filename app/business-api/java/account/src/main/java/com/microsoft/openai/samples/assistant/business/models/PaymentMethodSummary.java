package com.microsoft.openai.samples.assistant.business.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentMethodSummary(
        @JsonProperty("id") String id,
        @JsonProperty("type") String type,
        @JsonProperty("activationDate") String activationDate,
        @JsonProperty("expirationDate") String expirationDate
) {}

