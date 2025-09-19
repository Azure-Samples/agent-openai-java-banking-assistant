from azure.core.credentials import TokenCredential
from agent_framework.foundry import FoundryChatClient
from azure.ai.projects import AIProjectClient
from agent_framework import ChatAgent, MCPStreamableHTTPTool
from app.config.azure_credential import get_azure_credential_async
from datetime import datetime

import logging


logger = logging.getLogger(__name__)

class TransactionHistoryAgent :
    instructions = """
    you are a personal financial advisor who help the user with their recurrent bill payments. To search about the payments history you need to know the payee name.
    By default you should search the last 10 account transactions ordered by date.    
    If the user want to search last account transactions for a specific payee, extract it from the request and use it as filter.
    
    Use html list or table to display the transaction information.
    Always use the below logged user details to retrieve account info:
    {user_mail}
    Current timestamp:
    {current_date_time}
    """
    name = "TransactionHistoryAgent"
    description = "This agent manages user transactions related information such as banking movements and payments history"

    def __init__(self, foundry_project_client: AIProjectClient, 
                 chat_deployment_name:str,
                 account_mcp_server_url: str,
                 transaction_mcp_server_url: str,
                 foundry_endpoint: str  ):
        self.foundry_project_client = foundry_project_client
        self.account_mcp_server_url = account_mcp_server_url
        self.transaction_mcp_server_url = transaction_mcp_server_url
        self.foundry_endpoint = foundry_endpoint
        self.created_agent = foundry_project_client.agents.create_agent(
            model=chat_deployment_name, name=TransactionHistoryAgent.name, description=TransactionHistoryAgent.description
        )


    async def build_af_agent(self, thread_id: str | None) -> ChatAgent:
    
      logger.info("Building request scoped transaction agent run ")
      
      user_mail="bob.user@contoso.com"
      current_date_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
      full_instruction = TransactionHistoryAgent.instructions.format(user_mail=user_mail, current_date_time=current_date_time)

      credential = await get_azure_credential_async()  
    
      logger.info("Initializing Account MCP server tools ")
      #await self.account_mcp_server.__aenter__()
      account_mcp_server = MCPStreamableHTTPTool(
        name="Account MCP server client",
        url=self.account_mcp_server_url
     )
      await account_mcp_server.connect()
     
      logger.info("Initializing Transaction MCP server tools ")
      transaction_mcp_server = MCPStreamableHTTPTool(
        name="Transaction MCP server client",
        url=self.transaction_mcp_server_url
     )
      await transaction_mcp_server.connect()

      chat_agent =  ChatAgent(
            chat_client=FoundryChatClient(thread_id=thread_id, project_endpoint=self.foundry_endpoint, async_credential=credential, agent_id=self.created_agent.id),
            instructions=full_instruction,
            tools=[account_mcp_server, transaction_mcp_server]
        ) 
      return chat_agent