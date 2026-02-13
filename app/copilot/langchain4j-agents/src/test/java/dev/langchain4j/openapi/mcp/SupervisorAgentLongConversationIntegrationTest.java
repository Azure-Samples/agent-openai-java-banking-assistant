package dev.langchain4j.openapi.mcp;

import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.ai.documentintelligence.DocumentIntelligenceClientBuilder;
import com.azure.identity.AzureCliCredentialBuilder;
import com.microsoft.openai.samples.assistant.invoice.DocumentIntelligenceInvoiceScanHelper;
import com.microsoft.openai.samples.assistant.langchain4j.agent.SupervisorAgent;
import com.microsoft.openai.samples.assistant.langchain4j.agent.builder.AccountMCPAgentBuilder;
import com.microsoft.openai.samples.assistant.langchain4j.agent.builder.PaymentMCPAgentBuilder;
import com.microsoft.openai.samples.assistant.langchain4j.agent.builder.SupervisorAgentBuilder;
import com.microsoft.openai.samples.assistant.langchain4j.agent.builder.TransactionHistoryMCPAgentBuilder;
import com.microsoft.openai.samples.assistant.proxy.BlobStorageProxy;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;

public class SupervisorAgentLongConversationIntegrationTest {

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

        var accountAgent = new AccountMCPAgentBuilder(azureOpenAiChatModel,
                "bob.user@contoso.com",
                "http://localhost:8070/sse").buildProgrammatic();
        var transactionHistoryAgent = new TransactionHistoryMCPAgentBuilder(azureOpenAiChatModel,
                "bob.user@contoso.com",
                "http://localhost:8090/sse",
                "http://localhost:8070/sse").buildProgrammatic();
        var paymentAgent = new PaymentMCPAgentBuilder(azureOpenAiChatModel,
                documentIntelligenceInvoiceScanHelper,
                "bob.user@contoso.com",
                "http://localhost:8060/sse").buildProgrammatic();

        SupervisorAgent supervisorAgent = (SupervisorAgent) new SupervisorAgentBuilder(
                azureOpenAiChatModel, accountAgent, transactionHistoryAgent, paymentAgent).buildProgrammatic();

        String conversationId = "test-conversation-1";

        String response1 = supervisorAgent.chat(conversationId, "How much money do I have in my account?");
        System.out.println(response1);

        String response2 = supervisorAgent.chat(conversationId, "what about my visa");
        System.out.println(response2);

        String response3 = supervisorAgent.chat(conversationId, "When was last time I've paid contoso?");
        System.out.println(response3);

        String response4 = supervisorAgent.chat(conversationId, "Please pay this bill gori.png");
        System.out.println(response4);

        String response5 = supervisorAgent.chat(conversationId, "yep, they are correct");
        System.out.println(response5);

        String response6 = supervisorAgent.chat(conversationId, "use my visa");
        System.out.println(response6);

        String response7 = supervisorAgent.chat(conversationId, "yes please proceed with payment");
        System.out.println(response7);
    }
    private static BlobStorageProxy getBlobStorageProxyClient() {

        String containerName = "content";
        return new BlobStorageProxy(System.getenv("AZURE_STORAGE_ACCOUNT"),containerName,new AzureCliCredentialBuilder().build());
    }

    private static DocumentIntelligenceClient getDocumentIntelligenceClient() {
        String endpoint = "https://%s.cognitiveservices.azure.com".formatted(System.getenv("AZURE_DOCUMENT_INTELLIGENCE_SERVICE"));

        return new DocumentIntelligenceClientBuilder()
                .credential(new AzureCliCredentialBuilder().build())
                .endpoint(endpoint)
                .buildClient();
    }
}
