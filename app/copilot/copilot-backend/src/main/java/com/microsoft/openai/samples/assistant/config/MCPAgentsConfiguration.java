// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.assistant.config;

import com.microsoft.openai.samples.assistant.invoice.DocumentIntelligenceInvoiceScanHelper;
import com.microsoft.openai.samples.assistant.langchain4j.agent.SupervisorAgent;
import com.microsoft.openai.samples.assistant.langchain4j.agent.builder.AccountMCPAgentBuilder;
import com.microsoft.openai.samples.assistant.langchain4j.agent.builder.PaymentMCPAgentBuilder;
import com.microsoft.openai.samples.assistant.langchain4j.agent.builder.SupervisorAgentBuilder;
import com.microsoft.openai.samples.assistant.langchain4j.agent.builder.TransactionHistoryMCPAgentBuilder;
import com.microsoft.openai.samples.assistant.security.LoggedUserService;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for MCP-based agents using langchain4j-agentic module.
 * This configuration has been migrated from custom agent framework to use the official
 * langchain4j-agentic builders for better maintainability and access to new features.
 */
@Configuration
public class MCPAgentsConfiguration {
    @Value("${transactions.api.url}") String transactionsMCPServerUrl;
    @Value("${accounts.api.url}") String accountsMCPServerUrl;
    @Value("${payments.api.url}") String paymentsMCPServerUrl;

    private final ChatModel chatModel;
    private final LoggedUserService loggedUserService;
    private final DocumentIntelligenceInvoiceScanHelper documentIntelligenceInvoiceScanHelper;

    public MCPAgentsConfiguration(
            ChatModel chatModel, 
            LoggedUserService loggedUserService, 
            DocumentIntelligenceInvoiceScanHelper documentIntelligenceInvoiceScanHelper) {
        this.chatModel = chatModel;
        this.loggedUserService = loggedUserService;
        this.documentIntelligenceInvoiceScanHelper = documentIntelligenceInvoiceScanHelper;
    }

    /**
     * Creates the Account MCP Agent bean using the builder pattern.
     * This agent handles account-related queries and operations.
     * 
     * @return Account agent instance
     */
    @Bean
    public Object accountMCPAgent() {
        AccountMCPAgentBuilder builder = new AccountMCPAgentBuilder(
                chatModel, 
                loggedUserService.getLoggedUser().username(), 
                accountsMCPServerUrl
        );
        // Using programmatic approach for consistent API across all agents
        return builder.buildProgrammatic();
    }

    /**
     * Creates the Transaction History MCP Agent bean using the builder pattern.
     * This agent handles transaction history queries and searches.
     * 
     * @return Transaction history agent instance
     */
    @Bean
    public Object transactionHistoryMCPAgent() {
        TransactionHistoryMCPAgentBuilder builder = new TransactionHistoryMCPAgentBuilder(
                chatModel, 
                loggedUserService.getLoggedUser().username(), 
                transactionsMCPServerUrl, 
                accountsMCPServerUrl
        );
        // Using programmatic approach for consistent API across all agents
        return builder.buildProgrammatic();
    }

    /**
     * Creates the Payment MCP Agent bean using the builder pattern.
     * This agent handles payment processing, invoice scanning, and payment submissions.
     * 
     * @return Payment agent instance
     */
    @Bean
    public Object paymentMCPAgent() {
        PaymentMCPAgentBuilder builder = new PaymentMCPAgentBuilder(
                chatModel,
                documentIntelligenceInvoiceScanHelper, 
                loggedUserService.getLoggedUser().username(),
                paymentsMCPServerUrl
        );
        // Using programmatic approach for consistent API across all agents
        return builder.buildProgrammatic();
    }

    /**
     * Creates the Supervisor Agent bean using the builder pattern.
     * The supervisor routes user requests to the appropriate domain-specific agent.
     * 
     * @return Supervisor agent instance
     */
    @Bean
    public SupervisorAgent supervisorAgent() {
        SupervisorAgentBuilder builder = new SupervisorAgentBuilder(
                chatModel,
                accountMCPAgent(),
                transactionHistoryMCPAgent(),
                paymentMCPAgent()
        );
        // Using programmatic supervisor builder for multi-agent orchestration
        return (SupervisorAgent) builder.buildProgrammatic();
    }
}

