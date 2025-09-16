from azure.core.credentials import TokenCredential
from semantic_kernel.connectors.ai.open_ai import AzureChatCompletion
from semantic_kernel.connectors.mcp import MCPStreamableHttpPlugin
from semantic_kernel.agents import ChatCompletionAgent, Agent
from semantic_kernel.functions.kernel_plugin import KernelPlugin
from semantic_kernel.connectors.mcp import MCPPluginBase
from semantic_kernel.functions import KernelArguments
from app.agents.account_agent import AccountAgent
from app.agents.transactions_agent import TransactionHistoryAgent
from app.agents.payment_agent import PaymentAgent
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

      when
    """
    name = "SupervisorAgent"
    description = "This agent triages customer requests and routes them to the appropriate agent."

    def __init__(self, 
                 chatCompletionService: AzureChatCompletion,
                 account_agent: AccountAgent,
                 transaction_agent: TransactionHistoryAgent,
                 payment_agent: PaymentAgent                 ):
        self.chatCompletionService = chatCompletionService
        self.account_agent = account_agent
        self.transaction_agent = transaction_agent
        self.payment_agent = payment_agent


    async def build_sk_agent(self)-> Agent:
    
      account_agent_sk = await self.account_agent.build_sk_agent()
      transaction_agent_sk = await self.transaction_agent.build_sk_agent()
      payment_agent_sk = await self.payment_agent.build_sk_agent()  

      return  ChatCompletionAgent(
            service=self.chatCompletionService,
            name=SupervisorAgent.name,
            instructions=SupervisorAgent.instructions,
            plugins=[account_agent_sk, transaction_agent_sk, payment_agent_sk]

      )