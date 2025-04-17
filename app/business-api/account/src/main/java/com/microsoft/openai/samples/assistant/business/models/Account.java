package com.microsoft.openai.samples.assistant.business.models;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record Account(
        @JsonProperty("id") String id,
        @JsonProperty("userName") String userName,
        @JsonProperty("accountHolderFullName") String accountHolderFullName,
        @JsonProperty("currency") String currency,
        @JsonProperty("activationDate") String activationDate,
        @JsonProperty("balance") String balance,
    List<PaymentMethodSummary> paymentMethods
) {}

