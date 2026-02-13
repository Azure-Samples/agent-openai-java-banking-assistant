// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.assistant.langchain4j.agent;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.scope.AgenticScopeAccess;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.V;

/**
 * Supervisor agent interface for banking customer support.
 * This agent is responsible for routing customer requests to specialized domain agents.
 * 
 * The supervisor analyzes customer requests and delegates them to the most appropriate agent:
 * - AccountAgent: Account information, balance inquiries, payment methods
 * - TransactionHistoryAgent: Transaction history queries and payment tracking
 * - PaymentAgent: Bill payments, invoice scanning, payment submissions
 * 
 * Extends AgenticScopeAccess to allow retrieving the AgenticScope after execution,
 * which provides access to the agent invocation history (thought process).
 */
public interface SupervisorAgent extends AgenticScopeAccess {
    
    /**
     * Process a customer request and route to appropriate domain specialist.
     * 
     * @param conversationId Unique ID for tracking conversation memory
     * @param userMessage The customer's request message
     * @return The banking assistant's response
     */
    @SystemMessage(fromResource = "prompts/supervisor-agent-prompt.txt")
    @Agent(description = "Routes banking customer requests to specialized domain agents")
    String chat(@MemoryId String conversationId, @V("request") String userMessage);
}
