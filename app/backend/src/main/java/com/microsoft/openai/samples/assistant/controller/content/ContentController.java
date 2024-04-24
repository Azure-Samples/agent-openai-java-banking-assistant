// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.assistant.controller.content;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import com.microsoft.openai.samples.assistant.proxy.BlobStorageProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ContentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContentController.class);
    private final BlobStorageProxy blobStorageProxy;

    ContentController(BlobStorageProxy blobStorageProxy) {
        this.blobStorageProxy = blobStorageProxy;
    }

    @GetMapping("/api/content/{fileName}")
    public ResponseEntity<InputStreamResource> getContent(@PathVariable String fileName) {
        LOGGER.info("Received request for  content with name [{}] ]", fileName);

        if (!StringUtils.hasText(fileName)) {
            LOGGER.warn("file name cannot be null");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        String mimeType = URLConnection.guessContentTypeFromName(fileName);

        MediaType contentType = new MediaType(MimeTypeUtils.parseMimeType(mimeType));

        InputStream fileInputStream;

        try {
            fileInputStream = new ByteArrayInputStream(blobStorageProxy.getFileAsBytes(fileName));
        } catch (IOException ex) {
            LOGGER.error("Cannot retrieve file [{}] from blob.{}", fileName, ex.getMessage());
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=%s".formatted(fileName))
                .contentType(contentType)
                .body(new InputStreamResource(fileInputStream));
    }
}
