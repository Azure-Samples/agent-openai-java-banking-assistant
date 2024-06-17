package com.microsoft.openai.samples.assistant.agent.cache;

public record ToolParameter (String name, String value) {
    public ToolParameter {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name is empty or null");
        }
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Value is empty or null");
        }
    }

}
