package dev.langchain4j.openapi.mcp;

import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.ai.documentintelligence.DocumentIntelligenceClientBuilder;
import com.azure.identity.AzureCliCredentialBuilder;
import com.microsoft.openai.samples.assistant.invoice.DocumentIntelligenceInvoiceScanHelper;
import com.microsoft.openai.samples.assistant.langchain4j.agent.builder.PaymentMCPAgentBuilder;
import com.microsoft.openai.samples.assistant.langchain4j.agent.builder.PaymentMCPAgentBuilder.PaymentAgent;
import com.microsoft.openai.samples.assistant.proxy.BlobStorageProxy;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;

public class PaymentMCPAgentIntegrationTest {

    public static void main(String[] args) throws Exception {

        //Azure Open AI Chat Model
        var azureOpenAiChatModel = AzureOpenAiChatModel.builder()
      
                .apiKey(System.getenv("AZURE_OPENAI_KEY"))
                .endpoint(System.getenv("AZURE_OPENAI_ENDPOINT"))
                .deploymentName(System.getenv("AZURE_OPENAI_DEPLOYMENT_NAME"))
             .logRequestsAndResponses(true)
                .build();

        var documentIntelligenceInvoiceScanHelper = new DocumentIntelligenceInvoiceScanHelper(getDocumentIntelligenceClient(),getBlobStorageProxyClient());

        PaymentAgent paymentAgent = (PaymentAgent) new PaymentMCPAgentBuilder(
                azureOpenAiChatModel,
                documentIntelligenceInvoiceScanHelper,
                "bob.user@contoso.com",
                "http://localhost:8060/sse").buildProgrammatic();

        String conversationId = "test-conversation-1";

        String response1 = paymentAgent.chat(conversationId, "Please pay the bill: bill id 1234, payee name contoso, total amount 30.");
        System.out.println(response1);

        String response2 = paymentAgent.chat(conversationId, "use my visa");
        System.out.println(response2);

        String response3 = paymentAgent.chat(conversationId, "yes please proceed with payment");
        System.out.println(response3);

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
