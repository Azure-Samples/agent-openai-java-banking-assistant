package dev.langchain4j.openapi.mcp;

import com.microsoft.openai.samples.assistant.langchain4j.agent.builder.TransactionHistoryMCPAgentBuilder;
import com.microsoft.openai.samples.assistant.langchain4j.agent.builder.TransactionHistoryMCPAgentBuilder.TransactionHistoryAgent;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;

public class TransactionHistoryMCPAgentIntegrationTest {

    public static void main(String[] args) throws Exception {

        //Azure Open AI Chat Model
        var azureOpenAiChatModel = AzureOpenAiChatModel.builder()
               
                .apiKey(System.getenv("AZURE_OPENAI_KEY"))
                .endpoint(System.getenv("AZURE_OPENAI_ENDPOINT"))
                .deploymentName("gpt-4.1")
                .logRequestsAndResponses(true)
                .build();

        TransactionHistoryAgent transactionHistoryAgent = (TransactionHistoryAgent) new TransactionHistoryMCPAgentBuilder(
                azureOpenAiChatModel,
                "bob.user@contoso.com",
                "http://localhost:8090/sse",
                "http://localhost:8070/sse").buildProgrammatic();

        String conversationId = "test-conversation-1";

        String response = transactionHistoryAgent.chat(conversationId, "When was last time I've paid contoso?");
        System.out.println(response);

    }
}
