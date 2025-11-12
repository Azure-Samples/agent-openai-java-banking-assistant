from agent_framework.azure import AzureOpenAIChatClient
from agent_framework import ChatAgent, MCPStreamableHTTPTool

import logging


logger = logging.getLogger(__name__)

class AccountAgent :
    instructions = """
    you are a personal financial advisor who help the user to retrieve information about their bank accounts.
    Use html list or table to display the account information.
    Always use the below logged user details to retrieve account info:
    {user_mail}
    """
    name = "AccountAgent"
    description = "This agent manages user accounts related information such as balance, credit cards."

    def __init__(self, azure_chat_client: AzureOpenAIChatClient, account_mcp_server_url: str):
        self.azure_chat_client = azure_chat_client
        self.account_mcp_server_url = account_mcp_server_url



    async def build_af_agent(self)-> ChatAgent:
    
      logger.info("Initializing Account Agent connection for account api ")
      
      user_mail="bob.user@contoso.com"
      full_instruction = AccountAgent.instructions.format(user_mail=user_mail)

      account_mcp_server = MCPStreamableHTTPTool(
            name="Account MCP server client",
            url=self.account_mcp_server_url)
      logger.info("Initializing Account MCP server tools ")

      await account_mcp_server.connect()
      return ChatAgent(
            chat_client=self.azure_chat_client,
            instructions=full_instruction,
            name=AccountAgent.name,
            tools=[account_mcp_server]
        )
    
