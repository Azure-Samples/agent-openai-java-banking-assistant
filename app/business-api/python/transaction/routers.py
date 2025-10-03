from fastapi import APIRouter, HTTPException, status
import logging

from models import Transaction
from services import transaction_service_singleton as service

logger = logging.getLogger(__name__)

router = APIRouter()


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
