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
# Azure Chat based agent dependencies
#from app.agents.account_agent import AccountAgent
#from app.agents.supervisor_agent import SupervisorAgent
# Azure AI Foundry based agent dependencies
from app.agents.foundry.account_agent_foundry import AccountAgent
from app.agents.foundry.transaction_agent_foundry import TransactionHistoryAgent
from app.agents.foundry.payment_agent_foundry import PaymentAgent
from app.agents.foundry.supervisor_agent_foundry import SupervisorAgent
from agent_framework.azure import AzureChatClient
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
    

     

    #MCP plugin need to be initialized using an async __aenter__ method and disposed using __aexit__
    #We will use it as a singleton for now, and let the agent to call __aenter__ when building the agent.
    # TO_DO Need to understand when to call __aexit__ for each plugin
    # account_mcp_server = MCPStreamableHTTPTool(
    #     name="Account MCP server client",
    #     url=f"{settings.ACCOUNT_MCP_URL}/mcp"
    # )

    # transaction_mcp_server = MCPStreamableHTTPTool(
    #     name="Transaction MCP server client",
    #     url=f"{settings.TRANSACTION_MCP_URL}/mcp"
    # )

    # payment_mcp_server = MCPStreamableHTTPTool(
    #     name="Payment MCP server client",
    #     url=f"{settings.PAYMENT_MCP_URL}/mcp"
    # )
    
    #Azure Agent Service based agents

    # Foundry Agent Creation
    _foundry_project_client = AIProjectClient(settings.FOUNDRY_PROJECT_ENDPOINT, credential=get_azure_credential(), logging_enable=True)
    
    # Account Agent with Azure AI Foundry
    _foundry_account_agent = providers.Singleton(
        AccountAgent,
        foundry_project_client=_foundry_project_client,
        chat_deployment_name=settings.FOUNDRY_MODEL_DEPLOYMENT_NAME,
        account_mcp_server_url=f"{settings.ACCOUNT_MCP_URL}/mcp",
        foundry_endpoint=settings.FOUNDRY_PROJECT_ENDPOINT
    )

    # Transaction History Agent with Azure AI Foundry
    _foundry_transaction_history_agent = providers.Singleton(
        TransactionHistoryAgent,
        foundry_project_client=_foundry_project_client,
        chat_deployment_name=settings.FOUNDRY_MODEL_DEPLOYMENT_NAME,
        account_mcp_server_url=f"{settings.ACCOUNT_MCP_URL}/mcp",
        transaction_mcp_server_url=f"{settings.TRANSACTION_MCP_URL}/mcp",
        foundry_endpoint=settings.FOUNDRY_PROJECT_ENDPOINT
    )

    # Payment Agent with Azure AI Foundry
    _foundry_payment_agent = providers.Singleton(
        PaymentAgent,
        foundry_project_client=_foundry_project_client,
        chat_deployment_name=settings.FOUNDRY_MODEL_DEPLOYMENT_NAME,
        account_mcp_server_url=f"{settings.ACCOUNT_MCP_URL}/mcp",
        transaction_mcp_server_url=f"{settings.TRANSACTION_MCP_URL}/mcp",
        payment_mcp_server_url=f"{settings.PAYMENT_MCP_URL}/mcp",
        document_scanner_helper=document_intelligence_scanner,
        foundry_endpoint=settings.FOUNDRY_PROJECT_ENDPOINT
    )

    # Creating the native foundry supervisor agent to get the agent id
    _foundry_supervisor_native_agent = _foundry_project_client.agents.create_agent(
            model=settings.FOUNDRY_MODEL_DEPLOYMENT_NAME, name=SupervisorAgent.name, description=SupervisorAgent.description
    )

     # Supervisor Agent Azure AI to be used in agents-as-tool orchestration
    supervisor_agent = providers.Factory(
        SupervisorAgent,
        foundry_project_client=_foundry_project_client,
        chat_deployment_name=settings.FOUNDRY_MODEL_DEPLOYMENT_NAME,
        account_agent=_foundry_account_agent,
        transaction_agent=_foundry_transaction_history_agent,
        payment_agent=_foundry_payment_agent,
        foundry_endpoint=settings.FOUNDRY_PROJECT_ENDPOINT,
        agent_id=_foundry_supervisor_native_agent.id
    )

    #####################################################################################################

    # # Azure Chat based agents
    # _azure_chat_client = providers.Singleton(
    #     AzureChatClient,
    #     credential=providers.Factory(get_azure_credential), 
    #     endpoint=settings.AZURE_OPENAI_ENDPOINT,deployment_name=settings.AZURE_OPENAI_CHAT_DEPLOYMENT_NAME
    # )

    # #Account Agent with Azure chat based agents
    # account_agent = providers.Singleton(
    # AccountAgent,
    # azure_chat_client=_azure_chat_client,
    # account_mcp_server_url=f"{settings.ACCOUNT_MCP_URL}/mcp"
    # )

    
    # #Supervisor Agent with Azure chat based agents
    # supervisor_agent = providers.Singleton(
    #     SupervisorAgent,
    #     account_agent=account_agent
    # )

    # Transaction History Agent
    # transaction_history_agent = providers.Singleton(
    #     TransactionHistoryAgent,
    #     chatCompletionService=azure_chat_completion,
    #     account_mcp_plugin=account_mcp_plugin,
    #     transaction_mcp_plugin=transaction_mcp_plugin)
    
    # #Payment Agent
    # payment_agent = providers.Singleton(
    #     PaymentAgent,
    #     chatCompletionService=azure_chat_completion,
    #     account_mcp_plugin=account_mcp_plugin,
    #     transaction_mcp_plugin=transaction_mcp_plugin,
    #     payment_mcp_plugin=payment_mcp_plugin,
    #     scan_invoice_helper=document_intelligence_scanner)

    # # Triage Agent to be used in handoff orchestration
    # triage_agent = providers.Singleton(
    #     TriageAgent,
    #     chatCompletionService=azure_chat_completion
    # )

   



    # supervisor_orchestration_service = providers.Factory(
    #     SupervisorOrchestrationService,
    #     supervisorAgent=supervisor_agent)
    

   