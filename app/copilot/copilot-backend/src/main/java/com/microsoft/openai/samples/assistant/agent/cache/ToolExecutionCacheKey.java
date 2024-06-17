package com.microsoft.openai.samples.assistant.agent.cache;

import java.util.List;

public record ToolExecutionCacheKey(String userId, String threadId, String toolName, List<ToolParameter> parameters) {}
