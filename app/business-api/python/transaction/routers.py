from fastapi import APIRouter, HTTPException, Query, status
import logging
from typing import Optional

from models import Transaction
from services import transaction_service_singleton as service

logger = logging.getLogger(__name__)

router = APIRouter()


@router.get("/{account_id}")
def get_transactions(account_id: str, payment_type: Optional[str] = Query(None), transaction_type: Optional[str] = Query(None), card_id: Optional[str] = Query(None)):
    """Get transactions for an account. Optionally filter by payment type.
    """
    logger.info("Received request to get transactions for accountid[%s], payment_type=%s, transaction_type=%s, card_id=%s", account_id, payment_type, transaction_type, card_id)
    try:
        if payment_type or transaction_type:
            transactions = service.get_transactions_by_type(account_id,payment_type,transaction_type,card_id)
        else:
            transactions = service.get_transactions(account_id)
        return transactions
    except ValueError as ve:
        logger.exception("Validation error while getting transactions")
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(ve))
    except Exception:
        logger.exception("Unexpected error while getting transactions")
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail="Internal error")


@router.post("/{account_id}", status_code=status.HTTP_204_NO_CONTENT)
def notify_transaction(account_id: str, transaction: Transaction):
    """Notify a new transaction for an account.
    """
    logger.info("Received request to notify transaction for accountid[%s]. %s", account_id, transaction.json())
    try:
        service.notify_transaction(account_id, transaction)
    except ValueError as ve:
        logger.exception("Validation error while notifying transaction")
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(ve))
    except RuntimeError as re:
        logger.exception("Runtime error while notifying transaction")
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=str(re))
    except Exception:
        logger.exception("Unexpected error while notifying transaction")
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail="Internal error")
