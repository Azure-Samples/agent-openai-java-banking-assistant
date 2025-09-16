from azure.core.credentials import TokenCredential
from semantic_kernel.connectors.ai.open_ai import AzureChatCompletion
from semantic_kernel.connectors.mcp import MCPStreamableHttpPlugin
from semantic_kernel.agents import ChatCompletionAgent, Agent
from semantic_kernel.functions.kernel_plugin import KernelPlugin
from semantic_kernel.connectors.mcp import MCPPluginBase
from semantic_kernel.functions import KernelArguments
from app.helpers.document_intelligence_scanner import DocumentIntelligenceInvoiceScanHelper
import logging
from datetime import datetime


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
       '{{$user_mail}}'
        Current timestamp:
       '{{$current_date_time}}'
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
    description = "This agent manages user payments related information such as submitting payment requests."

    def __init__(self, chatCompletionService: AzureChatCompletion, 
                 account_mcp_plugin: MCPPluginBase, 
                 transaction_mcp_plugin: MCPPluginBase, 
                 payment_mcp_plugin: MCPPluginBase,
                scan_invoice_helper: DocumentIntelligenceInvoiceScanHelper  ):
        self.chatCompletionService = chatCompletionService
        self.account_mcp_plugin = account_mcp_plugin
        self.transaction_mcp_plugin = transaction_mcp_plugin
        self.payment_mcp_plugin = payment_mcp_plugin
        self.scan_invoice_helper = scan_invoice_helper


    async def build_sk_agent(self)-> Agent:
    
      logger.info("Initializing MCP connection for payment api ")
      # This is just an hack to make it work withoun running the whole orchestration 
      # logic in 1 big async with block.
      # I'm not sure when __aexit__ should be called here. I guess we should expose exit/dispose method
      # on the agent and call it when the agent is not needed anymore.
      await self.account_mcp_plugin.__aenter__()
      await self.transaction_mcp_plugin.__aenter__()
      await self.payment_mcp_plugin.__aenter__()

   
    #get the current time and date in python
      current_date_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
      argument_overrides = KernelArguments(user_mail="bob.user@contoso.com",current_date_time=current_date_time)
      return ChatCompletionAgent(
            service=self.chatCompletionService,
            name=PaymentAgent.name,
            instructions=PaymentAgent.instructions,
            plugins=[self.account_mcp_plugin,self.transaction_mcp_plugin,self.payment_mcp_plugin,self.scan_invoice_helper],
            arguments=argument_overrides
        )