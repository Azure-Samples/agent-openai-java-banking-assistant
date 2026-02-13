package com.microsoft.openai.samples.assistant.langchain4j.agent.builder;

import com.microsoft.openai.samples.assistant.langchain4j.agent.SupervisorAgent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.supervisor.SupervisorResponseStrategy;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Builder for creating Supervisor Agent using langchain4j-agentic module.
 * The supervisor agent routes user requests to the appropriate domain-specific agent.
 * 
 * This implementation uses the supervisorBuilder from AgenticServices which:
 * - Takes a list of sub-agents (account, transaction, payment)
 * - Automatically handles agent selection based on user intent
 * - Uses ResponseStrategy.LAST to return only the final agent's response
 */
public class SupervisorAgentBuilder {

    private static final Logger log = LoggerFactory.getLogger(SupervisorAgentBuilder.class);

    private static final String SUPERVISOR_AGENT_SYSTEM_MESSAGE = loadResource("prompts/supervisor-context-prompt.txt");

    private static String loadResource(String path) {
        try (InputStream is = SupervisorAgentBuilder.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalStateException("Resource not found: " + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load resource: " + path, e);
        }
    }

    private final ChatModel chatModel;
    private final Object accountAgent;
    private final Object transactionAgent;
    private final Object paymentAgent;

    public SupervisorAgentBuilder(
            ChatModel chatModel,
            Object accountAgent,
            Object transactionAgent,
            Object paymentAgent) {
        
        if (chatModel == null) {
            throw new IllegalArgumentException("chatModel cannot be null");
        }
        if (accountAgent == null) {
            throw new IllegalArgumentException("accountAgent cannot be null");
        }
        if (transactionAgent == null) {
            throw new IllegalArgumentException("transactionAgent cannot be null");
        }
        if (paymentAgent == null) {
            throw new IllegalArgumentException("paymentAgent cannot be null");
        }
        log.info("SupervisorAgentBuilder initialized with chatModel: {}, accountAgent: {}, transactionAgent: {}, paymentAgent: {}",
                chatModel, accountAgent, transactionAgent, paymentAgent);
        this.chatModel = chatModel;
        this.accountAgent = accountAgent;
        this.transactionAgent = transactionAgent;
        this.paymentAgent = paymentAgent;
    }

    /**
     * Builds the Supervisor Agent using the declarative approach with @Agent interface.
     * 
     * @return SupervisorAgent instance
     */
    public Object buildDeclarative() {
        // Build supervisor using AgenticServices with declarative interface
        return AgenticServices.supervisorBuilder(SupervisorAgent.class)
                .chatModel(chatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(20)
                .build())
                .subAgents(accountAgent, transactionAgent, paymentAgent)
                .build();
    }

    /**
     * Builds the Supervisor Agent using the programmatic supervisor builder.
     * This is the recommended approach for supervisor agents as it provides
     * specialized configuration options for multi-agent orchestration.
     * 
     * @return Supervisor Agent instance
     */
    public Object buildProgrammatic() {
        // Build supervisor using AgenticServices supervisorBuilder
        // The supervisor will automatically route requests to sub-agents
        return AgenticServices.supervisorBuilder(SupervisorAgent.class)

                .name("BankingSupervisor")
                .chatModel(chatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(20)
                .build())
                .supervisorContext(SUPERVISOR_AGENT_SYSTEM_MESSAGE)
                .subAgents(accountAgent, transactionAgent, paymentAgent)
                .responseStrategy(SupervisorResponseStrategy.LAST)  // Return only the final agent's response
                .build();
    }

    /**
     * Declarative supervisor interface.
     * This approach provides type-safe supervisor definition with annotations.
     * SupervisorAgent interface is now extracted to a public interface class.
     */
}

