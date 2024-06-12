// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.assistant.controller;


import com.microsoft.openai.samples.assistant.agent.AgentContext;
import com.microsoft.openai.samples.assistant.common.ChatGPTMessage;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;

import java.util.Collections;
import java.util.List;

public record ChatResponse(List<ResponseChoice> choices) {

    public static ChatResponse buildChatResponse(ChatHistory chatHistory, AgentContext agentContext) {
        List<String> dataPoints = Collections.emptyList();
        String thoughts = "";
        List<String> attachments = Collections.emptyList();

        if(agentContext.get("dataPoints") != null) dataPoints.addAll((List<String>) agentContext.get("dataPoints"));
        if(agentContext.get("thoughts") != null) thoughts = (String)agentContext.get("thoughts");
        if(agentContext.get("attachments") != null) attachments.addAll((List<String>) agentContext.get("attachments"));



        return new ChatResponse(
                List.of(
                        new ResponseChoice(
                                0,
                                new ResponseMessage(
                                        chatHistory.getLastMessage().get().getContent(),
                                        ChatGPTMessage.ChatRole.ASSISTANT.toString(),
                                        attachments
                                          ),
                                new ResponseContext(thoughts, dataPoints),
                                new ResponseMessage(
                                        chatHistory.getLastMessage().get().getContent(),
                                        ChatGPTMessage.ChatRole.ASSISTANT.toString(),
                                        attachments))));
    }

}
