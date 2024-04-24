// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.assistant.controller;

public record ChatAppRequestOverrides(
        boolean semantic_ranker,
        boolean semantic_captions,
        String exclude_category,
        int top,
        float temperature,
        String prompt_template,
        String prompt_template_prefix,
        String prompt_template_suffix,
        boolean suggest_followup_questions,
        boolean use_oid_security_filter,
        boolean use_groups_security_filter,
        String semantic_kernel_mode) {}
