package com.microsoft.openai.samples.assistant.config.agent.isolated;


import com.google.adk.agents.LlmAgent;
import com.google.adk.models.langchain4j.LangChain4j;
import com.google.adk.tools.AgentTool;
import com.google.adk.tools.BaseTool;
import com.google.adk.tools.mcp.McpTool;
import com.microsoft.openai.samples.assistant.config.agent.ADKUtils;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class TransactionAgentNoDependencies {
    String transactionMCPServerUrl;
    ChatModel chatModel;

    public TransactionAgentNoDependencies(ChatModel chatModel,
                                          @Value("${transactions.api.url}") String transactionMCPServerUrl){
        this.chatModel  = chatModel;
        this.transactionMCPServerUrl = transactionMCPServerUrl;
    }

    @Bean(name = "adkTransactionAgentNoDependencies")
    public LlmAgent getAgent() {

        List<McpTool> transactionHistoryTools = ADKUtils.buildMCPTools(this.transactionMCPServerUrl);

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
                              - To search about the payments history you need to know the payee name and the account id.
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
                              - List relevant transactions in a clear tabular format with columns such as ID, Date, Description, Payment Type, Amount, Recipient Name.
                              - Always include a summary above the table (e.g., “Here are your 10 most recent payments to Amazon”) and a note reminding about privacy (e.g., “Note: Sensitive account details are masked for your security.”). \s
                            
                            - **Example Output:** \s
                        
                                Here are your 10 most recent transactions in your Checking account (**data as of June 13, 2024**):
                        
                                | ID        | Date       | Description                | Recipient |Type          | Amount    | 
                                |-----------|------------|----------------------------|-----------|--------------|-----------|
                                |15526te53  | 06/12/2024 | order 123312               | Amazon    |Bank Trasfer  | $45.00    | 
                                |shj467466  | 06/11/2024 | Payroll                    | contoso   |Bank Transfer | €1.200,20 | 
                                |1256265ww  | 06/10/2024 | Payment of the bill 334398 | acme      | Credit Card  | $5.11     | 
                        
                        ## 3. Use below context information
                              - Current Timestamp: '{timestamp}'
                    """)
                .tools(transactionHistoryTools)
                .build();
    }


}
