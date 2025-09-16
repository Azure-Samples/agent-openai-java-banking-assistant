from azure.core.credentials import TokenCredential
from semantic_kernel.connectors.ai.open_ai import AzureChatCompletion
from semantic_kernel.connectors.mcp import MCPStreamableHttpPlugin
from semantic_kernel.agents import ChatCompletionAgent, Agent
from semantic_kernel.functions.kernel_plugin import KernelPlugin
from semantic_kernel.connectors.mcp import MCPPluginBase
from semantic_kernel.functions import KernelArguments
import logging


logger = logging.getLogger(__name__)

class AccountAgent :
    instructions = """
    you are a personal financial advisor who help the user to retrieve information about their bank accounts.
    Use html list or table to display the account information.
    Always use the below logged user details to retrieve account info:
    {{$user_mail}}
    """
    name = "AccountAgent"
    description = "This agent manages user accounts related information such as balance, credit cards."

    def __init__(self, chatCompletionService: AzureChatCompletion, account_mcp_plugin: MCPPluginBase):
        self.chatCompletionService = chatCompletionService
        self.account_mcp_plugin = account_mcp_plugin


    async def build_sk_agent(self)-> Agent:
    
      logger.info("Initializing MCP connection for account api ")
      # This is just an hack to make it work withoun running the whole orchestration 
      # logic in 1 big async with block.
      # I'm not sure when __aexit__ should be called here. I guess we should expose exit/dispose method
      # on the agent and call it when the agent is not needed anymore.
      await self.account_mcp_plugin.__aenter__()

      argument_overrides = KernelArguments(user_mail="bob.user@contoso.com")
      return ChatCompletionAgent(
            service=self.chatCompletionService,
            name=AccountAgent.name,
            instructions=AccountAgent.instructions,
            plugins=[self.account_mcp_plugin],
            arguments=argument_overrides
        )