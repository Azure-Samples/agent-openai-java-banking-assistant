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

public class TransactionHistoryMCPAgent extends MCPToolAgent {

    private final Prompt agentPrompt;

    private static final String TRANSACTION_HISTORY_AGENT_SYSTEM_MESSAGE = """
             you are a personal financial advisor who help the user with their recurrent bill payments. To search about the payments history you need to know the payee name and the account id.
            If the user doesn't provide the payee name, search the last 10 transactions order by date.
            If the user want to search last transactions for a specific payee, ask to provide the payee name.
            Use html list or table to display the transaction information.
            Always use the below logged user details to retrieve account info:
            '{loggedUserName}'
            Current timestamp:
            '{currentDateTime}'
            """;

    public TransactionHistoryMCPAgent(ChatModel chatModel, String loggedUserName, String transactionMCPServerUrl, String accountMCPServerUrl) {
        super(chatModel, List.of(new MCPServerMetadata("transaction-history", transactionMCPServerUrl, MCPProtocolType.SSE),
                new MCPServerMetadata("account", accountMCPServerUrl, MCPProtocolType.SSE)));

        if (loggedUserName == null || loggedUserName.isEmpty()) {
            throw new IllegalArgumentException("loggedUserName cannot be null or empty");
        }

        PromptTemplate promptTemplate = PromptTemplate.builder()
                .template(TRANSACTION_HISTORY_AGENT_SYSTEM_MESSAGE)
                .build();
        var datetimeIso8601 = java.time.ZonedDateTime.now(java.time.ZoneId.of("UTC"))
                .toInstant()
                .toString();

        this.agentPrompt = promptTemplate.create(Map.of(
                "loggedUserName", loggedUserName,
                "currentDateTime", datetimeIso8601
        ));
    }

    @Override
    public String getName() {
        return "TransactionHistoryAgent";
    }

    @Override
    public AgentMetadata getMetadata() {
        return new AgentMetadata(
                "Personal financial advisor for retrieving transaction history information.",
                List.of("RetrieveTransactionHistory", "DisplayTransactionDetails")
        );
    }

    @Override
    protected String getSystemMessage() {
        return agentPrompt.getContents();
    }

}