// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.assistant.config;


import com.azure.ai.openai.OpenAIClient;

import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Langchain4JConfiguration {

    @Value("${openai.chatgpt.deployment}")
    private String gptChatDeploymentModelId;

    @Bean
    public ChatLanguageModel chatLanguageModel(OpenAIClient azureOpenAICLient) {

        return AzureOpenAiChatModel.builder()
                .openAIClient(azureOpenAICLient)
                .deploymentName(gptChatDeploymentModelId)
                .temperature(0.3)
                .logRequestsAndResponses(true)
                .build();
    }


}
