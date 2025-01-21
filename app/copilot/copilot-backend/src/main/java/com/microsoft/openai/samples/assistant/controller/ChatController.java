// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.assistant.controller;

import com.microsoft.openai.samples.assistant.agent.*;


import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ChatController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatController.class);
    private final RouterAgent agentRouter;

    public ChatController(RouterAgent agentRouter){
        this.agentRouter = agentRouter;
    }


    @PostMapping(value = "/api/chat", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChatResponse> openAIAsk(@RequestBody ChatAppRequest chatRequest) {
        if (chatRequest.stream()) {
            LOGGER.warn(
                    "Requested a content-type of application/json however also requested streaming."
                            + " Please use a content-type of application/ndjson");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Requested a content-type of application/json however also requested streaming."
                            + " Please use a content-type of application/ndjson");
        }

        if (chatRequest.messages() == null || chatRequest.messages().isEmpty()) {
            LOGGER.warn("history cannot be null in Chat request");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        ChatHistory chatHistory = convertSKChatHistory(chatRequest);


        LOGGER.debug("Processing chat conversation..", chatHistory.getLastMessage().get().getContent());

        var agentContext = new AgentContext();
        agentContext.put("requestContext", chatRequest.context());
        agentContext.put("attachments", chatRequest.attachments());
        agentContext.put("approach", chatRequest.approach());

        agentRouter.run(chatHistory,agentContext);

        return ResponseEntity.ok(
                ChatResponse.buildChatResponse(chatHistory, agentContext));
    }

    private ChatHistory convertSKChatHistory(ChatAppRequest chatAppRequest) {
       ChatHistory chatHistory = new ChatHistory();
         chatAppRequest.messages().forEach(
               historyChat -> {
                   if("user".equals(historyChat.role())) {
                     if(historyChat.attachments() == null || historyChat.attachments().isEmpty())
                         chatHistory.addUserMessage(historyChat.content());
                     else
                         chatHistory.addUserMessage(historyChat.content() + " " + historyChat.attachments().toString());
                   }
                   if("assistant".equals(historyChat.role()))
                   chatHistory.addAssistantMessage(historyChat.content());
               });
       //-chatHistory.addUserMessage(lastUserMessage.getContent());

       return chatHistory;

    }
}
