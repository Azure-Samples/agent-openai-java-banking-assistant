Payment MCP Server (Python)

This module is a FastMCP 2.x based MCP server converted from the Java Spring MCP server.

Quick start

- Create a venv and install dependencies using uv:

  uv venv
  # activate the venv (platform dependent)
  . .venv/Scripts/Activate.ps1
  uv sync --active

- Run the server:

  python main.py

Environment variables
- PROFILE: set to `dev` to run on port 8071, otherwise 8081
- TRANSACTIONS_API_URL: URL for the transactions API (defaults to http://localhost:8060)
