package com.microsoft.openai.samples.assistant.langchain4j.agent.builder;



import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;


import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * Builder for creating Account MCP Agent using langchain4j-agentic module.
 * This agent handles account information retrieval and management tasks.
 */
public class AccountMCPAgentBuilder {

    
    private final ChatModel chatModel;
    private final String loggedUserName;
    private final String accountMCPServerUrl;

    public AccountMCPAgentBuilder(ChatModel chatModel, String loggedUserName, String accountMCPServerUrl) {
        if (chatModel == null) {
            throw new IllegalArgumentException("chatModel cannot be null");
        }
        if (loggedUserName == null || loggedUserName.isEmpty()) {
            throw new IllegalArgumentException("loggedUserName cannot be null or empty");
        }
        if (accountMCPServerUrl == null || accountMCPServerUrl.isEmpty()) {
            throw new IllegalArgumentException("accountMCPServerUrl cannot be null or empty");
        }

        this.chatModel = chatModel;
        this.loggedUserName = loggedUserName;
        this.accountMCPServerUrl = accountMCPServerUrl;
    }

    /**
     * Builds the Account MCP Agent using the declarative approach with @Agent interface.
     * 
     * @return AccountAgent instance
     */
    public AccountAgent buildDeclarative() {
        // Create MCP client for account service
        McpClient mcpClient = DefaultMcpClient.builder()
                .transport(HttpMcpTransport.builder()
                        .logRequests(true)
                        .logResponses(true)
                        .sseUrl(accountMCPServerUrl)
                        .build())
                .build();


        // Build agent using AgenticServices with declarative interface
        return AgenticServices.agentBuilder(AccountAgent.class)
                .chatModel(chatModel)
                .toolProvider(McpToolProvider.builder().mcpClients(mcpClient).build())
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(20)
                .build())
                .build();
    }

    /**
     * Builds the Account MCP Agent using the programmatic approach with ReAct pattern.
     * 
     * @return AccountAgent instance
     */
    public AccountAgent buildProgrammatic() {
        // Create MCP client for account service
        McpClient mcpClient = DefaultMcpClient.builder()
                .transport(HttpMcpTransport.builder()
                        .logRequests(true)
                        .logResponses(true)
                        .sseUrl(accountMCPServerUrl)
                        .build())
                .build();

        // Build agent using AgenticServices programmatic API
        return AgenticServices.agentBuilder(AccountAgent.class)
                .chatModel(chatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(20)
                .build())
                .toolProvider(McpToolProvider.builder().mcpClients(mcpClient).build())
                .build();
    }

    /**
     * Declarative agent interface for Account operations.
     * This approach provides type-safe agent definition with annotations.
     */
    public interface AccountAgent {

        @SystemMessage(fromResource = "prompts/account-agent-prompt.txt")
        @Agent(description = "Retrieves bank account information, balances, and payment methods")
        String chat(@MemoryId String conversationId, @UserMessage String userMessage);
    }

    /**
     * Augment user message with logged user context.
     * This ensures the agent and MCP tools know which user is making the request.
     */
    public String augmentUserMessage(String userMessage) {
        return "userName: " + loggedUserName + "\n" + userMessage;
    }
}
