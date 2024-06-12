// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.assistant.config;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAIConfiguration {

    @Value("${openai.service}")
    String openAIServiceName;

    @Value("${openai.chatgpt.deployment}")
    private String gptChatDeploymentModelId;

    final TokenCredential tokenCredential;

    public OpenAIConfiguration(TokenCredential tokenCredential) {
        this.tokenCredential = tokenCredential;
    }

    @Bean
    @ConditionalOnProperty(name = "openai.tracing.enabled", havingValue = "true")
    public OpenAIClient openAItracingEnabledClient() {
        String endpoint = "https://%s.openai.azure.com".formatted(openAIServiceName);

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
        String endpoint = "https://%s.openai.azure.com".formatted(openAIServiceName);
        return new OpenAIClientBuilder()
                .endpoint(endpoint)
                .credential(tokenCredential)
                .buildClient();
    }

    @Bean
    @ConditionalOnProperty(name = "openai.tracing.enabled", havingValue = "true")
    public OpenAIAsyncClient tracingEnabledAsyncClient() {
        String endpoint = "https://%s.openai.azure.com".formatted(openAIServiceName);

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
        String endpoint = "https://%s.openai.azure.com".formatted(openAIServiceName);
        return new OpenAIClientBuilder()
                .endpoint(endpoint)
                .credential(tokenCredential)
                .buildAsyncClient();
    }
}
