// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.assistant.config;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.microsoft.openai.samples.assistant.agent.cache.InMemoryToolsExecutionCache;
import com.microsoft.openai.samples.assistant.agent.cache.ToolsExecutionCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolExecutionCacheConfiguration {

    @Bean

    public ToolsExecutionCache inMemorytoolExecutionCache() {
     return new InMemoryToolsExecutionCache();
    }
}
