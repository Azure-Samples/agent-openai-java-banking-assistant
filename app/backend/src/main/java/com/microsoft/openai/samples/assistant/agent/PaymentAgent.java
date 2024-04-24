// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.assistant.agent;

import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.microsoft.openai.samples.assistant.controller.ChatController;
import com.microsoft.openai.samples.assistant.invoice.DocumentIntelligenceInvoiceScanHelper;
import com.microsoft.openai.samples.assistant.plugin.InvoiceScanPlugin;
import com.microsoft.openai.samples.assistant.plugin.PaymentPlugin;
import com.microsoft.openai.samples.assistant.plugin.TransactionHistoryPlugin;
import com.microsoft.openai.samples.assistant.proxy.BlobStorageProxy;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatMessageContent;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIFunctionToolCall;
import com.microsoft.semantickernel.contextvariables.CaseInsensitiveMap;
import com.microsoft.semantickernel.contextvariables.ContextVariable;
import com.microsoft.semantickernel.orchestration.FunctionResult;
import com.microsoft.semantickernel.orchestration.FunctionResultMetadata;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.ToolCallBehavior;
import com.microsoft.semantickernel.plugin.KernelPluginFactory;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class PaymentAgent {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentAgent.class);
    private OpenAIAsyncClient client;

    private Kernel kernel;

    private ChatCompletionService chat;

    private String PAYMENT_AGENT_SYSTEM_MESSAGE = """
     you are a personal financial advisor who help the user with their recurrent bill payments. The user may want to pay the bill uploading a photo of the bill, or it may start the payment checking transactions history for a specific payee.
     For the bill payment you need to know the: bill id or invoice number, payee name, the total amount and the bill expiration date.
     if you don't have enough information to pay the bill ask the user to provide the missing information.
     you have the below functions available:
        - paymentHistory: returns the list of the last payments based on the payee name
        - payBill: it pays the bill based on the bill id or invoice number, payee name, total amount
        - invoiceScan: it scans the invoice or bill photo to extract data
     
     Always check if the bill has been paid already based on payment history before asking to execute the bill payment.
     Always ask for the payment method to use: direct debit, credit card, or bank transfer 
     Before executing the payBill function provide the user with the bill details and ask for confirmation.
     If the payment succeeds provide the user with the payment confirmation. If not provide the user with the error message.
      
    """;

    public PaymentAgent(OpenAIAsyncClient client, String modelId, DocumentIntelligenceClient documentIntelligenceClient, BlobStorageProxy blobStorageProxy) {
        this.client = client;
        this.chat = OpenAIChatCompletion.builder()
                .withModelId(modelId)
                .withOpenAIAsyncClient(client)
                .build();

        kernel = Kernel.builder()
                .withAIService(ChatCompletionService.class, chat)
                .build();

        var paymentPlugin = KernelPluginFactory.createFromObject(new PaymentPlugin(), "PaymentPlugin");
        var historyPlugin = KernelPluginFactory.createFromObject(new TransactionHistoryPlugin(), "TransactionHistoryPlugin");
        var invoiceScanPlugin = KernelPluginFactory.createFromObject(new InvoiceScanPlugin(new DocumentIntelligenceInvoiceScanHelper(documentIntelligenceClient,blobStorageProxy)), "InvoiceScanPlugin");


        kernel = Kernel.builder()
                .withAIService(ChatCompletionService.class, chat)
                .withPlugin(paymentPlugin)
                .withPlugin(historyPlugin)
                .withPlugin(invoiceScanPlugin)
                .build();
    }


     public AgentContext run (ChatHistory userChatHistory) {
         LOGGER.info("======== Payment Agent: Starting ========");

         AgentContext agentContext = new AgentContext();

         var agentChatHistory = new ChatHistory(PAYMENT_AGENT_SYSTEM_MESSAGE);

         userChatHistory.forEach( chatMessageContent -> {
            if(chatMessageContent.getAuthorRole() != AuthorRole.SYSTEM)
             agentChatHistory.addMessage(chatMessageContent);
         });



         while (true) {
             var messages = this.chat.getChatMessageContentsAsync(
                             agentChatHistory,
                             kernel,
                             InvocationContext.builder()
                                     .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true)).build())
                     .block();

             var message = messages.get(0);

             agentContext.setResult(message.getContent());


             List<OpenAIFunctionToolCall> toolCalls = messages.stream()
                     .filter(it -> it instanceof OpenAIChatMessageContent)
                     .map(it -> (OpenAIChatMessageContent<?>) it)
                     .map(OpenAIChatMessageContent::getToolCall)
                     .flatMap(List::stream)
                     .collect(Collectors.toList());

             if (toolCalls.isEmpty()) {
                 break;
             }

             messages.stream()
                     .forEach(it -> agentChatHistory.addMessage(it));

             for (var toolCall : toolCalls) {

                 String content = null;
                 try {
                     // getFunction will throw an exception if the function is not found
                     var fn = kernel.getFunction(toolCall.getPluginName(),
                             toolCall.getFunctionName());
                     FunctionResult<?> fnResult = fn
                             .invokeAsync(kernel, toolCall.getArguments(), null, null).block();
                     content = (String) fnResult.getResult();
                 } catch (IllegalArgumentException e) {
                     content = "Unable to find function. Please try again!";
                 }

                 agentChatHistory.addMessage(
                         AuthorRole.TOOL,
                         content,
                         StandardCharsets.UTF_8,
                         new FunctionResultMetadata(new CaseInsensitiveMap<>() {
                             {
                                 put(FunctionResultMetadata.ID, ContextVariable.of(toolCall.getId()));
                             }
                         }));
             }
            }
             return agentContext;
         }


    }


