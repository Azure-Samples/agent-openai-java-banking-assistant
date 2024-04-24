// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.assistant.controller;


import com.microsoft.openai.samples.assistant.common.ChatGPTMessage;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;

import java.util.Collections;
import java.util.List;

public record ChatResponse(List<ResponseChoice> choices) {

    public static ChatResponse buildChatResponse(ChatHistory chatHistory) {
        List<String> dataPoints = Collections.emptyList();



        String thoughts = "";


        return new ChatResponse(
                List.of(
                        new ResponseChoice(
                                0,
                                new ResponseMessage(
                                        chatHistory.getLastMessage().get().getContent(),
                                        ChatGPTMessage.ChatRole.ASSISTANT.toString()),
                                new ResponseContext(thoughts, dataPoints),
                                new ResponseMessage(
                                        chatHistory.getLastMessage().get().getContent(),
                                        ChatGPTMessage.ChatRole.ASSISTANT.toString()))));
    }

}
