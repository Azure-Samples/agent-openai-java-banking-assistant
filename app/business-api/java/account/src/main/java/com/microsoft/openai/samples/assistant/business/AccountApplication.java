// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.assistant.business;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication
public class AccountApplication {

    private static final Logger LOG = LoggerFactory.getLogger(AccountApplication.class);

    public static void main(String[] args) {
        LOG.info(
                "Application profile from system property is [{}]",
                System.getProperty("spring.profiles.active"));
        new SpringApplication(AccountApplication.class).run(args);
    }
}
