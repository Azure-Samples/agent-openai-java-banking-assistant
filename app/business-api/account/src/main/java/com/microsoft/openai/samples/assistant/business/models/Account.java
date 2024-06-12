package com.microsoft.openai.samples.assistant.business.models;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Account(
    String id,
    String userName,
    String accountHolderFullName,
    String currency,
    String activationDate,
    String balance,
    List<PaymentMethodSummary> paymentMethods
) {}

