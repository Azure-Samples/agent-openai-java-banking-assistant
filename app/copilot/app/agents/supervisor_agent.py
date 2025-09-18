from agent_framework import ChatAgent
from agent_framework.foundry import FoundryChatClient
from azure.ai.projects import AIProjectClient
from app.agents.account_agent import AccountAgent
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
      - If the user request is not related to account, transactions or payments you should respond to the user that you are not able to help with the request.

      
    """
    name = "SupervisorAgent"
    description = "This agent triages customer requests and routes them to the appropriate agent."

    def __init__(self, 
                 
                 account_agent: AccountAgent,
                                ):
      self.account_agent = account_agent
     
        

    async def build_af_agent(self)-> ChatAgent:
    
      return await self.account_agent.build_af_agent()

      