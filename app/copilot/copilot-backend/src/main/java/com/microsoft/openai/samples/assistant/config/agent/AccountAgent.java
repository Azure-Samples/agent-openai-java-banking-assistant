package com.microsoft.openai.samples.assistant.config.agent;


import com.google.adk.agents.LlmAgent;
import com.google.adk.models.langchain4j.LangChain4j;
import com.google.adk.tools.mcp.McpTool;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
public class AccountAgent {
    String accountsMCPServerUrl;
    ChatModel chatModel;

    public AccountAgent(ChatModel chatModel,  @Value("${accounts.api.url}") String accountsMCPServerUrl){
        this.chatModel  = chatModel;
        this.accountsMCPServerUrl = accountsMCPServerUrl;
    }

    @Bean(name = "adkAccountAgent")
    public LlmAgent getAgent() {

        List<McpTool> accountTools = ADKUtils.buildMCPTools(accountsMCPServerUrl);

        return LlmAgent.builder()
                .name("AccountAgent")
                .model(new LangChain4j(this.chatModel))
                .description("Agent to retrieve information about bank accounts, payment methods, and credit cards details and beneficiaries registered code. It requires logged username to retrieve the account information.")
                .instruction("""
                    you are a personal financial advisor who help the users to retrieve information about their bank accounts, payment methods, credit cards.
                    Use html list or table to display the account information.
                    Always use the below logged user details to retrieve account info:
                    '{{loggedUserName}}'
                    """)
                .tools(accountTools)
                .build();
    }


}
