package com.microsoft.springai.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.DefaultToolCallingManager;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractReActAgent implements Agent {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractReActAgent.class);

    protected final ChatModel chatModel;

    protected AbstractReActAgent(ChatModel chatModel) {
        if (chatModel == null) {
            throw new IllegalArgumentException("chatModel cannot be null");
        }
        this.chatModel = chatModel;
    }

    @Override
    public List<Message> invoke(List<Message> chatHistory) throws AgentExecutionException {
        LOGGER.info("------------- {} -------------", this.getName());

        try {
            var internalChatMemory = buildInternalChat(chatHistory);

            ToolCallingManager toolCallingManager = DefaultToolCallingManager.builder()
                    .build();

            ChatOptions chatOptions = ToolCallingChatOptions.builder()
                    .toolCallbacks(getToolSpecifications())
                    .internalToolExecutionEnabled(false)
                    .build();

            Prompt prompt = Prompt.builder()
                    .messages(internalChatMemory.get("default"))
                    .chatOptions(chatOptions)
                    .build();

            var chatResponse = chatModel.call(prompt);
            internalChatMemory.add("default", chatResponse.getResult()
                    .getOutput());

            // ReAct planning with tools
            while (chatResponse != null && chatResponse.hasToolCalls()) {
                LOGGER.info("Tool call detected in response");
                var toolExecutionResult = executeToolRequests(chatResponse, prompt, toolCallingManager);

                internalChatMemory.add("default", toolExecutionResult.conversationHistory()
                        .get(toolExecutionResult.conversationHistory()
                                .size() - 1));

                Prompt toolExecutionResultResponseRequest = Prompt.builder()
                        .messages(internalChatMemory.get("default"))
                        .chatOptions(chatOptions)
                        .build();

                chatResponse = chatModel.call(toolExecutionResultResponseRequest);
                internalChatMemory.add("default", chatResponse.getResult()
                        .getOutput());
            }

            LOGGER.info("Agent response: {}", chatResponse);

            // add last ai message to agent internal memory
            internalChatMemory.add("default", chatResponse.getResult()
                    .getOutput());
            return buildResponse(chatHistory, internalChatMemory);
        } catch (Exception e) {
            throw new AgentExecutionException("Error during agent [%s] invocation".formatted(this.getName()), e);
        }
    }

    protected List<Message> buildResponse(List<Message> chatHistory, ChatMemory internalChatMemory) {
        return internalChatMemory.get("default")
                .stream()
                .filter(m -> !(m instanceof SystemMessage))
                .collect(Collectors.toList());
    }

    protected ChatMemory buildInternalChat(List<Message> chatHistory) {
        String conversationId = "default";
        ChatMemory internalChatMemory = MessageWindowChatMemory.builder()
                .maxMessages(20)
                .build();

        internalChatMemory.add(conversationId, SystemMessage.builder()
                .text(getSystemMessage())
                .build());
        chatHistory.forEach(chatMessage -> internalChatMemory.add(conversationId, chatMessage));
        return internalChatMemory;
    }

    protected abstract String getSystemMessage();

    protected abstract List<ToolCallback> getToolSpecifications();

    protected abstract ToolExecutionResult executeToolRequests(ChatResponse chatResponse, Prompt prompt, ToolCallingManager toolCallingManager);

}