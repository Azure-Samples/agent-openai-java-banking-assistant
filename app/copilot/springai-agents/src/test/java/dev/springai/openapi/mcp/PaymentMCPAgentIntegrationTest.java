package dev.springai.openapi.mcp;

import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.ai.documentintelligence.DocumentIntelligenceClientBuilder;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.identity.AzureCliCredentialBuilder;
import com.microsoft.openai.samples.assistant.invoice.DocumentIntelligenceInvoiceScanHelper;
import com.microsoft.openai.samples.assistant.springai.agent.mcp.PaymentMCPAgent;
import com.microsoft.openai.samples.assistant.proxy.BlobStorageProxy;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.azure.openai.AzureOpenAiChatOptions;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;

public class PaymentMCPAgentIntegrationTest {

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

        var documentIntelligenceInvoiceScanHelper = new DocumentIntelligenceInvoiceScanHelper(getDocumentIntelligenceClient(), getBlobStorageProxyClient());

        var paymentAgent = new PaymentMCPAgent(azureOpenAiChatModel,
                documentIntelligenceInvoiceScanHelper,
                "bob.user@contoso.com",
                "http://localhost:8090/sse",
                "http://localhost:8070/sse",
                "http://localhost:8060/sse");

        var chatHistory = new ArrayList<Message>();
        chatHistory.add(UserMessage.builder()
                .text("Please pay the bill: bill id 1234, payee name contoso, total amount 30.")
                .build());


        paymentAgent.invoke(chatHistory);
        System.out.println(chatHistory.get(chatHistory.size() - 1));


        chatHistory.add(UserMessage.builder()
                .text("Use my visa")
                .build());
        paymentAgent.invoke(chatHistory);
        System.out.println(chatHistory.get(chatHistory.size() - 1));


        chatHistory.add(UserMessage.builder()
                .text("yes please proceed with payment")
                .build());
        paymentAgent.invoke(chatHistory);
        System.out.println(chatHistory.get(chatHistory.size() - 1));


    }

    private static BlobStorageProxy getBlobStorageProxyClient() {

        String storageAccountService = "https://%s.blob.core.windows.net".formatted(System.getenv("AZURE_STORAGE_ACCOUNT"));
        String containerName = "content";
        return new BlobStorageProxy(storageAccountService, containerName, new AzureCliCredentialBuilder().build());
    }

    private static DocumentIntelligenceClient getDocumentIntelligenceClient() {
        String endpoint = "https://%s.cognitiveservices.azure.com".formatted(System.getenv("AZURE_STORAGE_ACCOUNT"));

        return new DocumentIntelligenceClientBuilder()
                .credential(new AzureCliCredentialBuilder().build())
                .endpoint(endpoint)
                .buildClient();
    }
}
