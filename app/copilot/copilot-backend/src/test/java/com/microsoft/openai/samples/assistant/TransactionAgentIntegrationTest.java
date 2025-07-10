package com.microsoft.openai.samples.assistant;

import com.google.adk.agents.LlmAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.microsoft.openai.samples.assistant.config.agent.AccountAgent;
import com.microsoft.openai.samples.assistant.config.agent.TransactionAgent;
import dev.langchain4j.model.chat.ChatModel;
import io.reactivex.rxjava3.core.Flowable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TransactionAgentIntegrationTest {
    private static String APPNAME = "TransactionAgentTest";
    private static String USER= "user1";

    public static void main(String[] args) {

        String transactionMCPServerURL = System.getenv("TRANSACTION_MCP_SERVER");

        ChatModel langchain4jModel =
                AzureOpenAILangchain4J.buildChatModel();

        LlmAgent accountAgent = buildAccountAgent(langchain4jModel);

        var agent = new TransactionAgent(langchain4jModel,transactionMCPServerURL,accountAgent);

        InMemoryRunner runner = new InMemoryRunner(agent.getAgent(), APPNAME);

        ConcurrentMap<String, Object> initialState = new ConcurrentHashMap<>();
        initialState.put("loggedUserName", "bob.user@contoso.com");
        var datetimeIso8601 = java.time.ZonedDateTime.now(java.time.ZoneId.of("UTC")).toInstant().toString();
        initialState.put("timestamp", datetimeIso8601);

        Session session =
                  runner.sessionService()
                        .createSession(APPNAME, USER,initialState,null)
                        .blockingGet();


        Content userMsg = Content.fromParts(Part.fromText("When was last time I've paid contoso?"));
        System.out.print("\nYou > "+ userMsg.text());
        Flowable<Event> events = runner.runAsync(USER, session.id(), userMsg);

        System.out.print("\nAgent > ");
        events.blockingForEach(event -> System.out.println(event.stringifyContent()));

        userMsg = Content.fromParts(Part.fromText("ok, what about my last transactions"));
        System.out.print("\nYou > "+userMsg.text());

        events = runner.runAsync(USER, session.id(), userMsg);

        System.out.print("\nAgent > ");
        events.blockingForEach(event -> System.out.println(event.stringifyContent()));
    }

    private static LlmAgent buildAccountAgent(ChatModel langchain4jModel) {

        String accountMCPServerURL = System.getenv("ACCOUNT_MCP_SERVER");
        var accountAgent = new AccountAgent(langchain4jModel,accountMCPServerURL);
        return accountAgent.getAgent();

    }
}
