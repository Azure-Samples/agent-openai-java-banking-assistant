package com.microsoft.langchain4j.agent.mcp;

import com.microsoft.langchain4j.agent.AbstractReActAgent;

import com.microsoft.langchain4j.agent.AgentExecutionException;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.ToolExecutionResultMessage;

import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import dev.langchain4j.model.chat.ChatLanguageModel;

import dev.langchain4j.service.tool.ToolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class MCPToolAgent extends AbstractReActAgent {
    private static final Logger LOGGER = LoggerFactory.getLogger(MCPToolAgent.class);

    protected  List<ToolSpecification> toolSpecifications;
    protected  Map<String, ToolExecutor> extendedExecutorMap;
    protected List<McpClient> mcpClients;
    protected Map<String, McpClient> tool2ClientMap;

    protected MCPToolAgent(ChatLanguageModel chatModel, List<MCPServerMetadata> mcpServerMetadata) {
        super(chatModel);
        this.mcpClients = new ArrayList<>();
        this.tool2ClientMap = new HashMap<>();
        this.toolSpecifications = new ArrayList<>();
        this.extendedExecutorMap = new HashMap<>();

        mcpServerMetadata.forEach(metadata -> {
            //only SSE is supported
            if(metadata.protocolType().equals(MCPProtocolType.SSE)){
                McpTransport transport = new HttpMcpTransport.Builder()
                        .sseUrl(metadata.url())
                        .logRequests(true) // if you want to see the traffic in the log
                        .logResponses(true)
                        .timeout(Duration.ofHours(3))
                        .build();

                McpClient mcpClient = new DefaultMcpClient.Builder()
                        .transport(transport)
                        .build();
                mcpClient
                        .listTools()
                        .forEach(toolSpecification -> {
                            this.tool2ClientMap.put(toolSpecification.name(),mcpClient);
                            this.toolSpecifications.add(toolSpecification);
                            }
                        );
                this.mcpClients.add(mcpClient);

            }

        });

    }

    @Override
    protected List<ToolSpecification> getToolSpecifications() {
        return this.toolSpecifications;
    }


    @Override
    protected ToolExecutor getToolExecutor(String toolName) {
        throw new AgentExecutionException("getToolExecutor not required when using MCP. if you landed here please review your agent code");
    }

    protected List<ToolExecutionResultMessage> executeToolRequests(List<ToolExecutionRequest> toolExecutionRequests) {
        List<ToolExecutionResultMessage> toolExecutionResultMessages = new ArrayList<>();
        for (ToolExecutionRequest toolExecutionRequest : toolExecutionRequests) {

            String result = "ko";

            // try first the extended executors
            var toolExecutor = extendedExecutorMap.get(toolExecutionRequest.name());
            if( toolExecutor != null){
                LOGGER.info("Executing {} with params {}", toolExecutionRequest.name(), toolExecutionRequest.arguments());
                 result = toolExecutor.execute(toolExecutionRequest,null);
                LOGGER.info("Response from {}: {}", toolExecutionRequest.name(), result);

            }else{
                var mcpClient = tool2ClientMap.get(toolExecutionRequest.name());
                if (mcpClient == null) {
                    throw new IllegalArgumentException("No MCP executor found for tool name: " + toolExecutionRequest.name());
                }
                LOGGER.info("Executing {} with params {}", toolExecutionRequest.name(), toolExecutionRequest.arguments());
                result =  mcpClient.executeTool(toolExecutionRequest);
                LOGGER.info("Response from {}: {}", toolExecutionRequest.name(), result);
            }

            if (result == null || result.isEmpty()) {
                LOGGER.warn("Tool {} returned empty result but successfully completed. Setting result=ok.", toolExecutionRequest.name());
                result = "ok";
            }
            toolExecutionResultMessages.add(ToolExecutionResultMessage.from(toolExecutionRequest, result));
        }
        return toolExecutionResultMessages;
    }
}