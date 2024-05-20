// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.assistant.agent;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.microsoft.openai.samples.assistant.plugin.TransactionHistoryPlugin;
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

public class HistoryReportingAgent {
    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryReportingAgent.class);
    private OpenAIAsyncClient client;

    private Kernel kernel;

    private ChatCompletionService chat;

    private String HISTORY_AGENT_SYSTEM_MESSAGE = """
     you are a personal financial advisor who help the user with their recurrent bill payments. To search about the payments history you need to know the payee name.
    If the user doesn't provide the payee name, search the last 10 transactions order by date.
    If the user want to search last transactions for a specific payee ask to provide the payee name.
    """;

    public HistoryReportingAgent(OpenAIAsyncClient client, String modelId){
        this.client = client;
        this.chat = OpenAIChatCompletion.builder()
                .withModelId(modelId)
                .withOpenAIAsyncClient(client)
                .build();

        var plugin = KernelPluginFactory.createFromObject(new TransactionHistoryPlugin(), "TransactionHistoryPlugin");


        kernel = Kernel.builder()
                .withAIService(ChatCompletionService.class, chat)
                .withPlugin(plugin)
                .build();
    }
    public HistoryReportingAgent(String azureClientKey, String clientEndpoint, String modelId){
        this.client = new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(azureClientKey))
                .endpoint(clientEndpoint)
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .buildAsyncClient();

        this.chat = OpenAIChatCompletion.builder()
                .withModelId(modelId)
                .withOpenAIAsyncClient(client)
                .build();

        var plugin = KernelPluginFactory.createFromObject(new TransactionHistoryPlugin(), "TransactionHistoryPlugin");


        kernel = Kernel.builder()
                .withAIService(ChatCompletionService.class, chat)
                .withPlugin(plugin)
                .build();
    }

     public AgentContext run (ChatHistory userChatHistory, AgentContext agentContext){
         LOGGER.info("======== HistoryAndTransaction Agent: Starting ========");

         var agentChatHistory = new ChatHistory(HISTORY_AGENT_SYSTEM_MESSAGE);
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



    public static void main(String[] args) throws NoSuchMethodException {

        String AZURE_CLIENT_KEY = System.getenv("AZURE_CLIENT_KEY");
        String CLIENT_ENDPOINT = System.getenv("CLIENT_ENDPOINT");
        String MODEL_ID = System.getenv()
                .getOrDefault("MODEL_ID", "gpt-3.5-turbo-1106");

        HistoryReportingAgent agent = new HistoryReportingAgent(AZURE_CLIENT_KEY, CLIENT_ENDPOINT, MODEL_ID);

        agent.run(new ChatHistory(), new AgentContext());



        }
    }


