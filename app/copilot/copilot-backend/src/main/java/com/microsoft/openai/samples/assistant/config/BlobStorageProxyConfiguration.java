// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.assistant.config;

import com.azure.core.credential.TokenCredential;
import com.microsoft.openai.samples.assistant.proxy.BlobStorageProxy;
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
