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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping("/api/content")
    public ResponseEntity<String> uploadContent(@RequestParam("file") MultipartFile file) {
        LOGGER.info("Received request to upload a file [{}}", file.getOriginalFilename());

        if (file.isEmpty()) {
            LOGGER.warn("Uploaded file is empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Uploaded file is empty");
        }

        try {
            byte[] bytes = file.getBytes();
            blobStorageProxy.storeFile(bytes, file.getOriginalFilename());
        } catch (IOException ex) {
            LOGGER.error("Cannot store file [{}] to blob.{}", file.getOriginalFilename(), ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while storing file");
        }

        return ResponseEntity.ok(file.getOriginalFilename());
    }

}
