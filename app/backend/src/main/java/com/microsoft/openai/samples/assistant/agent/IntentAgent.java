// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.assistant.agent;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.orchestration.*;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import org.json.JSONObject;

public class IntentAgent {

    private OpenAIAsyncClient client;

    private Kernel kernel;

    private ChatCompletionService chat;

    private String INTENT_SYSTEM_MESSAGE = """
     You are a personal financial advisor who help the user with their recurrent bill payments.
             The user may want to pay the bill uploading a photo of the bill, or it may start the payment checking payments history for a specific payee.
             In other cases it may want to just review the payments history.
             Based on the conversation you need to identify the user intent and ask the user for the missing information.
             The available intents are:
             "BillPayment", "RepeatTransaction","TransactionHistory"
             If none of the intents are identified provide the user with the list of the available intents.
             
             If an intent is identified return the output as json format as below
             {
               "intent": "BillPayment"
             }
             
             If none of the intents are identified ask the user for more clarity and list of the available intents. Use always a json format as output 
             {
               "intent": "None"
               "clarify_sentence": ""
             }
             
             Don't add any comments in the output or other characters, just the json format.
    """;

    public IntentAgent(OpenAIAsyncClient client, String modelId){
        this.client = client;
        this.chat = OpenAIChatCompletion.builder()
                .withModelId(modelId)
                .withOpenAIAsyncClient(client)
                .build();

        kernel = Kernel.builder()
                .withAIService(ChatCompletionService.class, chat)
                .build();
    }
    public IntentAgent(String azureClientKey, String clientEndpoint, String modelId){
        this.client = new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(azureClientKey))
                .endpoint(clientEndpoint)
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .buildAsyncClient();

        this.chat = OpenAIChatCompletion.builder()
                .withModelId(modelId)
                .withOpenAIAsyncClient(client)
                .build();

        kernel = Kernel.builder()
                .withAIService(ChatCompletionService.class, chat)
                .build();
    }

    public IntentResponse run(ChatHistory userChatHistory){
        var agentChatHistory = new ChatHistory();
        agentChatHistory.addAll(userChatHistory);
        agentChatHistory.addSystemMessage(INTENT_SYSTEM_MESSAGE);


        var messages = chat.getChatMessageContentsAsync(
                        agentChatHistory,
                        kernel,
                        InvocationContext.builder().withPromptExecutionSettings(
                                PromptExecutionSettings.builder()
                                        .withTemperature(0.0)
                                        .withTopP(1)
                                        .withMaxTokens(200)
                                        .build())
                                .build())
                .block();

        var message = messages.get(0);

        JSONObject json = new JSONObject(message.getContent());
        IntentType intentType = IntentType.valueOf(json.get("intent").toString());

        return new IntentResponse(intentType,json);
    }

    public static void main(String[] args) throws NoSuchMethodException {

        String AZURE_CLIENT_KEY = System.getenv("AZURE_CLIENT_KEY");
        String CLIENT_ENDPOINT = System.getenv("CLIENT_ENDPOINT");
        String MODEL_ID = System.getenv()
                .getOrDefault("MODEL_ID", "gpt-3.5-turbo-1106");

        IntentAgent agent = new IntentAgent(AZURE_CLIENT_KEY, CLIENT_ENDPOINT, MODEL_ID);
        var chatHistory = new ChatHistory();
        // agent.run("when did I pay my last electricity bill?");
        //chatHistory.addUserMessage("when did I pay my last electricity bill?");
        chatHistory.addUserMessage("I want to pay my electricity bill");
        agent.run( chatHistory);

        }
    }


