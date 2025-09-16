"""Dependency injection container configuration."""

from dependency_injector import containers, providers
from semantic_kernel.connectors.ai.open_ai import AzureChatCompletion
from semantic_kernel.connectors.mcp import MCPStreamableHttpPlugin
from azure.ai.documentintelligence import DocumentIntelligenceClient
from azure.storage.blob import BlobServiceClient
from app.api.agent_astool_orchestration import SupervisorOrchestrationService
from app.helpers.blob_proxy import BlobStorageProxy
from app.helpers.document_intelligence_scanner import DocumentIntelligenceInvoiceScanHelper
from app.config.azure_credential import get_azure_credential, get_azure_credential_async
from app.config.settings import settings
from app.agents.account_agent import AccountAgent
from app.agents.transactions_agent import TransactionHistoryAgent
from app.agents.payment_agent import PaymentAgent
from app.agents.triage_agent import TriageAgent
from app.agents.supervisor_agent import SupervisorAgent
from app.api.agent_handoff_orchestration import HandoffOrchestrationService


class Container(containers.DeclarativeContainer):
    """IoC container for application dependencies."""
   
    # Helpers
    blob_service_client = providers.Singleton(
        BlobServiceClient,
        credential = providers.Factory(get_azure_credential),
        account_url = f"https://{settings.AZURE_STORAGE_ACCOUNT}.blob.core.windows.net"
    )

    blob_proxy = providers.Singleton(
        BlobStorageProxy,
        client = blob_service_client,
        container_name = settings.AZURE_STORAGE_CONTAINER
    )

    # Document Intelligence client singleton
    document_intelligence_client = providers.Singleton(
        DocumentIntelligenceClient,
        credential=providers.Factory(get_azure_credential),
        endpoint=f"https://{settings.AZURE_DOCUMENT_INTELLIGENCE_SERVICE}.cognitiveservices.azure.com/"
    )

    # Document Intelligence scanner singleton
    document_intelligence_scanner = providers.Singleton(
        DocumentIntelligenceInvoiceScanHelper,
        client=document_intelligence_client,
        blob_storage_proxy=blob_proxy
    )
    
    # Azure Chat Completion singleton
    azure_chat_completion = providers.Singleton(
        AzureChatCompletion,
        credential=providers.Factory(get_azure_credential),
        deployment_name=settings.AZURE_OPENAI_CHATGPT_DEPLOYMENT,
        endpoint=f"https://{settings.AZURE_OPENAI_SERVICE}.openai.azure.com/"
    )

    #MCP plugin need to be initialized using an async __aenter__ method and disposed using __aexit__
    #We will use it as a singleton for now, and let the agent to call __aenter__ when building the agent
    account_mcp_plugin = providers.Singleton(
       MCPStreamableHttpPlugin,
       name="AccountClient",
       description="tools for account related operations",
       timeout=1000,
       url=f"{settings.ACCOUNT_MCP_URL}/mcp",
   )
    
    transaction_mcp_plugin = providers.Singleton(
       MCPStreamableHttpPlugin,
       name="TransactionClient",
       description="tools for banking movements and payments history",
       url=f"{settings.TRANSACTION_MCP_URL}/mcp",
    )

    payment_mcp_plugin = providers.Singleton(
       MCPStreamableHttpPlugin,
       name="PaymentClient",
       description="tools for submitting payment request",
       url=f"{settings.PAYMENT_MCP_URL}/mcp",
    )
    
    # Account Agent
    account_agent = providers.Singleton(
        AccountAgent,
        chatCompletionService=azure_chat_completion,
        account_mcp_plugin=account_mcp_plugin)
    
    # Transaction History Agent
    transaction_history_agent = providers.Singleton(
        TransactionHistoryAgent,
        chatCompletionService=azure_chat_completion,
        account_mcp_plugin=account_mcp_plugin,
        transaction_mcp_plugin=transaction_mcp_plugin)
    
    #Payment Agent
    payment_agent = providers.Singleton(
        PaymentAgent,
        chatCompletionService=azure_chat_completion,
        account_mcp_plugin=account_mcp_plugin,
        transaction_mcp_plugin=transaction_mcp_plugin,
        payment_mcp_plugin=payment_mcp_plugin,
        scan_invoice_helper=document_intelligence_scanner)

    # Triage Agent to be used in handoff orchestration
    triage_agent = providers.Singleton(
        TriageAgent,
        chatCompletionService=azure_chat_completion
    )

    # Supervisor Agent to be used in agents-as-tool orchestration
    supervisor_agent = providers.Singleton(
        SupervisorAgent,
        chatCompletionService=azure_chat_completion,
        account_agent=account_agent,
        transaction_agent=transaction_history_agent,
        payment_agent=payment_agent
    )

    # Orchestration service for handoff orchestration
    handoff_orchestration_service = providers.Singleton(
        HandoffOrchestrationService,
        accountAgent=account_agent,
        transactionAgent=transaction_history_agent,
        triageAgent=triage_agent,
        paymentAgent=payment_agent
    )

    supervisor_orchestration_service = providers.Singleton(
        SupervisorOrchestrationService,
        supervisorAgent=supervisor_agent)
    

   