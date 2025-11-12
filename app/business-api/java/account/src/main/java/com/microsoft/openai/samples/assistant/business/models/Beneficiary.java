package com.microsoft.openai.samples.assistant.business.models;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public record Beneficiary(
        @JsonProperty("id") String id,
        @JsonProperty("fullName") String fullName,
        @JsonProperty("bankCode") String bankCode,
        @JsonProperty("bankName") String bankName
) {}

