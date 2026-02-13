// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.assistant.controller;



import com.microsoft.openai.samples.assistant.common.ChatGPTMessage;
import dev.langchain4j.data.message.AiMessage;

import java.util.Collections;
import java.util.List;

public record ChatResponse(List<ResponseChoice> choices) {

    public static ChatResponse buildChatResponse(AiMessage aiMessage, String thoughts, String sessionState) {
        List<String> dataPoints = Collections.emptyList();
        List<String> attachments = Collections.emptyList();

        return new ChatResponse(
                List.of(
                        new ResponseChoice(
                                0,
                                new ResponseMessage(
                                        aiMessage.text(),
                                        ChatGPTMessage.ChatRole.ASSISTANT.toString(),
                                        attachments
                                          ),
                                new ResponseContext(thoughts != null ? thoughts : "", dataPoints),
                                new ResponseMessage(
                                        aiMessage.text(),
                                        ChatGPTMessage.ChatRole.ASSISTANT.toString(),
                                        attachments),
                                sessionState)));
    }

    /**
     * Builds an error response for the chat API.
     * 
     * @param errorMessage The error message to return
     * @return A ChatResponse containing the error
     */
    public static ChatResponse buildErrorResponse(String errorMessage) {
        List<String> dataPoints = Collections.emptyList();
        String thoughts = "";
        List<String> attachments = Collections.emptyList();

        return new ChatResponse(
                List.of(
                        new ResponseChoice(
                                0,
                                new ResponseMessage(
                                        errorMessage,
                                        ChatGPTMessage.ChatRole.ASSISTANT.toString(),
                                        attachments
                                ),
                                new ResponseContext(thoughts, dataPoints),
                                new ResponseMessage(
                                        errorMessage,
                                        ChatGPTMessage.ChatRole.ASSISTANT.toString(),
                                        attachments),
                                null)));
    }

}
