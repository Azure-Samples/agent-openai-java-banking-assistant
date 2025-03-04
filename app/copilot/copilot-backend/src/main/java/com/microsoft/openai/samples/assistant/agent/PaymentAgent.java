// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.assistant.agent;

import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.ai.openai.OpenAIAsyncClient;
import com.microsoft.openai.samples.assistant.agent.cache.ToolExecutionCacheKey;
import com.microsoft.openai.samples.assistant.agent.cache.ToolExecutionCacheUtils;
import com.microsoft.openai.samples.assistant.agent.cache.ToolsExecutionCache;
import com.microsoft.openai.samples.assistant.invoice.DocumentIntelligenceInvoiceScanHelper;
import com.microsoft.openai.samples.assistant.plugin.InvoiceScanPlugin;
import com.microsoft.openai.samples.assistant.plugin.LoggedUserPlugin;
import com.microsoft.openai.samples.assistant.proxy.BlobStorageProxy;
import com.microsoft.openai.samples.assistant.security.LoggedUserService;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.hooks.KernelHook;
import com.microsoft.semantickernel.implementation.EmbeddedResourceLoader;
import com.microsoft.semantickernel.orchestration.*;
import com.microsoft.semantickernel.plugin.KernelPlugin;
import com.microsoft.semantickernel.plugin.KernelPluginFactory;
import com.microsoft.semantickernel.samples.openapi.SemanticKernelOpenAPIImporter;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class PaymentAgent {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentAgent.class);
    private OpenAIAsyncClient client;

    private Kernel kernel;

    private ChatCompletionService chat;

    private LoggedUserService loggedUserService;

    private ToolsExecutionCache<Object> toolsExecutionCache;

    private String PAYMENT_AGENT_SYSTEM_MESSAGE = """
     you are a personal financial advisor who help the user with their recurrent bill payments. The user may want to pay the bill uploading a photo of the bill, or it may start the payment checking transactions history for a specific payee.
     For the bill payment you need to know the: bill id or invoice number, payee name, the total amount.
     If you don't have enough information to pay the bill ask the user to provide the missing information.
     If the user submit a photo, always ask the user to confirm the extracted data from the photo.
     Always check if the bill has been paid already based on payment history before asking to execute the bill payment.
     Ask for the payment method to use based on the available methods on the user account.
     if the user wants to pay using bank transfer, check if the payee is in account registered beneficiaries list. If not ask the user to provide the payee bank code.
     Check if the payment method selected by the user has enough funds to pay the bill. Don't use the account balance to evaluate the funds. 
     Before submitting the payment to the system ask the user confirmation providing the payment details.
     Include in the payment description the invoice id or bill id as following: payment for invoice 1527248. 
     When submitting payment always use the available functions to retrieve accountId, paymentMethodId.
     If the payment succeeds provide the user with the payment confirmation. If not provide the user with the error message.
     Use HTML list or table to display bill extracted data, payments, account or transaction details.
     Always use the below logged user details to retrieve account info:
     %s
     Current timestamp: %s
     Don't try to guess accountId,paymentMethodId from the conversation.When submitting payment always use functions to retrieve accountId, paymentMethodId.
     
     Before executing a function call, check data in below function calls cache:
     %s
    """;

    public PaymentAgent(OpenAIAsyncClient client, LoggedUserService loggedUserService,ToolsExecutionCache<Object> toolsExecutionCache, String modelId, DocumentIntelligenceClient documentIntelligenceClient, BlobStorageProxy blobStorageProxy, String transactionAPIUrl, String accountAPIUrl, String paymentsAPIUrl) {
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

        //var paymentPlugin = KernelPluginFactory.createFromObject(new PaymentMockPlugin(), "PaymentMockPlugin");
        //var historyPlugin = KernelPluginFactory.createFromObject(new TransactionHistoryMockPlugin(), "TransactionHistoryMockPlugin");
        var invoiceScanPlugin = KernelPluginFactory.createFromObject(new InvoiceScanPlugin(new DocumentIntelligenceInvoiceScanHelper(documentIntelligenceClient,blobStorageProxy)), "InvoiceScanPlugin");

        String transactionsAPIYaml = null;
        try {
            transactionsAPIYaml = EmbeddedResourceLoader.readFile("transaction-history.yaml",
                    TransactionsReportingAgent.class,
                    EmbeddedResourceLoader.ResourceLocation.CLASSPATH_ROOT);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Cannot find transaction-history.yaml file in the classpath",e);
        }

        //Used to retrieve transactions.
        KernelPlugin openAPIImporterTransactionPlugin = SemanticKernelOpenAPIImporter
                .builder()
                .withPluginName("TransactionHistoryPlugin")
                .withSchema(transactionsAPIYaml)
                .withServer(transactionAPIUrl)
                .build();


        String accountsAPIYaml = null;
        try {
            accountsAPIYaml = EmbeddedResourceLoader.readFile("account.yaml",
                    TransactionsReportingAgent.class,
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

        String paymentsAPIYaml = null;
        try {
            paymentsAPIYaml = EmbeddedResourceLoader.readFile("payments.yaml",
                    TransactionsReportingAgent.class,
                    EmbeddedResourceLoader.ResourceLocation.CLASSPATH_ROOT);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Cannot find account-history.yaml file in the classpath",e);
        }
        //Used to submit payments
        KernelPlugin openAPIImporterPaymentsPlugin = SemanticKernelOpenAPIImporter
                .builder()
                .withPluginName("PaymentsPlugin")
                .withSchema(paymentsAPIYaml)
                .withServer(paymentsAPIUrl)
                .build();

        kernel = Kernel.builder()
                .withAIService(ChatCompletionService.class, chat)
                .withPlugin(invoiceScanPlugin)
                .withPlugin(openAPIImporterTransactionPlugin)
                .withPlugin(openAPIImporterPaymentsPlugin)
                .withPlugin(openAPIImporterAccountPlugin)

                .build();

       KernelHook.FunctionInvokedHook postExecutionHandler = event -> {
           //avoid caching scan invoice and submitPayment calls
           if(event.getFunction().getName().equalsIgnoreCase("scanInvoice") ||
              event.getFunction().getName().equalsIgnoreCase("submitPayment")
           )
               return event;

           LOGGER.debug("Adding {} result to the cache:{}", event.getFunction().getName(),event.getResult().getResult());
           var toolsExecutionKey = new ToolExecutionCacheKey(loggedUserService.getLoggedUser().username(),null,event.getFunction().getName(), ToolExecutionCacheUtils.convert(event.getArguments()));
           this.toolsExecutionCache.put(toolsExecutionKey , event.getResult().getResult());
           return event;
       };

        kernel.getGlobalKernelHooks().addHook(postExecutionHandler);
    }


     public void run (ChatHistory userChatHistory, AgentContext agentContext){
         LOGGER.info("======== Payment Agent: Starting ========");

         // Extend system prompt with logged user details and current timestamp
         var userContext = new LoggedUserPlugin(loggedUserService).getUserContext();

         var datetimeIso8601 = ZonedDateTime.now(ZoneId.of("UTC")).toInstant().toString();


         /**
          * Add the function calls cache to the system prompt. This is a global in memory implementation used only for demo purposes.
          * In production scenario this should be stored globally in an external cache (e.g. Redis) or as scoped conversation context in a database.
          */
         var toolsExecutionCacheContent = this.toolsExecutionCache.entries();


         String extendedSystemMessage = PAYMENT_AGENT_SYSTEM_MESSAGE.formatted(userContext,datetimeIso8601,ToolExecutionCacheUtils.printWithToolNameAndValues(toolsExecutionCacheContent));
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
                                             .withPresencePenalty(-0)
                                             .withFrequencyPenalty(-0)
                                             .build())
                             .build())
                     .block();

         //get last message
         var message = messages.get(messages.size()-1);

         LOGGER.info("======== Payment Agent Response: {}",message.getContent());
         agentContext.setResult(message.getContent());

            }


         }




