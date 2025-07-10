package com.microsoft.openai.samples.assistant;

import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.ai.documentintelligence.DocumentIntelligenceClientBuilder;
import com.azure.identity.AzureCliCredentialBuilder;
import com.google.adk.agents.LlmAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.microsoft.openai.samples.assistant.config.agent.*;
import com.microsoft.openai.samples.assistant.config.agent.isolated.PaymentAgentNoDependencies;
import com.microsoft.openai.samples.assistant.invoice.DocumentIntelligenceInvoiceScanHelper;
import com.microsoft.openai.samples.assistant.proxy.BlobStorageProxy;
import dev.langchain4j.model.chat.ChatModel;
import io.reactivex.rxjava3.core.Flowable;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SupervisorAgentIntegrationTest {
    private static String APPNAME = "SupervisorTest";
    private static String USER= "user1";

    public static void main(String[] args) {

        ChatModel langchain4jModel =
                AzureOpenAILangchain4J.buildChatModel();

        LlmAgent accountAgent = buildAccountAgent(langchain4jModel);
        LlmAgent transactionsAgent = buildTransactionsAgent(langchain4jModel,accountAgent);
        LlmAgent paymentAgent = buildPaymentAgent(langchain4jModel,accountAgent,transactionsAgent);

       var supervisorAgent = new SupervisorAgent(langchain4jModel,accountAgent,transactionsAgent,paymentAgent);

        InMemoryRunner runner = new InMemoryRunner(supervisorAgent.getAgent(), APPNAME);

        ConcurrentMap<String, Object> initialState = new ConcurrentHashMap<>();
       // initialState.put("accountId", "1010");
        initialState.put("loggedUserName", "bob.user@contoso.com");
        var datetimeIso8601 = java.time.ZonedDateTime.now(java.time.ZoneId.of("UTC")).toInstant().toString();
        initialState.put("timestamp", datetimeIso8601);

        Session session =
                  runner.sessionService()
                        .createSession(APPNAME, USER,initialState,null)
                        .blockingGet();


        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            while (true) {
                System.out.print("\nYou > ");
                String userInput = scanner.nextLine();

                if ("quit".equalsIgnoreCase(userInput)) {
                    break;
                }

                Content userMsg = Content.fromParts(Part.fromText(userInput));
                Flowable<Event> events = runner.runAsync(USER, session.id(), userMsg);

                //events.blockingForEach(event -> System.out.println("\n"+event.author()+" > " +event.stringifyContent()));
                events.blockingIterable().forEach(event -> System.out.println("\n"+event.author()+(event.finalResponse()?"[Final]" :"") +" > " +event.stringifyContent()));
            }
        }
    }

    private static LlmAgent buildPaymentAgent(ChatModel langchain4jModel,LlmAgent accountAgent,LlmAgent transactionsAgent) {
        String paymentMCPServerURL = System.getenv("PAYMENT_MCP_SERVER");

        var documentIntelligenceInvoiceScanHelper = new DocumentIntelligenceInvoiceScanHelper(getDocumentIntelligenceClient(),getBlobStorageProxyClient());

        var paymentAgent = new PaymentAgent(langchain4jModel,
                paymentMCPServerURL,
                documentIntelligenceInvoiceScanHelper,
                accountAgent,
                transactionsAgent);
        return paymentAgent.getAgent();
    }

    private static LlmAgent buildTransactionsAgent(ChatModel langchain4jModel,LlmAgent accountAgent) {
        String transactionsMCPServerURL = System.getenv("TRANSACTION_MCP_SERVER");
        var transactionsAgent = new TransactionAgent(langchain4jModel,transactionsMCPServerURL,accountAgent);
        return transactionsAgent.getAgent();
    }

    private static LlmAgent buildAccountAgent(ChatModel langchain4jModel) {

        String accountMCPServerURL = System.getenv("ACCOUNT_MCP_SERVER");
        var accountAgent = new AccountAgent(langchain4jModel,accountMCPServerURL);
        return accountAgent.getAgent();

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
