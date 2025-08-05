package com.microsoft.springai.agent;

import org.springframework.ai.chat.messages.Message;

import java.util.List;

public interface Agent {

    String getName();

    AgentMetadata getMetadata();

    List<Message> invoke(List<Message> chatHistory) throws AgentExecutionException;
}
