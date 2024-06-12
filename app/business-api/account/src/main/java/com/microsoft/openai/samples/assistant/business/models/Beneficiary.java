package com.microsoft.openai.samples.assistant.business.models;


import java.util.List;

public record Beneficiary(
    String id,
    String fullName,
    String bankCode,
    String bankName
) {}

