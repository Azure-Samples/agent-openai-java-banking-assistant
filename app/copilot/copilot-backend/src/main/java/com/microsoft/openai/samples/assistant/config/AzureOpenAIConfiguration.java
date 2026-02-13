// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.assistant.config;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureOpenAIConfiguration {

    @Value("${openai.service}")
    String openAIServiceName;

    @Value("${openai.endpoint:}")
    String openAIEndpoint;

    @Value("${openai.chatgpt.deployment}")
    private String gptChatDeploymentModelId;

    final TokenCredential tokenCredential;

    public AzureOpenAIConfiguration(TokenCredential tokenCredential) {
        this.tokenCredential = tokenCredential;
    }

    /**
     * Resolves the AI Foundry endpoint. If AZURE_OPENAI_ENDPOINT is set, use it directly.
     * Otherwise, fall back to constructing from the service name (legacy OpenAI pattern).
     */
    private String resolveEndpoint() {
        if (openAIEndpoint != null && !openAIEndpoint.isBlank()) {
            return openAIEndpoint;
        }
        return "https://%s.openai.azure.com".formatted(openAIServiceName);
    }

    @Bean
    public ChatModel chatModel() {
        String endpoint = resolveEndpoint();
        return AzureOpenAiChatModel.builder()
                .endpoint(endpoint)
                .tokenCredential(tokenCredential)
                .deploymentName(gptChatDeploymentModelId)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "openai.tracing.enabled", havingValue = "true")
    public OpenAIClient openAItracingEnabledClient() {
        String endpoint = resolveEndpoint();

        var httpLogOptions = new HttpLogOptions();
        // httpLogOptions.setPrettyPrintBody(true);
        httpLogOptions.setLogLevel(HttpLogDetailLevel.BODY);

        return new OpenAIClientBuilder()
                .endpoint(endpoint)
                .credential(tokenCredential)
                .httpLogOptions(httpLogOptions)
                .buildClient();

    }

    @Bean
    @ConditionalOnProperty(name = "openai.tracing.enabled", havingValue = "false")
    public OpenAIClient openAIDefaultClient() {
        String endpoint = resolveEndpoint();
        return new OpenAIClientBuilder()
                .endpoint(endpoint)
                .credential(tokenCredential)
                .buildClient();
    }

    @Bean
    @ConditionalOnProperty(name = "openai.tracing.enabled", havingValue = "true")
    public OpenAIAsyncClient tracingEnabledAsyncClient() {
        String endpoint = resolveEndpoint();

        var httpLogOptions = new HttpLogOptions();
        httpLogOptions.setPrettyPrintBody(true);
        httpLogOptions.setLogLevel(HttpLogDetailLevel.BODY);

        return new OpenAIClientBuilder()
                .endpoint(endpoint)
                .credential(tokenCredential)
                .httpLogOptions(httpLogOptions)
                .buildAsyncClient();
    }

    @Bean
    @ConditionalOnProperty(name = "openai.tracing.enabled", havingValue = "false")
    public OpenAIAsyncClient defaultAsyncClient() {
        String endpoint = resolveEndpoint();
        return new OpenAIClientBuilder()
                .endpoint(endpoint)
                .credential(tokenCredential)
                .buildAsyncClient();
    }
}
