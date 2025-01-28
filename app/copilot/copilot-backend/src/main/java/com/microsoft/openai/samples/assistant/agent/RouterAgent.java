package com.microsoft.openai.samples.assistant.agent;

import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.ai.openai.OpenAIAsyncClient;
import com.microsoft.openai.samples.assistant.agent.cache.ToolsExecutionCache;
import com.microsoft.openai.samples.assistant.proxy.BlobStorageProxy;
import com.microsoft.openai.samples.assistant.security.LoggedUserService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RouterAgent {
    private static final Logger LOGGER = LoggerFactory.getLogger(RouterAgent.class);
    private final IntentExtractor intentExtractor;
    private final PaymentAgent paymentAgent;
    private final TransactionsReportingAgent historyReportingAgent;
    private final AccountAgent accountAgent;

    private final ToolsExecutionCache toolsExecutionCache;

    public RouterAgent(LoggedUserService loggedUserService, ToolsExecutionCache toolsExecutionCache, OpenAIAsyncClient openAIAsyncClient, DocumentIntelligenceClient documentIntelligenceClient, BlobStorageProxy blobStorageProxy, @Value("${openai.chatgpt.deployment}") String gptChatDeploymentModelId, @Value("${transactions.api.url}") String transactionsAPIUrl, @Value("${accounts.api.url}") String accountsAPIUrl, @Value("${payments.api.url}") String paymentsAPIUrl ){
        this.intentExtractor = new IntentExtractor(openAIAsyncClient,gptChatDeploymentModelId);
        this.paymentAgent = new PaymentAgent(openAIAsyncClient,loggedUserService,toolsExecutionCache,gptChatDeploymentModelId,documentIntelligenceClient,blobStorageProxy,transactionsAPIUrl,accountsAPIUrl,paymentsAPIUrl);
        this.historyReportingAgent = new TransactionsReportingAgent(openAIAsyncClient,loggedUserService,toolsExecutionCache,gptChatDeploymentModelId,transactionsAPIUrl,accountsAPIUrl);
        this.accountAgent = new AccountAgent(openAIAsyncClient,loggedUserService,toolsExecutionCache,gptChatDeploymentModelId,accountsAPIUrl);
        this.toolsExecutionCache = toolsExecutionCache;
    }

    public void run(ChatHistory chatHistory, AgentContext agentContext){
        LOGGER.info("======== Router Agent: Starting ========");
        IntentResponse intentResponse = intentExtractor.run(chatHistory);

        LOGGER.info("Intent Type for chat conversation is [{}]", intentResponse.getIntentType());

        routeToAgent(intentResponse, chatHistory, agentContext);
    }
    private void routeToAgent(IntentResponse intentResponse, ChatHistory chatHistory, AgentContext agentContext) {
        if(agentContext == null)
            agentContext = new AgentContext();

        //reset the function calls cache if it's a new conversation
        if( chatHistory.getMessages().size()<=1) {
            LOGGER.debug("Flushing tools call cache for new chat conversation : {}",chatHistory.getLastMessage().get().getContent());
            toolsExecutionCache.flush();
        }

        switch (intentResponse.getIntentType()) {
            case BillPayment:
            case RepeatTransaction:
                LOGGER.info("Routing request to PaymentAgent");
                paymentAgent.run(chatHistory, agentContext);

                chatHistory.addAssistantMessage(agentContext.getResult());
                break;
            case TransactionHistory:
                LOGGER.info("Routing request to TransactionsReportingAgent");
                historyReportingAgent.run(chatHistory, agentContext);
                chatHistory.addAssistantMessage(agentContext.getResult());
                break;
            case AccountInfo:
                LOGGER.info("Routing request to AccountAgent");
                accountAgent.run(chatHistory, agentContext);
                chatHistory.addAssistantMessage(agentContext.getResult());
                break;
            case None:
                LOGGER.info("Intent is None. Ask User to clarify with message: {}", intentResponse.getMessage());
                chatHistory.addAssistantMessage(intentResponse.getMessage()!= null ? intentResponse.getMessage() : "Sorry. Can't help with that.");
                break;
            default:
                break;
        }
    }
}