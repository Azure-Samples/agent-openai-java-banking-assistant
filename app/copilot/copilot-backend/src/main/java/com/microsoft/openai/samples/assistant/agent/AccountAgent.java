// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.assistant.agent;

import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.ai.openai.OpenAIAsyncClient;
import com.microsoft.openai.samples.assistant.invoice.DocumentIntelligenceInvoiceScanHelper;
import com.microsoft.openai.samples.assistant.plugin.InvoiceScanPlugin;
import com.microsoft.openai.samples.assistant.plugin.LoggedUserPlugin;
import com.microsoft.openai.samples.assistant.proxy.BlobStorageProxy;
import com.microsoft.openai.samples.assistant.security.LoggedUserService;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.implementation.EmbeddedResourceLoader;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.InvocationReturnMode;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.orchestration.ToolCallBehavior;
import com.microsoft.semantickernel.plugin.KernelPlugin;
import com.microsoft.semantickernel.plugin.KernelPluginFactory;
import com.microsoft.semantickernel.samples.openapi.SemanticKernelOpenAPIImporter;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;

public class AccountAgent {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountAgent.class);
    private OpenAIAsyncClient client;

    private Kernel kernel;

    private ChatCompletionService chat;

    private LoggedUserService loggedUserService;

    private String ACCOUNT_AGENT_SYSTEM_MESSAGE = """
     you are a personal financial advisor who help the user to retrieve information about their bank accounts.
     Use html list or table to display the account information.
     Always use the below logged user details to retrieve account info:
     %s
      
    """;

    public AccountAgent(OpenAIAsyncClient client, LoggedUserService loggedUserService, String modelId,String accountAPIUrl) {
        this.client = client;
        this.loggedUserService = loggedUserService;
        this.chat = OpenAIChatCompletion.builder()
                .withModelId(modelId)
                .withOpenAIAsyncClient(client)
                .build();

        kernel = Kernel.builder()
                .withAIService(ChatCompletionService.class, chat)
                .build();


        String accountsAPIYaml = null;
        try {
            accountsAPIYaml = EmbeddedResourceLoader.readFile("account.yaml",
                    HistoryReportingAgent.class,
                    EmbeddedResourceLoader.ResourceLocation.CLASSPATH_ROOT);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Cannot find account-history.yaml file in the classpath",e);
        }
        //Used to retrieve account id. Transaction API requires account id to retrieve transactions
        KernelPlugin openAPIImporterAccountPlugin = SemanticKernelOpenAPIImporter
                .builder()
                .withPluginName("AccountsPlugin")
                .withSchema(accountsAPIYaml)
                .withServer(accountAPIUrl)
                .build();



        kernel = Kernel.builder()
                .withAIService(ChatCompletionService.class, chat)
                .withPlugin(openAPIImporterAccountPlugin)

                .build();
    }


     public void run (ChatHistory userChatHistory, AgentContext agentContext){
         LOGGER.info("======== Account Agent: Starting ========");

         // Extend system prompt with logged user details
         String extendedSystemMessage = ACCOUNT_AGENT_SYSTEM_MESSAGE.formatted(new LoggedUserPlugin(loggedUserService).getUserContext());
         var agentChatHistory = new ChatHistory(extendedSystemMessage);

         userChatHistory.forEach( chatMessageContent -> {
            if(chatMessageContent.getAuthorRole() != AuthorRole.SYSTEM)
             agentChatHistory.addMessage(chatMessageContent);

         });

         var messages = this.chat.getChatMessageContentsAsync(
                             agentChatHistory,
                             kernel,
                             InvocationContext.builder().withToolCallBehavior(
                                     ToolCallBehavior.allowAllKernelFunctions(true))
                             .withReturnMode(InvocationReturnMode.NEW_MESSAGES_ONLY)
                             .withPromptExecutionSettings(
                                     PromptExecutionSettings.builder()
                                             .withTemperature(0.1)
                                             .withTopP(1)
                                             .withPresencePenalty(0)
                                             .withFrequencyPenalty(0)
                                             .build())
                             .build())
                     .block();

         //get last message
         var message = messages.get(messages.size()-1);

         agentContext.setResult(message.getContent());

            }
         }


