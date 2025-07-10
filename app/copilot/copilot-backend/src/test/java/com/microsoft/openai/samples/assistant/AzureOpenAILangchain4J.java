package com.microsoft.openai.samples.assistant;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.identity.AzureCliCredentialBuilder;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.chat.ChatModel;

public class AzureOpenAILangchain4J {

    public static ChatModel buildChatModel(){
        String azureOpenAIName = System.getenv("AZURE_OPENAI_NAME");
        String chatDeploymentName = System.getenv("AZURE_OPENAI_DEPLOYMENT_ID");
        if (azureOpenAIName == null || azureOpenAIName.isEmpty())
            throw new IllegalArgumentException("AZURE_OPENAI_NAME environment variable is not set.");

      
      TokenCredential tokenCredential = new AzureCliCredentialBuilder().build();
        
      String endpoint = "https://%s.openai.azure.com".formatted(azureOpenAIName);

        var httpLogOptions = new HttpLogOptions();
        // httpLogOptions.setPrettyPrintBody(true);
        httpLogOptions.setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS);
 
        OpenAIClient client = new OpenAIClientBuilder()
                .endpoint(endpoint)
                .credential(tokenCredential)
                .httpLogOptions(httpLogOptions)
                .buildClient();

          return AzureOpenAiChatModel.builder()
                .openAIClient(client)
                .logRequestsAndResponses(true)
                .deploymentName(chatDeploymentName)
                .build();
    }
}
