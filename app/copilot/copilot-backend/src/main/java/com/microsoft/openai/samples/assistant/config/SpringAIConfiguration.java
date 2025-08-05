package com.microsoft.openai.samples.assistant.config;

import com.azure.ai.openai.OpenAIClientBuilder;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.azure.openai.AzureOpenAiChatOptions;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringAIConfiguration {

    @Value("${openai.chatgpt.deployment}")
    private String gptChatDeploymentModelId;

    @Bean
    public ChatModel chatModel(OpenAIClientBuilder azureOpenAIClientBuilder) {
        AzureOpenAiChatOptions openAIChatOptions = AzureOpenAiChatOptions.builder()
                                                                         .deploymentName(gptChatDeploymentModelId)
                                                                         .temperature(0.3)
                                                                         .build();

        return AzureOpenAiChatModel.builder()
                                   .openAIClientBuilder(azureOpenAIClientBuilder)
                                   .defaultOptions(openAIChatOptions)
                                   .build();

    }

}
