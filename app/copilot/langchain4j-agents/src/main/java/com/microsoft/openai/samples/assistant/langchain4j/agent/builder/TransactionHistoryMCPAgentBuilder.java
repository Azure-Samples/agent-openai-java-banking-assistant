package com.microsoft.openai.samples.assistant.langchain4j.agent.builder;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

import java.time.Duration;

import java.time.ZonedDateTime;
import java.time.ZoneId;

/**
 * Builder for creating Transaction History MCP Agent using langchain4j-agentic module.
 * This agent handles transaction history retrieval and management tasks.
 * It connects to both transaction and account MCP servers.
 */
public class TransactionHistoryMCPAgentBuilder {

    private final ChatModel chatModel;
    private final String loggedUserName;
    private final String transactionMCPServerUrl;
    private final String accountMCPServerUrl;

    public TransactionHistoryMCPAgentBuilder(
            ChatModel chatModel,
            String loggedUserName,
            String transactionMCPServerUrl,
            String accountMCPServerUrl) {
        
        if (chatModel == null) {
            throw new IllegalArgumentException("chatModel cannot be null");
        }
        if (loggedUserName == null || loggedUserName.isEmpty()) {
            throw new IllegalArgumentException("loggedUserName cannot be null or empty");
        }
        if (transactionMCPServerUrl == null || transactionMCPServerUrl.isEmpty()) {
            throw new IllegalArgumentException("transactionMCPServerUrl cannot be null or empty");
        }
        if (accountMCPServerUrl == null || accountMCPServerUrl.isEmpty()) {
            throw new IllegalArgumentException("accountMCPServerUrl cannot be null or empty");
        }

        this.chatModel = chatModel;
        this.loggedUserName = loggedUserName;
        this.transactionMCPServerUrl = transactionMCPServerUrl;
        this.accountMCPServerUrl = accountMCPServerUrl;
    }

    /**
     * Builds the Transaction History MCP Agent using the declarative approach with @Agent interface.
     * 
     * @return TransactionHistoryAgent instance
     */
    public Object buildDeclarative() {

         McpTransport transactionTransport = new HttpMcpTransport.Builder()
                .sseUrl(transactionMCPServerUrl)
                .timeout(Duration.ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();
        // Create MCP client for transaction history service
        McpClient transactionMcpClient = DefaultMcpClient.builder()
                .transport(transactionTransport)
                .build();

        // Create MCP client for account service
        McpClient accountMcpClient = DefaultMcpClient.builder()
                .transport(HttpMcpTransport.builder()
                        .logRequests(true)
                        .logResponses(true)
                        .sseUrl(accountMCPServerUrl)
                        .build())
                .build();

        // Build agent using AgenticServices with declarative interface
        // Note: Multiple MCP clients can be added via McpToolProvider
        return AgenticServices.agentBuilder(TransactionHistoryAgent.class)
                .chatModel(chatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(20)
                .build())
                .toolProvider(McpToolProvider.builder().mcpClients(transactionMcpClient, accountMcpClient).build())
                .build();
    }

    /**
     * Builds the Transaction History MCP Agent using the programmatic approach with ReAct pattern.
     * 
     * @return TransactionHistoryAgent instance
     */
    public Object buildProgrammatic() {
        // Create MCP client for transaction history service
        McpClient transactionMcpClient = DefaultMcpClient.builder()
                .transport(HttpMcpTransport.builder()
                        .logRequests(true)
                        .logResponses(true)
                        .sseUrl(transactionMCPServerUrl)
                        .build())
                .build();

        // Create MCP client for account service
        McpClient accountMcpClient = DefaultMcpClient.builder()
                .transport(HttpMcpTransport.builder()
                        .logRequests(true)
                        .logResponses(true)
                        .sseUrl(accountMCPServerUrl)
                        .build())
                .build();

        // Get current timestamp
        String currentDateTime = ZonedDateTime.now(ZoneId.of("UTC")).toInstant().toString();

        // Build agent using AgenticServices programmatic API
        return AgenticServices.agentBuilder(TransactionHistoryAgent.class)
                .chatModel(chatModel)
            .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
            .id(memoryId)
            .maxMessages(20)
            .build())
                .toolProvider(McpToolProvider.builder().mcpClients(transactionMcpClient, accountMcpClient).build())
                .build();
    }

    /**
     * Declarative agent interface for Transaction History operations.
     * This approach provides type-safe agent definition with annotations.
     */
    public interface TransactionHistoryAgent {

        @SystemMessage(fromResource = "prompts/transaction-history-agent-prompt.txt")
        @Agent(description = "Manages transaction history queries and payment tracking")
        String chat(@MemoryId String conversationId, @UserMessage String userMessage);
    }

    /**
     * Augment user message with logged user context and current timestamp.
     * This ensures the agent and MCP tools know which user is making the request.
     */
    public String augmentUserMessage(String userMessage) {
        String currentDateTime = ZonedDateTime.now(ZoneId.of("UTC")).toInstant().toString();
        return "User: " + loggedUserName + " | Timestamp: " + currentDateTime + "\n" + userMessage;
    }
}
