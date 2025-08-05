package dev.springai.openapi.mcp;

import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.microsoft.openai.samples.assistant.springai.agent.mcp.AccountMCPAgent;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.azure.openai.AzureOpenAiChatOptions;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;

public class AccountMCPAgentIntegrationTest {

    public static void main(String[] args) throws Exception {

        var openAIClientBuilder = new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(System.getenv("AZURE_OPENAI_KEY")))
                .endpoint(System.getenv("AZURE_OPENAI_ENDPOINT"));

        var openAIChatOptions = AzureOpenAiChatOptions.builder()
                .deploymentName(System.getenv("AZURE_OPENAI_DEPLOYMENT_NAME"))
                .temperature(0.3)
                .build();

        //Azure Open AI Chat Model
        var azureOpenAiChatModel = AzureOpenAiChatModel.builder()
                .openAIClientBuilder(openAIClientBuilder)
                .defaultOptions(openAIChatOptions)
                .build();

        var accountAgent = new AccountMCPAgent(azureOpenAiChatModel, "bob.user@contoso.com", "http://localhost:8070/sse");

        var chatHistory = new ArrayList<Message>();
        chatHistory.add(UserMessage.builder()
                .text("How much money do I have in my account?")
                .build());

        accountAgent.invoke(chatHistory);
        System.out.println(chatHistory.get(chatHistory.size() - 1));

        chatHistory.add(UserMessage.builder()
                .text("What about my visa?")
                .build());
        accountAgent.invoke(chatHistory);
        System.out.println(chatHistory.get(chatHistory.size() - 1));

    }
}
