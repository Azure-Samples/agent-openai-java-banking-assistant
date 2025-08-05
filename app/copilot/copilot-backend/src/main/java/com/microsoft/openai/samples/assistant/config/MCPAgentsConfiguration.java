// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.assistant.config;

import com.microsoft.openai.samples.assistant.invoice.DocumentIntelligenceInvoiceScanHelper;
import com.microsoft.openai.samples.assistant.springai.agent.SupervisorAgent;
import com.microsoft.openai.samples.assistant.springai.agent.mcp.AccountMCPAgent;
import com.microsoft.openai.samples.assistant.springai.agent.mcp.PaymentMCPAgent;
import com.microsoft.openai.samples.assistant.springai.agent.mcp.TransactionHistoryMCPAgent;
import com.microsoft.openai.samples.assistant.security.LoggedUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class MCPAgentsConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(MCPAgentsConfiguration.class);
    @Value("${transactions.api.url}")
    String transactionsMCPServerUrl;
    @Value("${accounts.api.url}")
    String accountsMCPServerUrl;
    @Value("${payments.api.url}")
    String paymentsMCPServerUrl;

    private final ChatModel chatModel;
    private final LoggedUserService loggedUserService;
    private final DocumentIntelligenceInvoiceScanHelper documentIntelligenceInvoiceScanHelper;

    public MCPAgentsConfiguration(ChatModel chatModel, LoggedUserService loggedUserService, DocumentIntelligenceInvoiceScanHelper documentIntelligenceInvoiceScanHelper) {
        this.chatModel = chatModel;
        this.loggedUserService = loggedUserService;
        this.documentIntelligenceInvoiceScanHelper = documentIntelligenceInvoiceScanHelper;
    }

    @Bean
    public AccountMCPAgent accountMCPAgent() {
        return new AccountMCPAgent(chatModel, loggedUserService.getLoggedUser()
                .username(), accountsMCPServerUrl);
    }

    @Bean
    public TransactionHistoryMCPAgent transactionHistoryMCPAgent() {
        return new TransactionHistoryMCPAgent(chatModel, loggedUserService.getLoggedUser()
                .username(), transactionsMCPServerUrl, accountsMCPServerUrl);
    }

    @Bean
    public PaymentMCPAgent paymentMCPAgent() {
        return new PaymentMCPAgent(chatModel, documentIntelligenceInvoiceScanHelper, loggedUserService.getLoggedUser()
                .username(), transactionsMCPServerUrl, accountsMCPServerUrl, paymentsMCPServerUrl);
    }

    @Bean
    public SupervisorAgent supervisorAgent(ChatModel chatModel) {
        return new SupervisorAgent(chatModel,
                List.of(accountMCPAgent(),
                        transactionHistoryMCPAgent(),
                        paymentMCPAgent()));

    }

}
