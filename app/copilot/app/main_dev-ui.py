from app.config.settings import settings
from app.config.logging import get_logger, setup_logging
from agent_framework.observability import setup_observability
from agent_framework.devui import serve
from app.agents.azure_chat.supervisor_agent import SupervisorAgent
from dependency_injector.wiring import Provide, inject

# Foundry based dependency injection container
#from app.config.container_foundry import Container

# Azure Chat based dependency injection container
from app.config.container_azure_chat import Container



@inject
def main(supervisor_agent: SupervisorAgent = Provide[Container.supervisor_agent]) -> None:
     # Initialize logging for the app
    setup_logging()
    # Get logger for this module
    logger = get_logger(__name__)

    
    # Setup agent framework observability
    setup_observability(enable_sensitive_data=settings.ENABLE_OTEL,applicationinsights_connection_string=settings.APPLICATIONINSIGHTS_CONNECTION_STRING)

    agent = supervisor_agent._build_af_agent()

    logger.info("Starting DevUI on http://localhost:8090")

        # Launch server with auto-generated entity IDs
    serve(entities=[agent], port=8090, auto_open=True)


if __name__ == "__main__":
     # Initialize dependency injection container
    container = Container()
    container.wire(modules=[__name__])
    main()
