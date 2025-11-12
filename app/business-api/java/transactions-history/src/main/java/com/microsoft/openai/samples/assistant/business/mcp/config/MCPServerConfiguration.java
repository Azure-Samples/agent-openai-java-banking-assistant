package com.microsoft.openai.samples.assistant.business.mcp.config;

import com.microsoft.openai.samples.assistant.business.mcp.server.TransactionMCPService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MCPServerConfiguration {

    @Bean
    public ToolCallbackProvider transactionTools(TransactionMCPService transactionMCPService) {
        return MethodToolCallbackProvider.builder().toolObjects(transactionMCPService).build();
    }
}