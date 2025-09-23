from agent_framework.azure import AzureChatClient
from agent_framework import ChatAgent, MCPStreamableHTTPTool
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

    def __init__(self, azure_chat_client: AzureChatClient,
                 account_mcp_server_url: str,
                 transaction_mcp_server_url: str,
                  ):
        self.azure_chat_client = azure_chat_client
        self.account_mcp_server_url = account_mcp_server_url
        self.transaction_mcp_server_url = transaction_mcp_server_url
      


    async def build_af_agent(self) -> ChatAgent:
    
      logger.info("Building request scoped transaction agent run ")
      
      user_mail="bob.user@contoso.com"
      current_date_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
      full_instruction = TransactionHistoryAgent.instructions.format(user_mail=user_mail, current_date_time=current_date_time)

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

      return self.azure_chat_client.create_agent(
           instructions=full_instruction,
           name=TransactionHistoryAgent.name,
           tools=[account_mcp_server, transaction_mcp_server])