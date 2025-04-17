package dev.langchain4j.openapi.mcp;

import com.microsoft.openai.samples.assistant.langchain4j.agent.mcp.AccountMCPAgent;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;

import java.util.ArrayList;

public class AccountMCPAgentIntegrationTest {

    public static void main(String[] args) throws Exception {

        //Azure Open AI Chat Model
        var azureOpenAiChatModel = AzureOpenAiChatModel.builder()
                .apiKey(System.getenv("AZURE_OPENAI_KEY"))
                .endpoint(System.getenv("AZURE_OPENAI_ENDPOINT"))
                .deploymentName(System.getenv("AZURE_OPENAI_DEPLOYMENT_NAME"))
                .temperature(0.3)
                .logRequestsAndResponses(true)
                .build();

        var accountAgent = new AccountMCPAgent(azureOpenAiChatModel,"bob.user@contoso.com","http://localhost:8070/sse");

        var chatHistory = new ArrayList<ChatMessage>();
        chatHistory.add(UserMessage.from("How much money do I have in my account?"));

        accountAgent.invoke(chatHistory);
        System.out.println(chatHistory.get(chatHistory.size()-1));

        chatHistory.add(UserMessage.from("what about my visa"));
        accountAgent.invoke(chatHistory);
        System.out.println(chatHistory.get(chatHistory.size()-1));

    }
}
