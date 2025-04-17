package dev.langchain4j.openapi.mcp;

import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.ai.documentintelligence.DocumentIntelligenceClientBuilder;
import com.azure.identity.AzureCliCredentialBuilder;
import com.microsoft.openai.samples.assistant.invoice.DocumentIntelligenceInvoiceScanHelper;
import com.microsoft.openai.samples.assistant.langchain4j.agent.SupervisorAgent;
import com.microsoft.openai.samples.assistant.langchain4j.agent.mcp.AccountMCPAgent;
import com.microsoft.openai.samples.assistant.langchain4j.agent.mcp.PaymentMCPAgent;
import com.microsoft.openai.samples.assistant.langchain4j.agent.mcp.TransactionHistoryMCPAgent;
import com.microsoft.openai.samples.assistant.proxy.BlobStorageProxy;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;

import java.util.ArrayList;
import java.util.List;

public class SupervisorAgentNoRoutingIntegrationTest {

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

        var accountAgent = new AccountMCPAgent(azureOpenAiChatModel,"bob.user@contoso.com/sse","http://localhost:8070/sse");
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

        var supervisorAgent = new SupervisorAgent(azureOpenAiChatModel, List.of(accountAgent,transactionHistoryAgent,paymentAgent), false);
        var chatHistory = new ArrayList<ChatMessage>();

        chatHistory.add(UserMessage.from("How much money do I have in my account?"));
        supervisorAgent.invoke(chatHistory);

        chatHistory.add(UserMessage.from("you have 1000 on your account"));

        chatHistory.add(UserMessage.from("what about my visa"));
        supervisorAgent.invoke(chatHistory);
        chatHistory.add(UserMessage.from("these are the data for your visa card: id 1717171, expiration date 12/2023, cvv 123 balance 500"));

        chatHistory.add(UserMessage.from("When was last time I've paid contoso?"));
        supervisorAgent.invoke(chatHistory);

        chatHistory.add(UserMessage.from("Can you help me plan an investement?"));
        supervisorAgent.invoke(chatHistory);

        chatHistory.add(UserMessage.from("Ok so can you pay this bill for me?"));
        supervisorAgent.invoke(chatHistory);

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
