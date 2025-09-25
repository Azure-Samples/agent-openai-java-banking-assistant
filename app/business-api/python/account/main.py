
import os
from fastmcp import FastMCP
from logging_config import configure_logging
from mcp_tools import mcp


if __name__ == "__main__":
    configure_logging()
    profile = os.environ.get("PROFILE", "prod")
    port = 8070 if profile == "dev" else 8080
    mcp.run(transport="http", port=port)
