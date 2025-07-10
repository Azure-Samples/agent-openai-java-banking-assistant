package com.microsoft.openai.samples.assistant.config.agent.isolated;


import com.google.adk.agents.LlmAgent;
import com.google.adk.models.langchain4j.LangChain4j;
import com.google.adk.tools.*;
import dev.langchain4j.model.chat.ChatModel;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class CollaborationEvaluatorAgent {
    ChatModel chatModel;


    public CollaborationEvaluatorAgent(ChatModel chatModel
    ) {
        this.chatModel  = chatModel;
    }

    @Bean(name = "adkCollaborationEvaluatorAgent")
    public LlmAgent getAgent() {

        return LlmAgent.builder()
                .name("CollaborationEvaluatorAgent")
                .model(new LangChain4j(this.chatModel))
                .instruction("""
                 You need to evaluate a conversation among agents collaborating to solve requests from home banking customers.
                 #Evaluation Rules
                    - if account ID is required let account agent to figure that out.
                    - If during transaction review or payment processing account info details are needed agents need to keep working together.
                    - If user input or review is required or an answer with full details about user ask is generated you MUST call the 'exitLoop' function.
                    - DO NOT CALL 'exitLoop' function if account ID is required.
                      """)
                //.subAgents(accountAgent,transactionAgent,paymentAgent)
                .tools(FunctionTool.create(CollaborationEvaluatorAgent.class, "exitLoop"))
                .build();

    }

    @Annotations.Schema(
            description =
                    "Call this function ONLY when collaboration between agents need to stop or pause for user input or to show final response")
    public static Map<String, Object> exitLoop(@Annotations.Schema(name = "toolContext") ToolContext toolContext) {
        System.out.printf("[Tool Call] exitLoop triggered by %s \n", toolContext.agentName());
        toolContext.actions().setEscalate(true);
        //  Return empty dict as tools should typically return JSON-serializable output
        return Map.of();
    }


}
