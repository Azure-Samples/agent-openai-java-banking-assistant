// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.assistant.controller;



import com.microsoft.openai.samples.assistant.common.ChatGPTMessage;
import dev.langchain4j.data.message.AiMessage;

import java.util.Collections;
import java.util.List;

public record ChatResponse(
        List<ResponseChoice> choices,
        String threadId
) {

    public static ChatResponse buildChatResponse(String agentResponse, String threadId) {
        List<String> dataPoints = Collections.emptyList();
        String thoughts = "";
        List<String> attachments = Collections.emptyList();

        return new ChatResponse(
                List.of(
                        new ResponseChoice(
                                0,
                                new ResponseMessage(
                                        agentResponse,
                                        ChatGPTMessage.ChatRole.ASSISTANT.toString(),
                                        attachments
                                ),
                                new ResponseContext(thoughts, dataPoints),
                                new ResponseMessage(
                                        agentResponse,
                                        ChatGPTMessage.ChatRole.ASSISTANT.toString(),
                                        attachments)
                        )
                ),
                threadId
        );
    }

}
