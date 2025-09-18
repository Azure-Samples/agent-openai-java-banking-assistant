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
                 account_mcp_server: MCPStreamableHTTPTool,
                 transaction_mcp_server: MCPStreamableHTTPTool,
                 foundry_endpoint: str  ):
        self.foundry_project_client = foundry_project_client
        self.account_mcp_server = account_mcp_server
        self.transaction_mcp_server = transaction_mcp_server
        self.foundry_endpoint = foundry_endpoint
        self.created_agent = foundry_project_client.agents.create_agent(
            model=chat_deployment_name, name=TransactionHistoryAgent.name, description=TransactionHistoryAgent.description
        )


    async def build_af_agent(self)-> ChatAgent:
    
      logger.info("Building request scoped transaction agent run ")
      
      user_mail="bob.user@contoso.com"
      current_date_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
      full_instruction = TransactionHistoryAgent.instructions.format(user_mail=user_mail, current_date_time=current_date_time)

      credential = await get_azure_credential_async()  
    
      logger.info("Initializing Account MCP server tools ")
      await self.account_mcp_server.__aenter__()
     
      logger.info("Initializing Transaction MCP server tools ")
      await self.transaction_mcp_server.__aenter__()
      
      chat_agent =  ChatAgent(
            chat_client=FoundryChatClient(project_endpoint=self.foundry_endpoint, async_credential=credential, agent_id=self.created_agent.id),
            instructions=full_instruction,
            tools=[self.account_mcp_server, self.transaction_mcp_server]
        ) 
      return chat_agent