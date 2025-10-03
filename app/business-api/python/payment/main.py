import logging
import os
from logging_config import configure_logging
from mcp_tools import mcp

logger = logging.getLogger(__name__)

if __name__ == "__main__":
    configure_logging()
    profile = os.environ.get("PROFILE", "prod")
    port = 8072 if profile == "dev" else 8080
    logger.info(f"Starting payment service server with profile: {profile}, port: {port}")
    mcp.run(transport="http", port=port, host="0.0.0.0")
