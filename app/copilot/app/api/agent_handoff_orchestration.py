from app.models.chat import ChatMessage, ChatAppRequest, ChatResponse
from semantic_kernel.connectors.ai.open_ai import AzureChatCompletion
from semantic_kernel.agents import ChatHistoryAgentThread, HandoffOrchestration, OrchestrationHandoffs
from dependency_injector.wiring import Provide, inject
from app.agents.account_agent import AccountAgent
from app.agents.transactions_agent import TransactionHistoryAgent
from app.agents.triage_agent import TriageAgent
from app.agents.payment_agent import PaymentAgent
from semantic_kernel.agents.runtime import InProcessRuntime
from semantic_kernel.contents.chat_message_content import ChatMessageContent

import logging

logger = logging.getLogger(__name__)

class HandoffOrchestrationService:
   def  __init__(self,
    accountAgent: AccountAgent,
    transactionAgent: TransactionHistoryAgent,
    triageAgent: TriageAgent,
    paymentAgent: PaymentAgent):

    self.accountAgent = accountAgent
    self.transactionAgent = transactionAgent
    self.triageAgent = triageAgent
    self.paymentAgent = paymentAgent

    self.handoffs = (
    OrchestrationHandoffs()
    .add_many(
        source_agent=triageAgent.name,
        target_agents={
            accountAgent.name: "Transfer to this agent for any request related to bank account information ",
            transactionAgent.name: "Transfer to this agent for retrieving banking movements and payments history information",
            paymentAgent.name: "Transfer to this agent submitting payment request",
        },
    )
    .add(
        source_agent=accountAgent.name,
        target_agent=triageAgent.name,
        description="Transfer to this agent if the request is not related to banking account info",
    )
    .add(
        source_agent=transactionAgent.name,
        target_agent=triageAgent.name,
        description="Transfer to this agent if the request is not related to banking movements and payments history",
    )
    .add(
        source_agent=paymentAgent.name,
        target_agent=triageAgent.name,
        description="Transfer to this agent if the request is not related to payment processing",
    )
    )



   async def _build_sk_orchestration(self) -> HandoffOrchestration:
        logging.info( "Building SK account agent")
        sk_account_agent =  await self.accountAgent.build_sk_agent()

        logging.info( "Building SK transaction agent")
        sk_transaction_agent =  await self.transactionAgent.build_sk_agent()

        logging.info( "Building SK triage agent")
        sk_triage_agent =  await self.triageAgent.build_sk_agent()

        logging.info( "Building SK payment agent")
        sk_payment_agent =  await self.paymentAgent.build_sk_agent()

        return HandoffOrchestration(
            members=[sk_account_agent, sk_transaction_agent, sk_triage_agent, sk_payment_agent],
            handoffs=self.handoffs)
   

   async def processMessage(self,chat_history : list[ChatMessageContent]) -> ChatResponse:
        """Process a chat message using the injected Azure Chat Completion service."""
    

        handoff_orchestration : HandoffOrchestration = await self._build_sk_orchestration()


        # 2. Create a runtime and start it
        runtime = InProcessRuntime()
        runtime.start()

        # 3. Invoke the orchestration with a task and the runtime
        orchestration_result = await handoff_orchestration.invoke(
            task=chat_history,
            runtime=runtime,
        )

        # 4. Wait for the results
        value  = await orchestration_result.get()
        print(value)

        # 5. Stop the runtime after the invocation is complete
        await runtime.stop_when_idle()

        # Extract string content from ChatMessageContent
        if isinstance(value, list):
            content = "\n".join([msg.content for msg in value if hasattr(msg, 'content')])
        elif hasattr(value, 'content'):
            content = value.content
        else:
            content = str(value)

        return ChatResponse(content=content)