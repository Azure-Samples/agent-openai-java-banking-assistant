from agent_framework import ChatAgent
from agent_framework import AgentThread, ChatMessageList
from agent_framework.azure import AzureChatClient
from app.agents.azure_chat.account_agent import AccountAgent
from app.agents.azure_chat.transaction_agent import TransactionHistoryAgent
from app.agents.azure_chat.payment_agent import PaymentAgent
from uuid import uuid4
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
                 azure_chat_client: AzureChatClient,
                 account_agent: AccountAgent,
                 transaction_agent: TransactionHistoryAgent,
                 payment_agent: PaymentAgent,
                 thread: AgentThread = AgentThread()
                                ):
      self.azure_chat_client = azure_chat_client
      self.account_agent = account_agent
      self.transaction_agent = transaction_agent
      self.payment_agent = payment_agent
      self.thread = thread
     
        

    async def _build_af_agent(self) -> ChatAgent:
      
     
      return self.azure_chat_client.create_agent(
           instructions=SupervisorAgent.instructions,
           name=SupervisorAgent.name,
           tools=[self.route_to_account_agent,self.route_to_transaction_agent,self.route_to_payment_agent])

    async def processMessage(self, user_message: str , thread_id : str | None, chat_message_list: ChatMessageList) -> tuple[str, str | None]:
      """Process a chat message using the injected Azure Chat Completion service and return response and thread id."""
      #For azure chat based agents we need to provide the message history externally as there is no built-in memory thread implementation per thread id.
      
      
      agent = await self._build_af_agent()

      processed_thread_id = thread_id
      thread = None
      # The AgentThread doesn't allow to provide an external service id when using external message_store so we need to manage the thread id externally.
      if thread_id is None:
         thread = agent.get_new_thread()
         processed_thread_id = str(uuid4())

      else :
        thread = AgentThread(message_store=chat_message_list)

      # set the thread as class instance variable so that it can be shared by agents called in the tools
      self.thread = thread

      response = await agent.run(user_message, thread=thread)
      return response.text, processed_thread_id

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