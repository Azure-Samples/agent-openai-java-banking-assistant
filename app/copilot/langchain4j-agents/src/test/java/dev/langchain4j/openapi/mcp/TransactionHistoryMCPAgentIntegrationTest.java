package dev.langchain4j.openapi.mcp;

import com.microsoft.openai.samples.assistant.langchain4j.agent.mcp.TransactionHistoryMCPAgent;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;

import java.util.ArrayList;

public class TransactionHistoryMCPAgentIntegrationTest {

    public static void main(String[] args) throws Exception {

        //Azure Open AI Chat Model
        var azureOpenAiChatModel = AzureOpenAiChatModel.builder()
                .apiKey(System.getenv("AZURE_OPENAI_KEY"))
                .endpoint(System.getenv("AZURE_OPENAI_ENDPOINT"))
                .deploymentName(System.getenv("AZURE_OPENAI_DEPLOYMENT_NAME"))
                .temperature(0.3)
                .logRequestsAndResponses(true)
                .build();

        var transactionHistoryAgent = new TransactionHistoryMCPAgent(azureOpenAiChatModel,
                "bob.user@contoso.com",
                "http://localhost:8090/sse",
                "http://localhost:8070/sse");

        var chatHistory = new ArrayList<ChatMessage>();


        chatHistory.add(UserMessage.from("When was last time I've paid contoso?"));
        transactionHistoryAgent.invoke(chatHistory);
        System.out.println(chatHistory.get(chatHistory.size()-1));


    }
}
