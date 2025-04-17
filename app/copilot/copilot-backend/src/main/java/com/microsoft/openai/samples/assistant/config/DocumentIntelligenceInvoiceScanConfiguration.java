// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.assistant.config;

import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.ai.documentintelligence.DocumentIntelligenceClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.microsoft.openai.samples.assistant.invoice.DocumentIntelligenceInvoiceScanHelper;
import com.microsoft.openai.samples.assistant.proxy.BlobStorageProxy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumentIntelligenceInvoiceScanConfiguration {

    @Value("${documentintelligence.service}")
    String documentIntelligenceServiceName;

    final TokenCredential tokenCredential;

    public DocumentIntelligenceInvoiceScanConfiguration(TokenCredential tokenCredential) {
        this.tokenCredential = tokenCredential;
    }

    @Bean
    public DocumentIntelligenceInvoiceScanHelper documentIntelligenceInvoiceScanHelper(BlobStorageProxy blobStorageProxy) {

        String endpoint = "https://%s.cognitiveservices.azure.com".formatted(documentIntelligenceServiceName);

        var documentIntelligenceClient = new DocumentIntelligenceClientBuilder()
                .credential(tokenCredential)
                .endpoint(endpoint)
                .buildClient();

            return new DocumentIntelligenceInvoiceScanHelper(documentIntelligenceClient,blobStorageProxy);
    }

}
