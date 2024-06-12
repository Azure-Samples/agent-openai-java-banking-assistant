// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.assistant.proxy;

import com.azure.core.credential.TokenCredential;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;

/**
 * This class is a proxy to the Blob storage API. It is responsible for: - calling the API -
 * handling errors and retry strategy - add monitoring points - add circuit breaker with exponential
 * backoff
 */
@Component
public class BlobStorageProxy {

    private final BlobContainerClient client;

    public BlobStorageProxy(
            @Value("${storage-account.service}") String storageAccountServiceName,
            @Value("${blob.container.name}") String containerName,
            TokenCredential tokenCredential) {

        String endpoint = "https://%s.blob.core.windows.net".formatted(storageAccountServiceName);
        this.client =
                new BlobContainerClientBuilder()
                        .endpoint(endpoint)
                        .credential(tokenCredential)
                        .containerName(containerName)
                        .buildClient();
    }

    public byte[] getFileAsBytes(String fileName) throws IOException {
        var blobClient = client.getBlobClient(fileName);
        int dataSize = (int) blobClient.getProperties().getBlobSize();

        // There is no need to close ByteArrayOutputStream.
        // https://docs.oracle.com/javase/8/docs/api/java/io/ByteArrayOutputStream.html
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(dataSize);
        blobClient.downloadStream(outputStream);

        return outputStream.toByteArray();
    }

    public void storeFile(byte[] bytes, String originalFilename) {
        BlobClient blobClient = client.getBlobClient(originalFilename);
        blobClient.upload(new ByteArrayInputStream(bytes), bytes.length, true);
    }
}
