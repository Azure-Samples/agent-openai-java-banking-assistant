package com.microsoft.langchain4j.agent;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.service.tool.ToolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractReActAgent implements Agent {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractReActAgent.class);

    protected final ChatLanguageModel chatModel;

    protected AbstractReActAgent(ChatLanguageModel chatModel) {
        if (chatModel == null) {
            throw new IllegalArgumentException("chatModel cannot be null");
        }
        this.chatModel = chatModel;
    }

    @Override
    public void invoke(List<ChatMessage> chatHistory) throws AgentExecutionException {
        LOGGER.info("------------- {} -------------", this.getName());

        try {
            var internalChatMemory = buildInternalChat(chatHistory);

            ChatRequestParameters parameters = ChatRequestParameters.builder()
                .toolSpecifications(getToolSpecifications())
                .build();

            ChatRequest request = ChatRequest.builder()
                .messages(internalChatMemory.messages())
                .parameters(parameters)
                .build();

            var aiMessage = chatModel.chat(request).aiMessage();

            // ReAct planning with tools
            while (aiMessage != null && aiMessage.hasToolExecutionRequests()) {
                List<ToolExecutionResultMessage> toolExecutionResultMessages = executeToolRequests(aiMessage.toolExecutionRequests());

                internalChatMemory.add(aiMessage);
                toolExecutionResultMessages.forEach(internalChatMemory::add);

                ChatRequest toolExecutionResultResponseRequest = ChatRequest.builder()
                    .messages(internalChatMemory.messages())
                    .parameters(parameters)
                    .build();

                aiMessage = chatModel.chat(toolExecutionResultResponseRequest).aiMessage();
            }

            LOGGER.info("Agent response: {}", aiMessage.text());

            // add last ai message to agent internal memory
            internalChatMemory.add(aiMessage);
            updateChatHistory(chatHistory, internalChatMemory);
        } catch (Exception e) {
            throw new AgentExecutionException("Error during agent [%s] invocation".formatted(this.getName()), e);
        }
    }

    protected void updateChatHistory(List<ChatMessage> chatHistory, ChatMemory internalChatMemory) {
        //delete extenal messages to avoid duplication
        chatHistory.clear();
        //add previous history + agent internal messages
        internalChatMemory.messages()
            .stream()
            .filter(m -> !(m instanceof SystemMessage))
            .forEach(chatHistory::add);
    }

    protected List<ToolExecutionResultMessage> executeToolRequests(List<ToolExecutionRequest> toolExecutionRequests) {
        List<ToolExecutionResultMessage> toolExecutionResultMessages = new ArrayList<>();
        for (ToolExecutionRequest toolExecutionRequest : toolExecutionRequests) {
            var toolExecutor = getToolExecutor(toolExecutionRequest.name());
            LOGGER.info("Executing {} with params {}", toolExecutionRequest.name(), toolExecutionRequest.arguments());
            String result = toolExecutor.execute(toolExecutionRequest, null);
            LOGGER.info("Response from {}: {}", toolExecutionRequest.name(), result);
            if (result == null || result.isEmpty()) {
                LOGGER.warn("Tool {} returned empty result but successfully completed. Setting result=ok.", toolExecutionRequest.name());
                result = "ok";
            }
            toolExecutionResultMessages.add(ToolExecutionResultMessage.from(toolExecutionRequest, result));
        }
        return toolExecutionResultMessages;
    }

    protected ChatMemory buildInternalChat(List<ChatMessage> chatHistory) {
        var internalChatMemory = MessageWindowChatMemory.builder()
            .id("default")
            .maxMessages(20)
            .build();

        internalChatMemory.add(SystemMessage.from(getSystemMessage()));
        chatHistory.forEach(internalChatMemory::add);
        return internalChatMemory;
    }

    protected abstract String getSystemMessage();

    protected abstract List<ToolSpecification> getToolSpecifications();

    protected abstract ToolExecutor getToolExecutor(String toolName);
}