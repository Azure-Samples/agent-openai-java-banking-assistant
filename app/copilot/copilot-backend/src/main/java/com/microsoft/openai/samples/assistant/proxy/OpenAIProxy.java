// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.assistant.proxy;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.models.*;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.IterableStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * This class is a proxy to the OpenAI API to simplify cross-cutting concerns management (security,
 * load balancing, monitoring, resiliency). It is responsible for: - calling the OpenAI API -
 * handling errors and retry strategy - load balance requests across open AI instances - add
 * monitoring points - add circuit breaker with exponential backoff
 *
 * <p>It also makes unit testing easy using mockito to provide mock implementation for this bean.
 */
@Component
public class OpenAIProxy {

    private final OpenAIClient client;

    @Value("${openai.chatgpt.deployment}")
    private String gptChatDeploymentModelId;

    @Value("${openai.embedding.deployment}")
    private String embeddingDeploymentModelId;

    public OpenAIProxy(OpenAIClient client) {
        this.client = client;
    }

    public Completions getCompletions(CompletionsOptions completionsOptions) {
        Completions completions;
        try {
            completions = client.getCompletions(this.gptChatDeploymentModelId, completionsOptions);
        } catch (HttpResponseException e) {
            throw new ResponseStatusException(
                    e.getResponse().getStatusCode(), "Error calling OpenAI API:" + e.getValue(), e);
        }
        return completions;
    }

    public Completions getCompletions(String prompt) {

        Completions completions;
        try {
            completions = client.getCompletions(this.gptChatDeploymentModelId, prompt);
        } catch (HttpResponseException e) {
            throw new ResponseStatusException(
                    e.getResponse().getStatusCode(),
                    "Error calling OpenAI API:" + e.getMessage(),
                    e);
        }
        return completions;
    }

    public ChatCompletions getChatCompletions(ChatCompletionsOptions chatCompletionsOptions) {
        ChatCompletions chatCompletions;
        try {
            chatCompletions =
                    client.getChatCompletions(
                            this.gptChatDeploymentModelId, chatCompletionsOptions);
        } catch (HttpResponseException e) {
            throw new ResponseStatusException(
                    e.getResponse().getStatusCode(),
                    "Error calling OpenAI API:" + e.getMessage(),
                    e);
        }
        return chatCompletions;
    }

    public IterableStream<ChatCompletions> getChatCompletionsStream(
            ChatCompletionsOptions chatCompletionsOptions) {
        try {
            return client.getChatCompletionsStream(
                    this.gptChatDeploymentModelId, chatCompletionsOptions);
        } catch (HttpResponseException e) {
            throw new ResponseStatusException(
                    e.getResponse().getStatusCode(),
                    "Error calling OpenAI API:" + e.getMessage(),
                    e);
        }
    }

    public Embeddings getEmbeddings(List<String> texts) {
        Embeddings embeddings;
        try {
            EmbeddingsOptions embeddingsOptions = new EmbeddingsOptions(texts);
            embeddingsOptions.setUser("search-openai-demo-java");
            embeddingsOptions.setModel(this.embeddingDeploymentModelId);
            embeddingsOptions.setInputType("query");
            embeddings = client.getEmbeddings(this.embeddingDeploymentModelId, embeddingsOptions);
        } catch (HttpResponseException e) {
            throw new ResponseStatusException(
                    e.getResponse().getStatusCode(),
                    "Error calling OpenAI API:" + e.getMessage(),
                    e);
        }
        return embeddings;
    }
}
