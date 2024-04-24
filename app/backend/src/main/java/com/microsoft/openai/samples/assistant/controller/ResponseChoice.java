// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.assistant.controller;

public record ResponseChoice(
        int index, ResponseMessage message, ResponseContext context, ResponseMessage delta) {}
