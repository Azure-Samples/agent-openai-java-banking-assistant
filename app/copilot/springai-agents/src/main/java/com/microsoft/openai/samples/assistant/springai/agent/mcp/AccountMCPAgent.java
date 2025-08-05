package com.microsoft.openai.samples.assistant.springai.agent.mcp;

import com.microsoft.springai.agent.AgentMetadata;
import com.microsoft.springai.agent.mcp.MCPProtocolType;
import com.microsoft.springai.agent.mcp.MCPServerMetadata;
import com.microsoft.springai.agent.mcp.MCPToolAgent;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;

import java.util.List;
import java.util.Map;

public class AccountMCPAgent extends MCPToolAgent {

    private final Prompt agentPrompt;

    private static final String ACCOUNT_AGENT_SYSTEM_MESSAGE = """
             you are a personal financial advisor who help the user to retrieve information about their bank accounts.
             Use html list or table to display the account information.
             Always use the below logged user details to retrieve account info:
             '{loggedUserName}'
            """;

    public AccountMCPAgent(ChatModel chatModel, String loggedUserName, String accountMCPServerUrl) {
        super(chatModel, List.of(new MCPServerMetadata("account", accountMCPServerUrl, MCPProtocolType.SSE)));

        if (loggedUserName == null || loggedUserName.isEmpty()) {
            throw new IllegalArgumentException("loggedUserName cannot be null or empty");
        }

        PromptTemplate promptTemplate = PromptTemplate.builder()
                .template(ACCOUNT_AGENT_SYSTEM_MESSAGE)
                .build();
        this.agentPrompt = promptTemplate.create(Map.of("loggedUserName", loggedUserName));
    }

    @Override
    public String getName() {
        return "AccountAgent";
    }

    @Override
    public AgentMetadata getMetadata() {
        return new AgentMetadata(
                "Personal financial advisor for retrieving bank account information.",
                List.of("RetrieveAccountInfo", "DisplayAccountDetails")
        );
    }

    @Override
    protected String getSystemMessage() {
        return agentPrompt.getContents();
    }

}
