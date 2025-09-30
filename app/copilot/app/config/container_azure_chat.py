"""Dependency injection container configuration."""

import os
from dependency_injector import containers, providers
from azure.ai.projects import AIProjectClient
from azure.ai.documentintelligence import DocumentIntelligenceClient
from azure.storage.blob import BlobServiceClient
from app.helpers.blob_proxy import BlobStorageProxy
from app.helpers.document_intelligence_scanner import DocumentIntelligenceInvoiceScanHelper
from app.config.azure_credential import get_azure_credential, get_azure_credential_async
from app.config.settings import settings
#Azure Chat based agent dependencies
from app.agents.azure_chat.account_agent import AccountAgent
from app.agents.azure_chat.transaction_agent import TransactionHistoryAgent
from app.agents.azure_chat.payment_agent import PaymentAgent
from app.agents.azure_chat.supervisor_agent import SupervisorAgent


from agent_framework.azure import AzureOpenAIChatClient
from agent_framework import MCPStreamableHTTPTool




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
    

    # Azure Chat based agents
    _azure_chat_client = providers.Singleton(
        AzureOpenAIChatClient,
        credential=providers.Factory(get_azure_credential), 
        endpoint=settings.AZURE_OPENAI_ENDPOINT,deployment_name=settings.AZURE_OPENAI_CHAT_DEPLOYMENT_NAME
    )

    #Account Agent with Azure chat based agents. Can be singleton as thread state is passed to the underlying agent run method
    account_agent = providers.Singleton(
    AccountAgent,
    azure_chat_client=_azure_chat_client,
    account_mcp_server_url=f"{settings.ACCOUNT_MCP_URL}/mcp"
    )

    transaction_agent = providers.Singleton(
    TransactionHistoryAgent,
    azure_chat_client=_azure_chat_client,
    account_mcp_server_url=f"{settings.ACCOUNT_MCP_URL}/mcp",
    transaction_mcp_server_url=f"{settings.TRANSACTION_MCP_URL}/mcp"
    )

    payment_agent = providers.Singleton(
    PaymentAgent,
    azure_chat_client=_azure_chat_client,
    account_mcp_server_url=f"{settings.ACCOUNT_MCP_URL}/mcp",
    transaction_mcp_server_url=f"{settings.TRANSACTION_MCP_URL}/mcp",
    payment_mcp_server_url=f"{settings.PAYMENT_MCP_URL}/mcp",
    document_scanner_helper=document_intelligence_scanner
    )

    #Supervisor Agent with Azure chat based agents. A per request instance is created as it holds the thread state
    supervisor_agent = providers.Factory(
        SupervisorAgent,
        azure_chat_client=_azure_chat_client,
        account_agent=account_agent,
        transaction_agent=transaction_agent,
        payment_agent=payment_agent
    )


   