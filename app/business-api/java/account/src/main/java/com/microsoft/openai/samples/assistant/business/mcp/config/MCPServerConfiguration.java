package com.microsoft.openai.samples.assistant.business.mcp.config;


import com.microsoft.openai.samples.assistant.business.mcp.server.AccountMCPService;
import com.microsoft.openai.samples.assistant.business.mcp.server.UserMCPService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MCPServerConfiguration {

    @Bean
    public ToolCallbackProvider accountTools(AccountMCPService accountMCPService) {
        return MethodToolCallbackProvider.builder().toolObjects(accountMCPService).build();
    }

    @Bean
    public ToolCallbackProvider userTools(UserMCPService userMCPService) {
        return MethodToolCallbackProvider.builder().toolObjects(userMCPService).build();
    }

}
