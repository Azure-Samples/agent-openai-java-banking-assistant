import os
from typing import List
from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict


def get_env_files() -> List[str]:
    """Get list of environment files to load based on current environment."""
    env = os.getenv("PROFILE")

    if env:
        print(f"Loading environment files for environment: {env}")
    else:
        print("No environment specified, environment variables only configuration will be used.")
        return []
    
    env = env.lower()
    # List of env files to try (in order of priority - later files override earlier ones)
    env_files = [
        ".env",  # Base environment file
        f".env.{env}"  # Environment-specific file
        
    ]
    
    # Filter to only existing files
    return [f for f in env_files if os.path.exists(f)]    

class Settings(BaseSettings):
    """Application settings loaded from environment or environment-specific .env files.

    Settings are loaded in the following order (later sources override earlier ones):
    1. Default values defined in the class
    2. Environment variables
    3. Base .env file
    4. Environment-specific .env file (e.g., .env.development, .env.production)
    
    The environment is determined by the ENVIRONMENT environment variable or defaults to 'development'.
    """

    # app-level
    APP_NAME: str = "Copilot Multi Agent Chat API"
    PROFILE: str = Field(default="prod")

    #Logging and monitoring
    APPLICATIONINSIGHTS_CONNECTION_STRING: str | None = Field(default=None)
    ENABLE_OTEL : bool = Field(default=True)
  
    # Azure AI Foundry configuration
    # maps to environment variables described by the user

    AZURE_DOCUMENT_INTELLIGENCE_SERVICE: str | None = Field(default=None)
    FOUNDRY_PROJECT_ENDPOINT: str  = Field(default="",description="Azure AI Foundry Project Endpoint (required)", min_length=1)
    FOUNDRY_MODEL_DEPLOYMENT_NAME: str = Field(default="gpt-4o")
    AZURE_OPENAI_ENDPOINT: str | None = Field(default=None)
    AZURE_OPENAI_CHAT_DEPLOYMENT_NAME: str = Field(default="gpt-4o")

    # Azure services
    AZURE_STORAGE_ACCOUNT: str | None = Field(default=None)
    AZURE_STORAGE_CONTAINER: str | None = Field(default="content")

    #MCP servers
    ACCOUNT_MCP_URL: str | None= Field(default=None,description="MCP server URL (required)", min_length=1)
    TRANSACTION_MCP_URL: str | None= Field(default=None,description="MCP server URL (required)", min_length=1)
    PAYMENT_MCP_URL: str | None= Field(default=None,description="MCP server URL (required)", min_length=1)

    # Support for User Assigned Managed Identity: empty means system-managed
    AZURE_CLIENT_ID: str  | None = Field(default="system-managed-identity")

    model_config = SettingsConfigDict(
        env_file=get_env_files(),
        env_file_encoding="utf-8",
        extra="ignore",
    )


settings = Settings()