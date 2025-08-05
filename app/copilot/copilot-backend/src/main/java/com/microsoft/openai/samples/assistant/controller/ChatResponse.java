// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.assistant.controller;



import com.microsoft.openai.samples.assistant.common.ChatGPTMessage;
import org.springframework.ai.chat.messages.AssistantMessage;

import java.util.Collections;
import java.util.List;

public record ChatResponse(List<ResponseChoice> choices) {

    public static ChatResponse buildChatResponse(AssistantMessage aiMessage) {
        List<String> dataPoints = Collections.emptyList();
        String thoughts = "";
        List<String> attachments = Collections.emptyList();

        return new ChatResponse(
                List.of(
                        new ResponseChoice(
                                0,
                                new ResponseMessage(
                                        aiMessage.getText(),
                                        ChatGPTMessage.ChatRole.ASSISTANT.toString(),
                                        attachments
                                          ),
                                new ResponseContext(thoughts, dataPoints),
                                new ResponseMessage(
                                        aiMessage.getText(),
                                        ChatGPTMessage.ChatRole.ASSISTANT.toString(),
                                        attachments))));
    }

}
