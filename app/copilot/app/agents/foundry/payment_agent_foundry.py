from azure.core.credentials import TokenCredential
from agent_framework.foundry import FoundryChatClient
from azure.ai.projects import AIProjectClient
from agent_framework import ChatAgent, MCPStreamableHTTPTool
from app.helpers.document_intelligence_scanner import DocumentIntelligenceInvoiceScanHelper
from app.config.azure_credential import get_azure_credential_async
from datetime import datetime

import logging


logger = logging.getLogger(__name__)

class PaymentAgent :
    instructions = """
    you are a personal financial advisor who help the user with their recurrent bill payments. The user may want to pay the bill uploading a photo of the bill, or it may start the payment checking transactions history for a specific payee.
        For the bill payment you need to know the: bill id or invoice number, payee name, the total amount.
        If you don't have enough information to pay the bill ask the user to provide the missing information.
        If the user submit a photo, always ask the user to confirm the extracted data from the photo.
        Always check if the bill has been paid already based on payment history before asking to execute the bill payment.
        Ask for the payment method to use based on the available methods on the user account.
        if the user wants to pay using bank transfer, check if the payee is in account registered beneficiaries list. If not ask the user to provide the payee bank code.
        Check if the payment method selected by the user has enough funds to pay the bill. Don't use the account balance to evaluate the funds.
        Before submitting the payment to the system ask the user confirmation providing the payment details.
        Include in the payment description the invoice id or bill id as following: payment for invoice 1527248.
        When submitting payment always use the available functions to retrieve accountId, paymentMethodId.
        If the payment succeeds provide the user with the payment confirmation. If not provide the user with the error message.
        Use HTML list or table to display bill extracted data, payments, account or transaction details.
        Always use the below logged user details to retrieve account info:
       {user_mail}
        Current timestamp:
       {current_date_time}
        Don't try to guess accountId,paymentMethodId from the conversation.When submitting payment always use functions to retrieve accountId, paymentMethodId.
        
        ### Output format
        - Example of showing Payment information:
            <table border="1">
              <tr>
                <th>Payee Name</th>
                <td>contoso</td>
              </tr>
              <tr>
                <th>Invoice ID</th>
                <td>9524011000817857</td>
              </tr>
              <tr>
                <th>Amount</th>
                <td>€85.20</td>
              </tr>
              <tr>
                <th>Payment Method</th>
                <td>Visa (Card Number: ***477)</td>
              </tr>
              <tr>
                <th>Description</th>
                <td>Payment for invoice 9524011000817857</td>
              </tr>
            </table>
            
        - Example of showing Payment methods:
            <ol>
              <li><strong>Bank Transfer</strong></li>
              <li><strong>Visa</strong> (Card Number: ***3667)</li>
            </ol>
        
        """
    name = "PaymentAgent"
    description = "This agent manages user payments related information such as submitting payment requests and bill payments."

    def __init__(self, foundry_project_client: AIProjectClient,
                  chat_deployment_name:str,
                  account_mcp_server: MCPStreamableHTTPTool,
                  transaction_mcp_server: MCPStreamableHTTPTool,
                  payment_mcp_server: MCPStreamableHTTPTool,
                  document_scanner_helper : DocumentIntelligenceInvoiceScanHelper,
                  foundry_endpoint: str  ):
        self.foundry_project_client = foundry_project_client
        self.account_mcp_server = account_mcp_server
        self.transaction_mcp_server = transaction_mcp_server
        self.payment_mcp_server = payment_mcp_server    
        self.foundry_endpoint = foundry_endpoint
        self.document_scanner_helper = document_scanner_helper
        
        self.created_agent = foundry_project_client.agents.create_agent(
            model=chat_deployment_name, name=PaymentAgent.name, description=PaymentAgent.description
        )


    async def build_af_agent(self)-> ChatAgent:
    
      logger.info("Building request scoped transaction agent run ")
      
      user_mail="bob.user@contoso.com"
      current_date_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
      full_instruction = PaymentAgent.instructions.format(user_mail=user_mail, current_date_time=current_date_time)

      credential = await get_azure_credential_async()  
      
      
      logger.info("Initializing Account MCP server tools ")
      await self.account_mcp_server.__aenter__()
      
      logger.info("Initializing Transaction MCP server tools ")
      await self.transaction_mcp_server.__aenter__()

      logger.info("Initializing Payment  MCP server tools ")
      await self.payment_mcp_server.__aenter__()

      chat_agent =  ChatAgent(
            chat_client=FoundryChatClient(project_endpoint=self.foundry_endpoint, async_credential=credential, agent_id=self.created_agent.id),
            instructions=full_instruction,
            tools=[self.account_mcp_server,self.transaction_mcp_server,self.payment_mcp_server,self.document_scanner_helper.scan_invoice_plugin]
        ) 
      return chat_agent