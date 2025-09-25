from fastapi import FastAPI
from app.api import auth_routers, chat_routers, content_routers
from app.config.settings import settings
from app.config.logging import get_logger, setup_logging
from agent_framework.observability import setup_observability
# Foundry based dependency injection container
from app.config.container_foundry import Container
# Azure Chat based dependency injection container
#from app.config.container_azure_chat import Container



def create_app() -> FastAPI:
    # Initialize logging for the app
    setup_logging()
    # Get logger for this module
    logger = get_logger(__name__)

    # Setup agent framework observability
    setup_observability(enable_sensitive_data=True,applicationinsights_connection_string=settings.APPLICATIONINSIGHTS_CONNECTION_STRING)

    logger.info(f"Creating FastAPI application: {settings.APP_NAME}")
    
    app = FastAPI(title=settings.APP_NAME)
   
    # Initialize dependency injection container
    container = Container()
    
    # Wire dependencies to modules that need them
    container.wire(modules=[chat_routers,content_routers])
    
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
    app.include_router(auth_routers.router, prefix="/api", tags=["auth"])
    app.include_router(chat_routers.router, prefix="/api", tags=["chat"])
    app.include_router(content_routers.router, prefix="/api", tags=["content"])


    logger.info("FastAPI application created successfully")
    return app


app = create_app()
