package com.microsoft.openai.samples.assistant.langchain4j.agent.builder;

import com.microsoft.openai.samples.assistant.invoice.DocumentIntelligenceInvoiceScanHelper;
import com.microsoft.openai.samples.assistant.langchain4j.tools.InvoiceScanTool;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

import java.time.ZonedDateTime;
import java.time.ZoneId;

/**
 * Builder for creating Payment MCP Agent using langchain4j-agentic module.
 * This is the most complex agent as it:
 * - Connects to three MCP servers (payment, transaction, account)
 * - Includes a custom InvoiceScanTool for OCR processing
 * - Manages the complete payment workflow
 */
public class PaymentMCPAgentBuilder {

    private final ChatModel chatModel;
    private final DocumentIntelligenceInvoiceScanHelper documentIntelligenceHelper;
    private final String loggedUserName;
 
    private final String paymentMCPServerUrl;

    public PaymentMCPAgentBuilder(
            ChatModel chatModel,
            DocumentIntelligenceInvoiceScanHelper documentIntelligenceHelper,
            String loggedUserName,
            String paymentMCPServerUrl) {
        
        if (chatModel == null) {
            throw new IllegalArgumentException("chatModel cannot be null");
        }
        if (documentIntelligenceHelper == null) {
            throw new IllegalArgumentException("documentIntelligenceHelper cannot be null");
        }
        if (loggedUserName == null || loggedUserName.isEmpty()) {
            throw new IllegalArgumentException("loggedUserName cannot be null or empty");
        }
        if (paymentMCPServerUrl == null || paymentMCPServerUrl.isEmpty()) {
            throw new IllegalArgumentException("paymentMCPServerUrl cannot be null or empty");
        }

        this.chatModel = chatModel;
        this.documentIntelligenceHelper = documentIntelligenceHelper;
        this.loggedUserName = loggedUserName;
        this.paymentMCPServerUrl = paymentMCPServerUrl;
    }

    /**
     * Builds the Payment MCP Agent using the declarative approach with @Agent interface.
     * 
     * @return PaymentAgent instance
     */
    public Object buildDeclarative() {
        // Create custom InvoiceScanTool instance
        InvoiceScanTool invoiceScanTool = new InvoiceScanTool(documentIntelligenceHelper);

        // Create MCP client for payment service
        McpClient paymentMcpClient = DefaultMcpClient.builder()
                .transport(HttpMcpTransport.builder()
                        .logRequests(true)
                        .logResponses(true)
                        .sseUrl(paymentMCPServerUrl)
                        .build())
                .build();

      // Build agent using AgenticServices with declarative interface
        // Custom tool is added via tools() method
        return AgenticServices.agentBuilder(PaymentAgent.class)
                .chatModel(chatModel)
                .toolProvider(McpToolProvider.builder().mcpClients(paymentMcpClient).build())
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder().id(memoryId)
                .maxMessages(20)
                .build())
                .tools(invoiceScanTool)  // Add custom invoice scan tool
                .build();
    }

    /**
     * Builds the Payment MCP Agent using the programmatic approach with ReAct pattern.
     * 
     * @return PaymentAgent instance
     */
    public Object buildProgrammatic() {
        // Create custom InvoiceScanTool instance
        InvoiceScanTool invoiceScanTool = new InvoiceScanTool(documentIntelligenceHelper);

        // Create MCP client for payment service
        McpClient paymentMcpClient = DefaultMcpClient.builder()
                .transport(HttpMcpTransport.builder()
                        .logRequests(true)
                        .logResponses(true)
                        .sseUrl(paymentMCPServerUrl)
                        .build())
                .build();

        // Get current timestamp
        String currentDateTime = ZonedDateTime.now(ZoneId.of("UTC")).toInstant().toString();

        // Build agent using AgenticServices programmatic API
        return AgenticServices.agentBuilder(PaymentAgent.class)
                .chatModel(chatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(20)
                .build())
                .toolProvider(McpToolProvider.builder().mcpClients(paymentMcpClient).build())
                .tools(invoiceScanTool)  // Add custom invoice scan tool
                .build();
    }

    /**
     * Declarative agent interface for Payment operations.
     * This approach provides type-safe agent definition with annotations.
     */
    public interface PaymentAgent {

        @SystemMessage(fromResource = "prompts/payment-agent-prompt.txt")
        @Agent(description = "Processes bill payments, invoice scanning, and payment submissions")
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
