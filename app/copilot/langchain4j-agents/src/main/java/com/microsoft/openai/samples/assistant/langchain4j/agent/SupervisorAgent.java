package com.microsoft.openai.samples.assistant.langchain4j.agent;


import com.microsoft.langchain4j.agent.Agent;
import com.microsoft.langchain4j.agent.AgentExecutionException;
import com.microsoft.langchain4j.agent.AgentMetadata;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SupervisorAgent {

    private final Logger LOGGER = LoggerFactory.getLogger(SupervisorAgent.class);
    private final ChatLanguageModel chatLanguageModel;
    private final List<Agent> agents;
    private final Map<String, AgentMetadata> agentsMetadata;
    private final Prompt agentPrompt;
    //When false only detect the next agent but doesn't route to it. It will answer with the agent name.
    private Boolean routing = true;

   private final String SUPERVISOR_AGENT_SINGLETURN_SYSTEM_MESSAGE = """
        You are a banking customer support agent triaging conversation and select the best agent name that can solve the customer need.
        Use the below list of agents metadata to select the best one for the customer request:
        {{agentsMetadata}}
        Answer only with the agent name.
        if you are not able to select an agent answer with none.
        """;

    public SupervisorAgent(ChatLanguageModel chatLanguageModel, List<Agent> agents, Boolean routing) {
        this.chatLanguageModel = chatLanguageModel;
        this.agents = agents;
        this.routing = routing;

        this.agentsMetadata = agents.stream()
                .collect(Collectors.toMap(Agent::getName, Agent::getMetadata));

        PromptTemplate promptTemplate = PromptTemplate.from(SUPERVISOR_AGENT_SINGLETURN_SYSTEM_MESSAGE);
        agentPrompt =promptTemplate.apply(Map.of("agentsMetadata", this.agentsMetadata));

    }
    public SupervisorAgent(ChatLanguageModel chatLanguageModel, List<Agent> agents) {
       this(chatLanguageModel, agents, true);
    }


    public void invoke(List<ChatMessage> chatHistory) {
        LOGGER.info("------------- SupervisorAgent -------------");

        var internalChatMemory = buildInternalChat(chatHistory);

        ChatRequest request = ChatRequest.builder()
                .messages(internalChatMemory.messages())
                .build();

        AiMessage aiMessage = chatLanguageModel.chat(request).aiMessage();
        String nextAgent = aiMessage.text();
        LOGGER.info("Supervisor Agent handoff to [{}]", nextAgent);

     if (routing) {
            singleTurnRouting(nextAgent, chatHistory);
        }
    }


    protected void singleTurnRouting(String nextAgent, List<ChatMessage> chatHistory) {
        Agent agent = agents.stream()
                .filter(a -> a.getName().equals(nextAgent))
                .findFirst()
                .orElseThrow(() -> new AgentExecutionException("Agent not found: " + nextAgent));

        agent.invoke(chatHistory);
    }



    private ChatMemory buildInternalChat(List<ChatMessage> chatHistory) {
        //build a new chat memory to preserve order of messages otherwise the model hallucinate.
        var internalChatMemory = MessageWindowChatMemory.builder()
                .id("default")
                .maxMessages(20)
                .build();

        internalChatMemory.add(dev.langchain4j.data.message.SystemMessage.from(agentPrompt.text()));
        // filter out tool requests and tool execution results
        chatHistory.stream()
                .filter(chatMessage -> {
                    if (chatMessage instanceof ToolExecutionResultMessage) {
                        return false;
                    }
                    if (chatMessage instanceof AiMessage) {
                        return !((AiMessage) chatMessage).hasToolExecutionRequests();
                    }
                    return true;
                })
                .forEach(internalChatMemory::add);
        return internalChatMemory;
    }
}
