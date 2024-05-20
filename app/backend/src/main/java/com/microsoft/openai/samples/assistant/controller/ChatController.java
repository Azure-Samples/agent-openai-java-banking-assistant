// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.assistant.controller;

import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.ai.openai.OpenAIAsyncClient;
import com.microsoft.openai.samples.assistant.agent.*;


import com.microsoft.openai.samples.assistant.proxy.BlobStorageProxy;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;

import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;



import java.util.List;

@RestController
public class ChatController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatController.class);

    private final IntentAgent intentAgent;
    private final PaymentAgent paymentAgent;
    private final HistoryReportingAgent historyReportingAgent;

    public ChatController(OpenAIAsyncClient openAIAsyncClient, DocumentIntelligenceClient documentIntelligenceClient, BlobStorageProxy blobStorageProxy, @Value("${openai.chatgpt.deployment}") String gptChatDeploymentModelId){
        this.intentAgent = new IntentAgent(openAIAsyncClient,gptChatDeploymentModelId);
        this.paymentAgent = new PaymentAgent(openAIAsyncClient,gptChatDeploymentModelId,documentIntelligenceClient,blobStorageProxy);
        this.historyReportingAgent = new HistoryReportingAgent(openAIAsyncClient,gptChatDeploymentModelId);

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

        LOGGER.info("Received request for chat api with approach[{}]", chatRequest.approach());

        if (chatRequest.messages() == null || chatRequest.messages().isEmpty()) {
            LOGGER.warn("history cannot be null in Chat request");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        ChatHistory chatHistory = convertSKChatHistory(chatRequest);

        LOGGER.info("Processing chat conversation..", chatHistory.getLastMessage().get().getContent());

        IntentResponse response = intentAgent.run(chatHistory);

        LOGGER.info("Intent Type for chat conversation: {}", response.getIntentType());
        if (response.getIntentType() == IntentType.None) {
            chatHistory.addAssistantMessage(response.getMessage());
        }

        var agentContext = new AgentContext();
        agentContext.put("requestContext", chatRequest.context());
        agentContext.put("attachments", chatRequest.attachments());
        agentContext.put("approach", chatRequest.approach());

        if (response.getIntentType() == IntentType.BillPayment || response.getIntentType() == IntentType.RepeatTransaction) {
            paymentAgent.run(chatHistory,agentContext);
            chatHistory.addAssistantMessage(agentContext.getResult());
        }

        if (response.getIntentType() == IntentType.TransactionHistory) {
            historyReportingAgent.run(chatHistory,agentContext);
            chatHistory.addAssistantMessage(agentContext.getResult());
        }

        return ResponseEntity.ok(
                ChatResponse.buildChatResponse(chatHistory, agentContext));
    }

    private ChatHistory convertSKChatHistory(ChatAppRequest chatAppRequest) {
       ChatHistory chatHistory = new ChatHistory(false);
       /*
       ChatMessageContent lastUserMessage = new ChatMessageContent(AuthorRole.USER,
                chatAppRequest.messages().remove(chatAppRequest.messages().size()-1).content());

       if(chatAppRequest.attachments() != null && !chatAppRequest.attachments().isEmpty()) {
           // prepare last user message including attachments. Append list of attachments to the last user message content
           lastUserMessage = new ChatMessageContent(AuthorRole.USER,
                   chatAppRequest.messages().remove(chatAppRequest.messages().size()-1).content() + " " +chatAppRequest.attachments().toString());
       }

        */

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
