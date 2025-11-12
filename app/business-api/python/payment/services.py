import logging
import os
from typing import Optional
import uuid
import requests

from models import Payment, Transaction

logger = logging.getLogger(__name__)


class PaymentService:
    def __init__(self, transaction_api_url: Optional[str] = None):
        # Prefer explicit constructor param. If not provided, read from env.
        env_url = os.environ.get("TRANSACTIONS_API_SERVER_URL")
        if transaction_api_url:
            self.transaction_api_url = transaction_api_url
        elif env_url:
            self.transaction_api_url = env_url
        else:
            # Defensive: fail fast if the transaction API URL isn't configured.
            raise ValueError(
                "TRANSACTIONS_API_SERVER_URL is not configured. Provide `transaction_api_url` to PaymentService or set the TRANSACTIONS_API_URL environment variable."
            )

    def process_payment(self, payment: Payment):
        # validations similar to Java implementation
        if not payment.accountId:
            raise ValueError("AccountId is empty or null")
        if not payment.accountId.isdigit():
            raise ValueError("AccountId is not a valid number")

        if (payment.paymentType or "").lower() != "transfer" and (not payment.paymentMethodId):
            raise ValueError("paymentMethodId is empty or null")

        if payment.paymentMethodId and not payment.paymentMethodId.isdigit():
            raise ValueError("paymentMethodId is not a valid number")

        # Pydantic v2: `json()` is deprecated. Use `model_dump_json()` instead.
        logger.info("Payment successful for: %s", payment.model_dump_json())

        transaction = self._convert_payment_to_transaction(payment)

        logger.info("Notifying payment [%s] for account[%s]..", payment.description, transaction.accountId)

        try:
            url = f"{self.transaction_api_url}/api/transactions/{payment.accountId}"
            # prefer sending native dict for requests body, use model_dump for Pydantic v2
            resp = requests.post(url, json=transaction.model_dump())
            resp.raise_for_status()
            logger.info("Transaction notified for: %s", transaction.model_dump_json())
        except Exception as ex:
            logger.exception("Failed to notify transaction: %s", ex)

    def _convert_payment_to_transaction(self, payment: Payment) -> Transaction:
        return Transaction(
            id=str(uuid.uuid4()),
            description=payment.description,
            type="outcome",
            recipientName=payment.recipientName,
            recipientBankReference=payment.recipientBankCode,
            accountId=payment.accountId,
            paymentType=payment.paymentType,
            amount=payment.amount,
            timestamp=payment.timestamp,
        )
