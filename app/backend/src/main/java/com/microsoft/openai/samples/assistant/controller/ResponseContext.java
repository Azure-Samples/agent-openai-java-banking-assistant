// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.assistant.controller;

import java.util.List;

public record ResponseContext(String thoughts, List<String> data_points) {}
