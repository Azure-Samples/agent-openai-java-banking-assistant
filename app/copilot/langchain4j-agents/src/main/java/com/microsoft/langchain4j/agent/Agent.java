package com.microsoft.langchain4j.agent;

import dev.langchain4j.data.message.ChatMessage;

import java.util.List;

public interface Agent {

    String getName();
    AgentMetadata getMetadata();
    List<ChatMessage> invoke(List<ChatMessage> chatHistory) throws AgentExecutionException;
}
