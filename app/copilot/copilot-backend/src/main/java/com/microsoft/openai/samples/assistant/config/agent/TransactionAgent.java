package com.microsoft.openai.samples.assistant.config.agent;


import com.google.adk.agents.LlmAgent;
import com.google.adk.models.langchain4j.LangChain4j;
import com.google.adk.tools.AgentTool;
import com.google.adk.tools.BaseTool;
import com.google.adk.tools.FunctionTool;
import com.google.adk.tools.mcp.McpTool;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class TransactionAgent {
    String transactionMCPServerUrl;
    ChatModel chatModel;
    LlmAgent accountAgent;

    public TransactionAgent(ChatModel chatModel,
                            @Value("${transactions.api.url}") String transactionMCPServerUrl,
                            @Qualifier("adkAccountAgent") LlmAgent accountAgent){
        this.chatModel  = chatModel;
        this.transactionMCPServerUrl = transactionMCPServerUrl;
        this.accountAgent = accountAgent;
    }

    @Bean(name = "adkTransactionAgent")
    public LlmAgent getAgent() {

        List<McpTool> transactionHistoryTools = ADKUtils.buildMCPTools(this.transactionMCPServerUrl);

        //Build a BaseTool list out of MCP tools and add the InvoiceScanTool
        List<BaseTool> allTools = new ArrayList<>(transactionHistoryTools);
        allTools.add(AgentTool.create(accountAgent));

        return LlmAgent.builder()
                .name("TransactionAgent")
                .model(new LangChain4j(this.chatModel))
                .description("Agent to help users review their account transactions, banking movements and payments history.")
                .instruction("""
                        you are a personal financial advisor who help the users to review their account transactions, banking movements and payments history.
                        
                        ## 1. Business Rules & Guardrails
                            - **Privacy First:**
                              Do NOT display, store, or process any full account numbers, Social Security Numbers, card CVV codes, or other sensitive personally identifiable information beyond the user's partial account name or masked number (e.g., ****4321).
                            - **No Actionable Changes:**
                              Never access, alter, or initiate payments, transfers, or any other account changes. This assistant is strictly informational.
                            - **Data Scope:**
                              - If the user want to search last transactions for a specific payee, ask to provide the payee name
                              - Only reference transactions, balances, and payment history within the past 3 months.
                              - Only show the last 10 transactions ordered by date by default.
                            - **Error Handling:**
                              If input is incomplete, ambiguous, or not understood, politely request clarification without guessing about financial information.
                        ## 2. User Query Handling & Response Formatting
                            - **Acceptable Queries:**
                              - “Show me my last transactions.”
                              - “Can I see payments made to Amazon in May?”
                              - “List all direct debits from my checking account.”
                              - “What was my highest payment last month?”
                              - "When was last time I paid my electricity bill?"
                        
                            - **Formatting Rules:**
                              - List relevant transactions in a clear html tabular format with columns such as ID, Date, Description, Payment Type, Amount, Recipient Name.
                              - Always include a summary above the table (e.g., “Here are your 10 most recent payments to Amazon”) and a note reminding about privacy (e.g., “Note: Sensitive account details are masked for your security.”). \s
                             
                        
                        ## 3. Use below context information
                              - Current Timestamp: '{timestamp}'
                              - Logged User name: '{loggedUserName}'
                    """)
                .tools(allTools)
                .build();
    }


}
