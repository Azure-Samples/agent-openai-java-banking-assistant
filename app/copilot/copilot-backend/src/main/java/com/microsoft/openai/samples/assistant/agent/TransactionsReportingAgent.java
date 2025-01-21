// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.assistant.agent;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.microsoft.openai.samples.assistant.agent.cache.ToolExecutionCacheUtils;
import com.microsoft.openai.samples.assistant.agent.cache.ToolsExecutionCache;
import com.microsoft.openai.samples.assistant.plugin.LoggedUserPlugin;
import com.microsoft.openai.samples.assistant.security.LoggedUserService;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.hooks.KernelHook;
import com.microsoft.semantickernel.implementation.EmbeddedResourceLoader;
import com.microsoft.semantickernel.orchestration.*;
import com.microsoft.semantickernel.plugin.KernelPlugin;
import com.microsoft.semantickernel.samples.openapi.SemanticKernelOpenAPIImporter;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TransactionsReportingAgent {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionsReportingAgent.class);
    private OpenAIAsyncClient client;

    private Kernel kernel;

    private ChatCompletionService chat;

    private LoggedUserService loggedUserService;

    ToolsExecutionCache<Object> toolsExecutionCache;

    private String HISTORY_AGENT_SYSTEM_MESSAGE = """
    you are a personal financial advisor who help the user with their recurrent bill payments. To search about the payments history you need to know the payee name.
    If the user doesn't provide the payee name, search the last 10 transactions order by date.
    If the user want to search last transactions for a specific payee, ask to provide the payee name.
    Use html list or table to display the transaction information.
    Always use the below logged user details to search the transactions:
    %s
    Current timestamp: %s
    
    Before executing a function call, check data in below function calls cache:
     %s
    """;

    public TransactionsReportingAgent(OpenAIAsyncClient client, LoggedUserService loggedUserService, ToolsExecutionCache<Object> toolsExecutionCache, String modelId, String transactionAPIUrl, String accountAPIUrl){
        this.client = client;
        this.loggedUserService = loggedUserService;
        this.toolsExecutionCache = toolsExecutionCache;
        this.chat = OpenAIChatCompletion.builder()
                .withModelId(modelId)
                .withOpenAIAsyncClient(client)
                .build();

        /**
         * Using native function to create a plugin
         */
        //var plugin = KernelPluginFactory.createFromObject(new TransactionHistoryMockPlugin(), "TransactionHistoryMockPlugin");

        String transactionAPIYaml = null;
        try {
            transactionAPIYaml = EmbeddedResourceLoader.readFile("transaction-history.yaml",
                    TransactionsReportingAgent.class,
                    EmbeddedResourceLoader.ResourceLocation.CLASSPATH_ROOT);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Cannot find transaction-history.yaml file in the classpath",e);
        }

        //Used to retrieve transactions.
        KernelPlugin openAPIImporterTransactionPlugin = SemanticKernelOpenAPIImporter
                .builder()
                .withPluginName("TransactionHistoryPlugin")
                .withSchema(transactionAPIYaml)
                .withServer(transactionAPIUrl)
                .build();


        String accountAPIYaml = null;
        try {
            accountAPIYaml = EmbeddedResourceLoader.readFile("account.yaml",
                    TransactionsReportingAgent.class,
                    EmbeddedResourceLoader.ResourceLocation.CLASSPATH_ROOT);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Cannot find account-history.yaml file in the classpath",e);
        }
        //Used to retrieve account id. Transaction API requires account id to retrieve transactions
        KernelPlugin openAPIImporterAccountPlugin = SemanticKernelOpenAPIImporter
                .builder()
                .withPluginName("AccountPlugin")
                .withSchema(accountAPIYaml)
                .withServer(accountAPIUrl)
                .build();



        kernel = Kernel.builder()
                .withAIService(ChatCompletionService.class, chat)
                .withPlugin(openAPIImporterTransactionPlugin)
                .withPlugin(openAPIImporterAccountPlugin)
                .build();

        KernelHook.FunctionInvokedHook postExecutionHandler = event -> {
            LOGGER.debug("Post execution handler for {} function. Result won't be added to cache: {}", event.getFunction().getName(),event.getResult().getResult());
            return event;
        };

        kernel.getGlobalKernelHooks().addHook(postExecutionHandler);

    }

     public void run (ChatHistory userChatHistory, AgentContext agentContext){
         LOGGER.info("======== TransactionsHistory Agent: Starting ========");

         // Extend system prompt with logged user details and current timestamp
         var datetimeIso8601 = ZonedDateTime.now(ZoneId.of("UTC")).toInstant().toString();

         /**
          * Add the function calls cache to the system prompt. This is a global in memory implementation used only for demo purposes.
          * In production scenario this should be stored globally in an external cache (e.g. Redis) or as scoped conversation context in a database.
          * Search results won't be cached. Only account details.
          */

         var toolsExecutionCacheContent = this.toolsExecutionCache.entries();
         String extendedSystemMessage = HISTORY_AGENT_SYSTEM_MESSAGE.formatted(new LoggedUserPlugin(loggedUserService).getUserContext(),datetimeIso8601, ToolExecutionCacheUtils.printWithToolNameAndValues(toolsExecutionCacheContent));

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
                                /* .withPromptExecutionSettings(
                                         PromptExecutionSettings.builder()
                                                 .withTemperature(0.1)
                                                 .withTopP(1)
                                                 //.withPresencePenalty(0)
                                                 //.withFrequencyPenalty(0)
                                                 .build())*/
                                 .build())
                 .block();

         //get last message
         var message = messages.get(messages.size()-1);
         LOGGER.info("======== TransactionsHistory Agent Response: {}",message.getContent());
         agentContext.setResult(message.getContent());
         }


    }


