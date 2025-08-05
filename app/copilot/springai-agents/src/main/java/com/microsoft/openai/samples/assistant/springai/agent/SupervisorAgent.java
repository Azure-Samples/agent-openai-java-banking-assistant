package com.microsoft.openai.samples.assistant.springai.agent;


import com.microsoft.springai.agent.Agent;
import com.microsoft.springai.agent.AgentExecutionException;
import com.microsoft.springai.agent.AgentMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SupervisorAgent {

    private final Logger LOGGER = LoggerFactory.getLogger(SupervisorAgent.class);
    private final ChatModel chatModel;
    private final List<Agent> agents;
    private final Map<String, AgentMetadata> agentsMetadata;
    private final Prompt agentPrompt;
    //When false only detect the next agent but doesn't route to it. It will answer with the agent name.
    private Boolean routing = true;

    private final String SUPERVISOR_AGENT_SINGLETURN_SYSTEM_MESSAGE = """
            You are a banking customer support agent triaging conversation and select the best agent name that can solve the customer need.
            Use the below list of agents metadata to select the best one for the customer request:
            {agentsMetadata}
            Answer only with the agent name.
            if you are not able to select an agent answer with none.
            """;

    public SupervisorAgent(ChatModel chatModel, List<Agent> agents, Boolean routing) {
        this.chatModel = chatModel;
        this.agents = agents;
        this.routing = routing;

        this.agentsMetadata = agents.stream()
                .collect(Collectors.toMap(Agent::getName, Agent::getMetadata));

        PromptTemplate promptTemplate = new PromptTemplate(SUPERVISOR_AGENT_SINGLETURN_SYSTEM_MESSAGE);
        agentPrompt = promptTemplate.create(Map.of("agentsMetadata", this.agentsMetadata));

    }

    public SupervisorAgent(ChatModel chatModel, List<Agent> agents) {
        this(chatModel, agents, true);
    }


    public List<Message> invoke(List<Message> chatHistory) {
        LOGGER.info("------------- SupervisorAgent -------------");

        var internalChatMemory = buildInternalChat(chatHistory);

        AssistantMessage aiMessage = chatModel.call(Prompt.builder()
                        .messages(internalChatMemory.get("default"))
                        .build())
                .getResult()
                .getOutput();
        String nextAgent = aiMessage.getText();
        LOGGER.info("Supervisor Agent handoff to [{}]", nextAgent);

        if (routing) {
            return singleTurnRouting(nextAgent, chatHistory);
        }

        return new ArrayList<>();
    }


    protected List<Message> singleTurnRouting(String nextAgent, List<Message> chatHistory) {
        if ("none".equalsIgnoreCase(nextAgent)) {
            LOGGER.info("Gracefully handle clarification.. ");
            var clarificationMessage = new AssistantMessage("I'm not sure about your request. Can you please clarify?");
            chatHistory.add(clarificationMessage);
            return chatHistory;
        }

        Agent agent = agents.stream()
                .filter(a -> a.getName()
                        .equals(nextAgent))
                .findFirst()
                .orElseThrow(() -> new AgentExecutionException("Agent not found: " + nextAgent));

        return agent.invoke(chatHistory);
    }


    private ChatMemory buildInternalChat(List<Message> chatHistory) {
        String converationId = "default";
        //build a new chat memory to preserve order of messages otherwise the model hallucinate.
        ChatMemory internalChatMemory = MessageWindowChatMemory.builder()
                .maxMessages(20)
                .build();

        internalChatMemory.add(converationId, SystemMessage.builder()
                .text(agentPrompt.getContents())
                .build());
        // filter out tool requests and tool execution results
        chatHistory.stream()
                .filter(chatMessage -> {
                    if (chatMessage instanceof ToolResponseMessage) {
                        return false;
                    }
                    if (chatMessage instanceof AssistantMessage) {
                        return !((AssistantMessage) chatMessage).hasToolCalls();
                    }
                    return true;
                })
                .forEach(chatMessaage -> {
                    internalChatMemory.add(converationId, chatMessaage);
                });
        return internalChatMemory;
    }
}
