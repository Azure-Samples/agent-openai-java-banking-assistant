from agent_framework import ChatAgent
from agent_framework.foundry import FoundryChatClient
from azure.ai.projects import AIProjectClient
from app.agents.foundry.account_agent_foundry import AccountAgent
from app.agents.foundry.transaction_agent_foundry import TransactionHistoryAgent
from app.agents.foundry.payment_agent_foundry import PaymentAgent
from agent_framework import AgentThread
from app.config.azure_credential import get_azure_credential_async
import logging


logger = logging.getLogger(__name__)

class SupervisorAgent :
    """ this agent is used in agent-as-tool orchestration as supervisor agent to decide which tool/agent to use.
    """
    instructions = """
      You are a banking customer support agent triaging customer requests about their banking account, movements, payments.
      You have to evaluate the whole conversation with the customer and forward it to the appropriate agent based on triage rules.
      Once you got a response from an agent use it to provide the answer to the customer.
      
      
      # Triage rules
      - If the user request is related to bank account information like account balance, payment methods, cards and beneficiaries book you should route the request to AccountAgent.
      - If the user request is related to banking movements and payments history, you should route the request to TransactionHistoryAgent.
      - If the user request is related to initiate a payment request, upload a bill or invoice image for payment or manage an on-going payment process, you should route the request to PaymentAgent.
      - If the user request is not related to account, transactions or payments you should respond to the user that you are not able to help with the request.

      
    """
    name = "SupervisorAgent"
    description = "This agent triages customer requests and routes them to the appropriate agent."

    def __init__(self, 
                 foundry_project_client: AIProjectClient, 
                 chat_deployment_name:str,
                 account_agent: AccountAgent,
                 transaction_agent: TransactionHistoryAgent,
                 payment_agent: PaymentAgent,
                 foundry_endpoint: str,
                 agent_id: str
                                ):
      self.account_agent = account_agent
      self.transaction_agent = transaction_agent  
      self.payment_agent = payment_agent
      self.foundry_project_client = foundry_project_client
      self.foundry_endpoint = foundry_endpoint
      self.agent_id = agent_id
      self.thread = AgentThread()



    async def set_active_thread(self,thread: AgentThread):
        self.thread = thread

    async def build_af_agent(self)-> ChatAgent:
      
      credential = await get_azure_credential_async()  
      return ChatAgent(
            chat_client=FoundryChatClient(project_endpoint=self.foundry_endpoint, async_credential=credential, agent_id=self.agent_id),
            instructions=SupervisorAgent.instructions,
            tools=[self.route_to_account_agent,self.route_to_transaction_agent,self.route_to_payment_agent]
        ) 


    async def route_to_account_agent(self, user_message: str) -> str:
       """ Route the conversation to Account Agent"""
       af_account_agent = await self.account_agent.build_af_agent()

       response = await af_account_agent.run(user_message, thread=self.thread)
       return response.text
    
    async def route_to_transaction_agent(self, user_message: str) -> str:
       """ Route the conversation to Transaction History Agent"""
       af_transaction_agent = await self.transaction_agent.build_af_agent()

       response = await af_transaction_agent.run(user_message, thread=self.thread)
       return response.text
    
    async def route_to_payment_agent(self, user_message: str) -> str:
       """ Route the conversation to Payment Agent"""
       af_payment_agent = await self.payment_agent.build_af_agent()

       response = await af_payment_agent.run(user_message, thread=self.thread)
       return response.text
