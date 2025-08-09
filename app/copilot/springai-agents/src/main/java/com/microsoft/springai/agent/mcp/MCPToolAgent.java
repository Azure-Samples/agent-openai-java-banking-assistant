package com.microsoft.springai.agent.mcp;

import com.microsoft.springai.agent.AbstractReActAgent;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;

import java.time.Duration;
import java.util.*;

public abstract class MCPToolAgent extends AbstractReActAgent {
    private static final Logger LOGGER = LoggerFactory.getLogger(MCPToolAgent.class);

    protected List<ToolCallback> toolSpecifications;
    protected List<McpSyncClient> mcpClients;
    protected Map<String, McpSyncClient> tool2ClientMap;

    protected MCPToolAgent(ChatModel chatModel, List<MCPServerMetadata> mcpServerMetadata) {
        super(chatModel);
        this.mcpClients = new ArrayList<>();
        this.tool2ClientMap = new HashMap<>();
        this.toolSpecifications = new ArrayList<>();

        mcpServerMetadata.forEach(metadata -> {
            //only SSE is supported
            if (metadata.protocolType()
                    .equals(MCPProtocolType.SSE)) {

                McpClientTransport mcpClientTransport = HttpClientSseClientTransport
                        .builder(metadata.url()
                                .contains("/sse") ? metadata.url()
                                .substring(0, metadata.url()
                                        .lastIndexOf("/sse")) : metadata.url())
                        .sseEndpoint("/sse")
                        .build();

                LOGGER.info("Initializing MCP Client for server: {} at URL: {}", metadata.serverName(), metadata.url());

                McpSyncClient mcpSyncClient = McpClient.sync(mcpClientTransport)
                        .requestTimeout(Duration.ofSeconds(30))
                        .loggingConsumer(notification -> {
                            System.out.println("Received log message: " + notification.data());
                        })
                        .build();

                mcpSyncClient.initialize();
                mcpSyncClient.setLoggingLevel(McpSchema.LoggingLevel.DEBUG);

                ToolCallbackProvider provider = new SyncMcpToolCallbackProvider(mcpSyncClient);
                ToolCallback[] toolCallbacks = provider.getToolCallbacks();

                LOGGER.info("Found {} tools in MCP server: {}", Arrays.stream(toolCallbacks)
                        .map(toolCallback -> toolCallback.getToolDefinition()
                                .name())
                        .toList(), metadata.serverName());

                Arrays.stream(toolCallbacks)
                        .forEach(toolCallback -> {
                            this.tool2ClientMap.put(toolCallback.getToolDefinition()
                                    .name(), mcpSyncClient);
                            this.toolSpecifications.add(toolCallback);
                        });

                this.mcpClients.add(mcpSyncClient);
            }

        });

    }

    @Override
    protected List<ToolCallback> getToolSpecifications() {
        LOGGER.info("Found {} tools in MCP servers", this.toolSpecifications.stream()
                .map(toolCallback -> toolCallback.getToolDefinition()
                        .name())
                .toList());
        return this.toolSpecifications;
    }


    @Override
    protected ToolExecutionResult executeToolRequests(ChatResponse chatResponse, Prompt prompt, ToolCallingManager toolCallingManager) {
        return toolCallingManager.executeToolCalls(prompt, chatResponse);
    }
}