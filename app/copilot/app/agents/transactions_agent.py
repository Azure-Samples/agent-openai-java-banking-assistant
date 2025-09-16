from azure.core.credentials import TokenCredential
from semantic_kernel.connectors.ai.open_ai import AzureChatCompletion
from semantic_kernel.connectors.mcp import MCPStreamableHttpPlugin
from semantic_kernel.agents import ChatCompletionAgent, Agent
from semantic_kernel.functions.kernel_plugin import KernelPlugin
from semantic_kernel.connectors.mcp import MCPPluginBase
from semantic_kernel.functions import KernelArguments
import logging
from datetime import datetime


logger = logging.getLogger(__name__)

class TransactionHistoryAgent :
    instructions = """
    you are a personal financial advisor who help the user with their recurrent bill payments. To search about the payments history you need to know the payee name.
    By default you should search the last 10 account transactions ordered by date.    
    If the user want to search last account transactions for a specific payee, extract it from the request and use it as filter.
    
    Use html list or table to display the transaction information.
    Always use the below logged user details to retrieve account info:
    '{{$user_mail}}'
    Current timestamp:
    '{{$current_date_time}}'
    """
    name = "TransactionHistoryAgent"
    description = "This agent manages user transactions related information such as banking movements and payments history."

    def __init__(self, chatCompletionService: AzureChatCompletion, account_mcp_plugin: MCPPluginBase, transaction_mcp_plugin: MCPPluginBase):
        self.chatCompletionService = chatCompletionService
        self.account_mcp_plugin = account_mcp_plugin
        self.transaction_mcp_plugin = transaction_mcp_plugin


    async def build_sk_agent(self)-> Agent:
    
      logger.info("Initializing MCP connection for transaction api ")
      # This is just an hack to make it work withoun running the whole orchestration 
      # logic in 1 big async with block.
      # I'm not sure when __aexit__ should be called here. I guess we should expose exit/dispose method
      # on the agent and call it when the agent is not needed anymore.
      await self.account_mcp_plugin.__aenter__()
      await self.transaction_mcp_plugin.__aenter__()

    #get the current time and date in python
      current_date_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
      argument_overrides = KernelArguments(user_mail="bob.user@contoso.com",current_date_time=current_date_time)
      return ChatCompletionAgent(
            service=self.chatCompletionService,
            name=TransactionHistoryAgent.name,
            instructions=TransactionHistoryAgent.instructions,
            plugins=[self.account_mcp_plugin,self.transaction_mcp_plugin],
            arguments=argument_overrides
        )