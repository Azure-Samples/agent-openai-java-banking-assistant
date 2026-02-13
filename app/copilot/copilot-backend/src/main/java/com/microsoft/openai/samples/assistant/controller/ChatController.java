// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.assistant.controller;


import com.microsoft.openai.samples.assistant.langchain4j.agent.SupervisorAgent;
import com.microsoft.openai.samples.assistant.security.LoggedUserService;
import dev.langchain4j.agentic.scope.AgentInvocation;
import dev.langchain4j.agentic.scope.AgenticScope;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for handling chat interactions with the banking assistant.
 * This controller works with langchain4j-agentic SupervisorAgent which uses
 * String-based API for chat interactions with built-in memory management.
 * 
 * User context (authenticated user) is automatically injected into all chat messages
 * so that agents and MCP tools can properly scope their responses to the current user.
 */
@RestController
public class ChatController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatController.class);
    private final SupervisorAgent supervisorAgent;
    private final LoggedUserService loggedUserService;

    public ChatController(
            @Qualifier("supervisorAgent") SupervisorAgent supervisorAgent,
            LoggedUserService loggedUserService) {
        this.supervisorAgent = supervisorAgent;
        this.loggedUserService = loggedUserService;
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

        // Extract the last user message for the agent API
        String userMessage = getLastUserMessage(chatRequest);
        
        // Augment the user message with authenticated user context
        // This ensures agents and MCP tools know which user owns the data
        String loggedUserName = loggedUserService.getLoggedUser().username();
        String augmentedMessage = "User: " + loggedUserName + "\n" + userMessage;
        
        // Reuse conversation ID from session state for memory continuity, or generate a new one
        String conversationId = chatRequest.session_state() != null && !chatRequest.session_state().isEmpty()
                ? chatRequest.session_state()
                : UUID.randomUUID().toString();

        LOGGER.info("Processing chat - conversationId: {}, user: {}, userMessage: {}", 
                conversationId, loggedUserName, userMessage);

        try {
            // Invoke the supervisor agent directly with proper parameter binding
            // The framework's annotation processors will handle @MemoryId and @UserMessage binding
            // The augmented message ensures user context flows to domain agents and MCP tools
            
            String agentResponse = supervisorAgent.chat(conversationId, augmentedMessage);
            
            // Extract the agent thought process from the AgenticScope
            String thoughts = extractThoughtProcess(conversationId);
            
            // Convert response to AiMessage for consistent response format
            AiMessage aiMessage = AiMessage.from(agentResponse);
            
            LOGGER.info("Agent response: {}", agentResponse);
            return ResponseEntity.ok(ChatResponse.buildChatResponse(aiMessage, thoughts, conversationId));
            
        } catch (Exception e) {
            LOGGER.error("Error invoking supervisor agent for user: {}", loggedUserName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ChatResponse.buildErrorResponse("Error processing request: " + e.getMessage()));
        }
    }

    /**
     * Extracts the agent thought process from the AgenticScope after execution.
     * The scope tracks all agent invocations made during the supervisor's orchestration,
     * including which sub-agents were called, their inputs, and outputs.
     * 
     * @param conversationId The conversation memory ID used for the chat call
     * @return HTML-formatted thought process string, or empty string if unavailable
     */
    private String extractThoughtProcess(String conversationId) {
        try {
            AgenticScope scope = supervisorAgent.getAgenticScope(conversationId);
            if (scope == null) {
                return "";
            }
            
            List<AgentInvocation> invocations = scope.agentInvocations();
            if (invocations == null || invocations.isEmpty()) {
                return "";
            }
            
            StringBuilder html = new StringBuilder();
            html.append("<div class='thought-process'>")
                .append("<h4>Agent Orchestration Steps</h4>")
                .append("<ol>");
            
            for (AgentInvocation invocation : invocations) {
                html.append("<li>")
                    .append("<strong>").append(escapeHtml(invocation.agentName())).append("</strong>");
                
                if (invocation.input() != null && !invocation.input().isEmpty()) {
                    html.append("<br/><em>Input:</em> ").append(escapeHtml(truncate(invocation.input().toString(), 500)));
                }
                
                if (invocation.output() != null) {
                    html.append("<br/><em>Output:</em> ").append(escapeHtml(truncate(invocation.output().toString(), 500)));
                }
                
                html.append("</li>");
            }
            
            html.append("</ol></div>");
            
            return html.toString();
        } catch (Exception e) {
            LOGGER.warn("Failed to extract thought process: {}", e.getMessage());
            return "";
        }
    }
    
    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                   .replace("\"", "&quot;").replace("'", "&#39;");
    }
    
    private static String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }

    /**
     * Extracts the last user message from the chat request.
     * This is used to get the current user input for agent processing.
     * 
     * @param chatRequest The incoming chat request
     * @return The last user message content
     */
    private String getLastUserMessage(ChatAppRequest chatRequest) {
        // Find the last user message in the conversation
        for (int i = chatRequest.messages().size() - 1; i >= 0; i--) {
            var message = chatRequest.messages().get(i);
            if ("user".equals(message.role())) {
                String content = message.content();
                // Append attachments if present
                if (message.attachments() != null && !message.attachments().isEmpty()) {
                    content += " " + message.attachments().toString();
                }
                return content;
            }
        }
        throw new IllegalArgumentException("No user message found in chat request");
    }

    /**
     * Legacy method for converting chat request to langchain4j format.
     * This is kept for reference but is no longer used with the new agent API.
     * 
     * @deprecated Use getLastUserMessage instead
     */
    @Deprecated
    private List<ChatMessage> convertToLangchain4j(ChatAppRequest chatAppRequest) {
       List<ChatMessage> chatHistory = new ArrayList<>();
         chatAppRequest.messages().forEach(
               historyChat -> {
                   if("user".equals(historyChat.role())) {
                     if(historyChat.attachments() == null || historyChat.attachments().isEmpty())
                         chatHistory.add(UserMessage.from(historyChat.content()));
                     else
                         chatHistory.add(UserMessage.from(historyChat.content() + " " + historyChat.attachments().toString()));
                   }
                   if("assistant".equals(historyChat.role()))
                   chatHistory.add(AiMessage.from(historyChat.content()));
               });
       return chatHistory;

    }
}
