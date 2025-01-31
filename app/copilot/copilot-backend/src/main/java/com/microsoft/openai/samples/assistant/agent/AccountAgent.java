// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.assistant.agent;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.microsoft.openai.samples.assistant.agent.cache.ToolExecutionCacheKey;
import com.microsoft.openai.samples.assistant.agent.cache.ToolExecutionCacheUtils;
import com.microsoft.openai.samples.assistant.agent.cache.ToolsExecutionCache;
import com.microsoft.openai.samples.assistant.plugin.LoggedUserPlugin;
import com.microsoft.openai.samples.assistant.security.LoggedUserService;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.implementation.EmbeddedResourceLoader;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.InvocationReturnMode;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.orchestration.ToolCallBehavior;
import com.microsoft.semantickernel.plugin.KernelPlugin;
import com.microsoft.semantickernel.hooks.KernelHook.FunctionInvokedHook;
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

    private ToolsExecutionCache<Object> toolsExecutionCache;

    private String ACCOUNT_AGENT_SYSTEM_MESSAGE = """
     you are a personal financial advisor who help the user to retrieve information about their bank accounts.
     Use html list or table to display the account information.
     Always use the below logged user details to retrieve account info:
     %s
     
     Before executing a function call, check data in below function calls cache:
     %s
    """;

    public AccountAgent(OpenAIAsyncClient client, LoggedUserService loggedUserService,ToolsExecutionCache toolsExecutionCache, String modelId,String accountAPIUrl) {
        this.client = client;
        this.loggedUserService = loggedUserService;
        this.toolsExecutionCache = toolsExecutionCache;
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
                    TransactionsReportingAgent.class,
                    EmbeddedResourceLoader.ResourceLocation.CLASSPATH_ROOT);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Cannot find account.yaml file in the classpath", e);
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

        FunctionInvokedHook postExecutionHandler = event -> {
            LOGGER.debug("Adding {} result to the cache:{}", event.getFunction().getName(),event.getResult().getResult());
            var tollsExecutionKey = new ToolExecutionCacheKey(loggedUserService.getLoggedUser().username(),null,event.getFunction().getName(),ToolExecutionCacheUtils.convert(event.getArguments()));
            this.toolsExecutionCache.put(tollsExecutionKey , event.getResult().getResult());
            return event;
        };

        kernel.getGlobalKernelHooks().addHook(postExecutionHandler);

    }
     public void run (ChatHistory userChatHistory, AgentContext agentContext){
         LOGGER.info("======== Account Agent: Starting ========");

         // Extend system prompt with logged user details
         var userContext = new LoggedUserPlugin(loggedUserService).getUserContext();

         /**
          * Add the function calls cache to the system prompt. This is a global in memory implementation used only for demo purposes.
          * In production scenario this should be stored globally in an external cache (e.g. Redis) or as scoped conversation context in a database.
          */
         var toolsExecutionCacheContent = this.toolsExecutionCache.entries();

         String extendedSystemMessage = ACCOUNT_AGENT_SYSTEM_MESSAGE.formatted(userContext,ToolExecutionCacheUtils.printWithToolNameAndValues(toolsExecutionCacheContent));

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

         LOGGER.info("======== Account Agent Response: {}",message.getContent());
         agentContext.setResult(message.getContent());

            }
         }


