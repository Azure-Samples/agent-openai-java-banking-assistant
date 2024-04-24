// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.assistant.config;

import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.ai.documentintelligence.DocumentIntelligenceClientBuilder;
import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumentIntelligenceConfiguration {

    @Value("${documentintelligence.service}")
    String documentIntelligenceServiceName;

    final TokenCredential tokenCredential;

    public DocumentIntelligenceConfiguration(TokenCredential tokenCredential) {
        this.tokenCredential = tokenCredential;
    }

    @Bean
    public DocumentIntelligenceClient documentIntelligenceClient() {
        String endpoint = "https://%s.cognitiveservices.azure.com".formatted(documentIntelligenceServiceName);

        return new DocumentIntelligenceClientBuilder()
                .credential(tokenCredential)
                .endpoint(endpoint)
                .buildClient();
    }

}
