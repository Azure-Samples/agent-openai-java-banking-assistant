package dev.springai.openapi.mcp;

import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.ai.documentintelligence.DocumentIntelligenceClientBuilder;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.identity.AzureCliCredentialBuilder;
import com.microsoft.openai.samples.assistant.invoice.DocumentIntelligenceInvoiceScanHelper;
import com.microsoft.openai.samples.assistant.springai.agent.SupervisorAgent;
import com.microsoft.openai.samples.assistant.springai.agent.mcp.AccountMCPAgent;
import com.microsoft.openai.samples.assistant.springai.agent.mcp.PaymentMCPAgent;
import com.microsoft.openai.samples.assistant.springai.agent.mcp.TransactionHistoryMCPAgent;
import com.microsoft.openai.samples.assistant.proxy.BlobStorageProxy;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.azure.openai.AzureOpenAiChatOptions;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.List;

public class SupervisorAgentRoutingIntegrationTest {

    public static void main(String[] args) throws Exception {

        //Azure Open AI Chat Model
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

        var documentIntelligenceInvoiceScanHelper = new DocumentIntelligenceInvoiceScanHelper(getDocumentIntelligenceClient(), getBlobStorageProxyClient());

        var accountAgent = new AccountMCPAgent(azureOpenAiChatModel, "bob.user@contoso.com",
                "http://localhost:8070/sse");
        var transactionHistoryAgent = new TransactionHistoryMCPAgent(azureOpenAiChatModel,
                "bob.user@contoso.com",
                "http://localhost:8090/sse",
                "http://localhost:8070/sse");
        var paymentAgent = new PaymentMCPAgent(azureOpenAiChatModel,
                documentIntelligenceInvoiceScanHelper,
                "bob.user@contoso.com",
                "http://localhost:8090/sse",
                "http://localhost:8070/sse",
                "http://localhost:8060/sse");

        var supervisorAgent = new SupervisorAgent(azureOpenAiChatModel, List.of(accountAgent, transactionHistoryAgent, paymentAgent));
        var chatHistory = new ArrayList<Message>();


        chatHistory.add(UserMessage.builder()
                .text("How much money do I have in my account?")
                .build());
        supervisorAgent.invoke(chatHistory);
        System.out.println(chatHistory.get(chatHistory.size() - 1));

        chatHistory.add(UserMessage.builder()
                .text("What about my Visa")
                .build());
        supervisorAgent.invoke(chatHistory);
        System.out.println(chatHistory.get(chatHistory.size() - 1));

        chatHistory.add(UserMessage.builder()
                .text("When was the last time I paid Contoso")
                .build());
        supervisorAgent.invoke(chatHistory);
        System.out.println(chatHistory.get(chatHistory.size() - 1));

    }

    private static BlobStorageProxy getBlobStorageProxyClient() {

        String containerName = "content";
        return new BlobStorageProxy(System.getenv("AZURE_STORAGE_ACCOUNT"), containerName, new AzureCliCredentialBuilder().build());
    }

    private static DocumentIntelligenceClient getDocumentIntelligenceClient() {
        String endpoint = "https://%s.cognitiveservices.azure.com".formatted(System.getenv("AZURE_DOCUMENT_INTELLIGENCE_SERVICE"));

        return new DocumentIntelligenceClientBuilder()
                .credential(new AzureCliCredentialBuilder().build())
                .endpoint(endpoint)
                .buildClient();
    }
}
