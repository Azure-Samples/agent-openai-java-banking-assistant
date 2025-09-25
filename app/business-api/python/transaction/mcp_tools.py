from fastmcp import FastMCP
import logging
from services import TransactionService

logger = logging.getLogger(__name__)
service = TransactionService()
mcp = FastMCP("Transaction MCP Server")


@mcp.tool(name="getTransactionsByRecipientName", description="Get transactions by recipient name")
def get_transactions_by_recipient_name(accountId: str, recipientName: str):
    logger.info("getTransactionsByRecipientName called with accountId=%s, recipientName=%s", accountId, recipientName)
    return service.get_transactions_by_recipient_name(accountId, recipientName)


@mcp.tool(name="getLastTransactions", description="Get the last transactions for an account")
def get_last_transactions(accountId: str):
    logger.info("getLastTransactions called with accountId=%s", accountId)
    return service.get_last_transactions(accountId)


