package com.microsoft.openai.samples.assistant.agent;

import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.ai.openai.OpenAIAsyncClient;
import com.microsoft.openai.samples.assistant.proxy.BlobStorageProxy;
import com.microsoft.openai.samples.assistant.security.LoggedUserService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AgentRouter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentRouter.class);
    private final IntentAgent intentAgent;
    private final PaymentAgent paymentAgent;
    private final HistoryReportingAgent historyReportingAgent;
    private final AccountAgent accountAgent;

    public AgentRouter(LoggedUserService loggedUserService, OpenAIAsyncClient openAIAsyncClient, DocumentIntelligenceClient documentIntelligenceClient, BlobStorageProxy blobStorageProxy, @Value("${openai.chatgpt.deployment}") String gptChatDeploymentModelId, @Value("${transactions.api.url}") String transactionsAPIUrl, @Value("${accounts.api.url}") String accountsAPIUrl, @Value("${payments.api.url}") String paymentsAPIUrl ){
        this.intentAgent = new IntentAgent(openAIAsyncClient,gptChatDeploymentModelId);
        this.paymentAgent = new PaymentAgent(openAIAsyncClient,loggedUserService,gptChatDeploymentModelId,documentIntelligenceClient,blobStorageProxy,transactionsAPIUrl,accountsAPIUrl,paymentsAPIUrl);
        this.historyReportingAgent = new HistoryReportingAgent(openAIAsyncClient,loggedUserService,gptChatDeploymentModelId,transactionsAPIUrl,accountsAPIUrl);
        this.accountAgent = new AccountAgent(openAIAsyncClient,loggedUserService,gptChatDeploymentModelId,accountsAPIUrl);

    }

    public void run(ChatHistory chatHistory, AgentContext agentContext){
        IntentResponse intentResponse = intentAgent.run(chatHistory, agentContext);

        LOGGER.info("Intent Type for chat conversation: {}", intentResponse.getIntentType());

        routeToAgent(intentResponse, chatHistory, agentContext);
    }
    private void routeToAgent(IntentResponse intentResponse, ChatHistory chatHistory, AgentContext agentContext) {
        if(agentContext == null)
            agentContext = new AgentContext();

        switch (intentResponse.getIntentType()) {
            case BillPayment:
            case RepeatTransaction:
                paymentAgent.run(chatHistory, agentContext);
                chatHistory.addAssistantMessage(agentContext.getResult());
                break;
            case TransactionHistory:
                historyReportingAgent.run(chatHistory, agentContext);
                chatHistory.addAssistantMessage(agentContext.getResult());
                break;
            case AccountInfo:
                accountAgent.run(chatHistory, agentContext);
                chatHistory.addAssistantMessage(agentContext.getResult());
                break;
            case None:
                chatHistory.addAssistantMessage(agentContext.getResult());
                break;
            default:
                break;
        }
    }
}