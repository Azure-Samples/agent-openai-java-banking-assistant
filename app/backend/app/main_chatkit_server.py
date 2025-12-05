from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.routers.chatkit import attachment_routers
from app.routers.chatkit import chat_routers
from app.config.settings import settings
from app.config.logging import get_logger, setup_logging
from agent_framework.observability import setup_observability
# Foundry based dependency injection container
#from app.config.container_foundry import Container

# Azure Chat based dependency injection container
from app.config.container_azure_chat import Container



def create_app() -> FastAPI:
    # Initialize logging for the app
    setup_logging()
    # Get logger for this module
    logger = get_logger(__name__)

    # Setup agent framework observability
    #commenting out for now due to incompatibility between agent-framework 1.0.0b251120 and opentelemetry-sdk 1.39
    #setup_observability(enable_sensitive_data=settings.ENABLE_OTEL,applicationinsights_connection_string=settings.APPLICATIONINSIGHTS_CONNECTION_STRING)

    logger.info(f"Creating FastAPI application: {settings.APP_NAME}")
    
    app = FastAPI(title=settings.APP_NAME)
   
    # Initialize dependency injection container
    container = Container()
    
    # Wire dependencies to modules that need them
    container.wire(modules=[chat_routers,attachment_routers])
    
    # Store container in app state for potential cleanup
    app.state.container = container

    # Use FastAPI lifespan for startup and shutdown events
    from contextlib import asynccontextmanager

    @asynccontextmanager
    async def lifespan(app: FastAPI):
        yield
        logger.info("Shutting down application...")
        container.unwire()

    app.router.lifespan_context = lifespan

    # Include routers
    app.include_router(chat_routers.router, tags=["chat"])
    app.include_router(attachment_routers.router, tags=["attachments"])



    logger.info("FastAPI application created successfully")
    app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # In production, specify exact origins
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)
    return app


app = create_app()
