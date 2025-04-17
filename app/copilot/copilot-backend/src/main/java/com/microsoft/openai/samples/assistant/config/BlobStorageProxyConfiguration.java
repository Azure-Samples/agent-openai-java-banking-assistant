// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.assistant.config;

import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.core.credential.TokenCredential;
import com.microsoft.openai.samples.assistant.invoice.DocumentIntelligenceInvoiceScanHelper;
import com.microsoft.openai.samples.assistant.langchain4j.agent.mcp.AccountMCPAgent;
import com.microsoft.openai.samples.assistant.langchain4j.agent.mcp.PaymentMCPAgent;
import com.microsoft.openai.samples.assistant.langchain4j.agent.mcp.TransactionHistoryMCPAgent;
import com.microsoft.openai.samples.assistant.proxy.BlobStorageProxy;
import com.microsoft.openai.samples.assistant.security.LoggedUserService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BlobStorageProxyConfiguration {
    @Value("${storage-account.service}")
    String storageAccountServiceName;
    @Value("${blob.container.name}")
    String containerName;

    @Bean
    public BlobStorageProxy blobStorageProxy(TokenCredential tokenCredential) {
            return new BlobStorageProxy(storageAccountServiceName,containerName,tokenCredential);
    }

}
