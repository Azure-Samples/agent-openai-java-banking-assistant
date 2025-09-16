from app.models.chat import ChatMessage, ChatAppRequest, ChatResponse
from semantic_kernel.connectors.ai.open_ai import AzureChatCompletion
from semantic_kernel.agents import ChatHistoryAgentThread, GroupChatOrchestration, OrchestrationHandoffs
from dependency_injector.wiring import Provide, inject
from app.agents.account_agent import AccountAgent
from app.agents.transactions_agent import TransactionHistoryAgent
from app.agents.payment_agent import PaymentAgent
from app.agents.supervisor_agent import SupervisorAgent
from semantic_kernel.agents.runtime import InProcessRuntime
from semantic_kernel.contents.chat_message_content import ChatMessageContent
from app.config.observability import enable_trace

import logging

logger = logging.getLogger(__name__)

class SupervisorOrchestrationService:
   def  __init__(self,supervisorAgent: SupervisorAgent):

    self.supervisorAgent = supervisorAgent

   @enable_trace
   async def processMessage(self, thread : ChatHistoryAgentThread | None) -> str:
        """Process a chat message using the injected Azure Chat Completion service."""

        sk_supervisor_agent =  await self.supervisorAgent.build_sk_agent()

        response = await sk_supervisor_agent.get_response(thread=thread)

        return response.content.content