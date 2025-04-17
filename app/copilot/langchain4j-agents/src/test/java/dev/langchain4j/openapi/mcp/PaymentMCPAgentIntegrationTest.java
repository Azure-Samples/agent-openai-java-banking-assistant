package dev.langchain4j.openapi.mcp;

import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.ai.documentintelligence.DocumentIntelligenceClientBuilder;
import com.azure.identity.AzureCliCredentialBuilder;
import com.microsoft.openai.samples.assistant.invoice.DocumentIntelligenceInvoiceScanHelper;
import com.microsoft.openai.samples.assistant.langchain4j.agent.mcp.PaymentMCPAgent;
import com.microsoft.openai.samples.assistant.proxy.BlobStorageProxy;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;

import java.util.ArrayList;

public class PaymentMCPAgentIntegrationTest {

    public static void main(String[] args) throws Exception {

        //Azure Open AI Chat Model
        var azureOpenAiChatModel = AzureOpenAiChatModel.builder()
                .apiKey(System.getenv("AZURE_OPENAI_KEY"))
                .endpoint(System.getenv("AZURE_OPENAI_ENDPOINT"))
                .deploymentName(System.getenv("AZURE_OPENAI_DEPLOYMENT_NAME"))
                .temperature(0.3)
                .logRequestsAndResponses(true)
                .build();

        var documentIntelligenceInvoiceScanHelper = new DocumentIntelligenceInvoiceScanHelper(getDocumentIntelligenceClient(),getBlobStorageProxyClient());

        var paymentAgent = new PaymentMCPAgent(azureOpenAiChatModel,
                                                    documentIntelligenceInvoiceScanHelper,
                                      "bob.user@contoso.com",
                                      "http://localhost:8090/sse",
                                        "http://localhost:8070/sse",
                                        "http://localhost:8060/sse");

        var chatHistory = new ArrayList<ChatMessage>();
        chatHistory.add(UserMessage.from("Please pay the bill: bill id 1234, payee name contoso, total amount 30."));


        paymentAgent.invoke(chatHistory);
        System.out.println(chatHistory.get(chatHistory.size()-1));


        chatHistory.add(UserMessage.from("use my visa"));
        paymentAgent.invoke(chatHistory);
        System.out.println(chatHistory.get(chatHistory.size()-1));


        chatHistory.add(UserMessage.from("yes please proceed with payment"));
        paymentAgent.invoke(chatHistory);
        System.out.println(chatHistory.get(chatHistory.size()-1));



    }

    private static BlobStorageProxy getBlobStorageProxyClient() {

        String storageAccountService = "https://%s.blob.core.windows.net".formatted(System.getenv("AZURE_STORAGE_ACCOUNT"));
        String containerName = "content";
        return new BlobStorageProxy(storageAccountService,containerName,new AzureCliCredentialBuilder().build());
    }

    private static DocumentIntelligenceClient getDocumentIntelligenceClient() {
        String endpoint = "https://%s.cognitiveservices.azure.com".formatted(System.getenv("AZURE_STORAGE_ACCOUNT"));

        return new DocumentIntelligenceClientBuilder()
                .credential(new AzureCliCredentialBuilder().build())
                .endpoint(endpoint)
                .buildClient();
    }
}
