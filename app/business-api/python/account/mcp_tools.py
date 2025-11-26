from fastmcp import FastMCP
import logging
from typing import Annotated
from services import AccountService, UserService, card_service_singleton

logger = logging.getLogger(__name__)
user_service = UserService()
account_service = AccountService()
mcp = FastMCP("Account MCP Server")

@mcp.tool(name="getAccountsByUserName", description="Get the list of all accounts for a specific user")
def get_accounts_by_user_name(userName: Annotated[str, "username of logged user"]):
    logger.info("getAccountsByUserName called with userName=%s", userName)
    return user_service.get_accounts_by_user_name(userName)

@mcp.tool(name="getAccountDetails", description="Get account details and available payment methods")
def get_account_details(accountId: Annotated[str, "Unique identifier for the user account"]):
    logger.info("Request to getAccountDetails with accountId: %s", accountId)
    return account_service.get_account_details(accountId)



# @mcp.tool(name="getPaymentMethodDetails", description="Get payment method detail with available balance")
# def get_payment_method_details(paymentMethodId: Annotated[str, "Unique identifier for the payment method"]):
#     logger.info("Request to getPaymentMethodDetails with paymentMethodId: %s", paymentMethodId)
#     return account_service.get_payment_method_details(paymentMethodId)


@mcp.tool(name="getRegisteredBeneficiary", description="Get list of registered beneficiaries for a specific account")
def get_registered_beneficiary(accountId: Annotated[str, "Unique identifier for the user account"]):
    logger.info("Request to getRegisteredBeneficiary with accountId: %s", accountId)
    return account_service.get_registered_beneficiary(accountId)


@mcp.tool(name="getCreditCards", description="Get the list of credit cards bound to an account")
def get_credit_cards(accountId: Annotated[str, "Unique identifier for the user account"]):
    logger.info("Request to getCreditCards with accountId: %s", accountId)
    return card_service_singleton.get_credit_cards(accountId)


@mcp.tool(name="getCardDetails", description="Get the details of a single credit card")
def get_card_details(cardId: Annotated[str, "Unique identifier for the card"]):
    logger.info("Request to getCardDetails with cardId: %s", cardId)
    return card_service_singleton.get_card_details(cardId)
