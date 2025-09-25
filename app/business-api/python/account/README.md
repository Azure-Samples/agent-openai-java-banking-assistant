Python MCP server for account service using FastMCP

Install and sync dependencies using uv (repo uses uv for dependency management):

1) Create a venv and sync dependencies

    uv venv
    uv sync

2) Run the MCP server locally

    python main.py

The MCP server exposes the following tools (registered via @mcp.tool decorator):

- get_account_details(accountId: str) -> Account | None
- get_payment_method_details(paymentMethodId: str) -> PaymentMethod | None
- get_registered_beneficiary(accountId: str) -> list[Beneficiary]
- get_accounts_by_user_name(userName: str) -> list[Account]

Logging uses the standard Python logging module. When running locally you can enable INFO logs with:

    python -c "import logging; logging.basicConfig(level=logging.INFO)"
