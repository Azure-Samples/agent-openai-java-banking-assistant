package com.microsoft.openai.samples.assistant.config.agent;


import com.google.adk.agents.LlmAgent;
import com.google.adk.models.langchain4j.LangChain4j;
import com.google.adk.tools.AgentTool;
import com.google.adk.tools.BaseTool;
import com.google.adk.tools.FunctionTool;
import com.google.adk.tools.mcp.McpTool;
import com.microsoft.openai.samples.assistant.invoice.DocumentIntelligenceInvoiceScanHelper;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SupervisorAgent {
    ChatModel chatModel;
    LlmAgent accountAgent;
    LlmAgent transactionAgent;
    LlmAgent paymentAgent;

    public SupervisorAgent(ChatModel chatModel,

                           @Qualifier("adkAccountAgent") LlmAgent accountAgent,
                           @Qualifier("adkTransactionAgent") LlmAgent transactionAgent,
                           @Qualifier("adkPaymentAgent") LlmAgent paymentAgent
    ) {
        this.chatModel  = chatModel;
        this.accountAgent = accountAgent;
        this.transactionAgent = transactionAgent;
        this.paymentAgent = paymentAgent;
    }

    @Bean(name = "adkSupervisorAgent")
    public LlmAgent getAgent() {

        return LlmAgent.builder()
                .name("SupervisorAgent")
                .model(new LangChain4j(this.chatModel))
                .instruction("""
                 You are a banking customer support agent triaging conversation and coordinating agents work to solve the customer need.
                 #Coordination Rules
                    - If the task is about account details, and credit cards details and beneficiaries registered code select the AccountAgent.
                    - If the task is about banking movements, transactions or payments history select the TransactionAgent.
                    - If the task is about initiating and/or progress towards a payment task select the PaymentAgent.
          
                    """)
                .subAgents(accountAgent,transactionAgent,paymentAgent)
                .build();


    }


}
