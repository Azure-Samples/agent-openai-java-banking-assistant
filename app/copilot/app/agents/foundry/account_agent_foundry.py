from azure.core.credentials import TokenCredential
from agent_framework.foundry import FoundryChatClient
from azure.ai.projects import AIProjectClient
from agent_framework import ChatAgent, MCPStreamableHTTPTool
from app.config.azure_credential import get_azure_credential_async

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

    def __init__(self, foundry_project_client: AIProjectClient, 
                 chat_deployment_name:str,
                 account_mcp_server_url: str,
                 foundry_endpoint: str  ):
        self.foundry_project_client = foundry_project_client
        self.account_mcp_server_url = account_mcp_server_url
        self.foundry_endpoint = foundry_endpoint
        self.created_agent = foundry_project_client.agents.create_agent(
            model=chat_deployment_name, name=AccountAgent.name, description=AccountAgent.description
        )
        


    async def build_af_agent(self, thread_id: str | None) -> ChatAgent:

      logger.info("Building request scoped Account agent run ")

      user_mail="bob.user@contoso.com"
      full_instruction = AccountAgent.instructions.format(user_mail=user_mail)

      credential = await get_azure_credential_async()  
          
      
      logger.info("Initializing Account MCP server tools ")
      #await self.account_mcp_server.__aenter__()
      account_mcp_server = MCPStreamableHTTPTool(
        name="Account MCP server client",
        url=self.account_mcp_server_url
     )
      await account_mcp_server.connect()

      chat_agent =  ChatAgent(
            chat_client=FoundryChatClient(thread_id=thread_id, project_endpoint=self.foundry_endpoint, async_credential=credential, agent_id=self.created_agent.id),
            instructions=full_instruction,
            tools=[account_mcp_server]
        ) 
      return chat_agent