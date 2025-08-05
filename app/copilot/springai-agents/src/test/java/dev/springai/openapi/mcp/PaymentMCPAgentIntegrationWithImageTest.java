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

public class PaymentMCPAgentIntegrationWithImageTest {

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
                "http://localhost:8090",
                "http://localhost:8070",
                "http://localhost:8060");

        var chatHistory = new ArrayList<Message>();
        chatHistory.add(UserMessage.builder()
                .text("Please pay this bill gori.png")
                .build());

        //this flow should activate the scanInvoice tool

        paymentAgent.invoke(chatHistory);
        System.out.println(chatHistory.get(chatHistory.size() - 1));

        chatHistory.add(UserMessage.builder()
                .text("Yep, they are correct")
                .build());
        paymentAgent.invoke(chatHistory);
        System.out.println(chatHistory.get(chatHistory.size() - 1));


        chatHistory.add(UserMessage.builder()
                .text("Use my visa")
                .build());
        paymentAgent.invoke(chatHistory);
        System.out.println(chatHistory.get(chatHistory.size() - 1));


        chatHistory.add(UserMessage.builder()
                .text("Yes, please proceed with the payment")
                .build());
        paymentAgent.invoke(chatHistory);
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
