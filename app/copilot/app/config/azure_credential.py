import os
from azure.identity import ManagedIdentityCredential, AzureCliCredential
from azure.identity.aio import ManagedIdentityCredential as AioManagedIdentityCredential, AzureCliCredential as AioCliCredential
from app.config.settings import settings

async def get_azure_credential_async():
    """
    Returns an Azure credential asynchronously based on the application environment.

    If the environment is 'dev', it uses AioDefaultAzureCredential.
    Otherwise, it uses AioManagedIdentityCredential.

    Args:
        client_id (str, optional): The client ID for the Managed Identity Credential.

    Returns:
        Credential object: Either AioDefaultAzureCredential or AioManagedIdentityCredential.
    """
    if settings.PROFILE == 'dev':
        return AioCliCredential()  # CodeQL [SM05139] Okay use of DefaultAzureCredential as it is only used in development
    else:
        return AioManagedIdentityCredential(client_id=settings.AZURE_CLIENT_ID)


def get_azure_credential():
    """
    Returns an Azure credential based on the application environment.

    If the environment is 'dev', it uses DefaultAzureCredential.
    Otherwise, it uses ManagedIdentityCredential.

    Args:
        client_id (str, optional): The client ID for the Managed Identity Credential.

    Returns:
        Credential object: Either DefaultAzureCredential or ManagedIdentityCredential.
    """
    if settings.PROFILE == 'dev':
        return AzureCliCredential()  # CodeQL [SM05139] Okay use of DefaultAzureCredential as it is only used in development
    else:
        return ManagedIdentityCredential(client_id=settings.AZURE_CLIENT_ID)
