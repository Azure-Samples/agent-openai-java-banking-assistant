// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.assistant.controller;

import java.util.List;

public record ChatAppRequest(
        List<ResponseMessage> messages,

        List<String> attachments,
        ChatAppRequestContext context,
        boolean stream,
        String approach) {}
