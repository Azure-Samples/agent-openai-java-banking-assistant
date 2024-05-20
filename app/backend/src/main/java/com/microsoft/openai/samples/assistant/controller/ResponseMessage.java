// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.assistant.controller;

import java.util.List;

public record ResponseMessage(String content, String role, List<String> attachments) {}
