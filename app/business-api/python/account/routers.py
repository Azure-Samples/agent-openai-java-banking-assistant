from fastapi import APIRouter, HTTPException, status
from pydantic import BaseModel, Field
from typing import List
import logging

from models import Card
from services import card_service_singleton

logger = logging.getLogger(__name__)
router = APIRouter()


class CardAmountRequest(BaseModel):
    amount: float = Field(..., gt=0, description="Amount for the requested card operation")


def _to_runtime_http_error(err: RuntimeError) -> HTTPException:
    message = str(err)
    if "not found" in message.lower():
        code = status.HTTP_404_NOT_FOUND
    else:
        code = status.HTTP_400_BAD_REQUEST
    return HTTPException(status_code=code, detail=message)


@router.get("/accounts/{account_id}/cards", response_model=List[Card])
def list_credit_cards(account_id: str):
    """Return all credit cards for a given account."""
    logger.info("List credit cards for account_id=%s", account_id)
    try:
        return card_service_singleton.get_credit_cards(account_id)
    except ValueError as ve:
        logger.exception("Validation error while listing cards")
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(ve))
    except Exception:
        logger.exception("Unexpected error while listing cards")
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail="Internal server error")


@router.get("/cards/{card_id}", response_model=Card)
def get_card_details(card_id: str):
    """Return the card details for a single identifier."""
    logger.info("Get card details for card_id=%s", card_id)
    try:
        card = card_service_singleton.get_card_details(card_id)
        if card is None:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Card not found")
        return card
    except ValueError as ve:
        logger.exception("Validation error while retrieving card detail")
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(ve))


@router.post("/cards/{card_id}/recharge", response_model=Card)
def recharge_card(card_id: str, request: CardAmountRequest):
    """Recharge the selected card.

    The request amount must be positive."""
    logger.info("Recharge card card_id=%s amount=%.2f", card_id, request.amount)
    try:
        return card_service_singleton.recharge_card(card_id, request.amount)
    except ValueError as ve:
        logger.exception("Validation error during recharge")
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(ve))
    except RuntimeError as re:
        logger.exception("Runtime error during recharge")
        raise _to_runtime_http_error(re)
    except Exception:
        logger.exception("Unexpected error during recharge")
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail="Internal server error")


@router.post("/cards/{card_id}/pay", response_model=Card)
def pay_with_card(card_id: str, request: CardAmountRequest):
    """Record a payment and debit the available balance."""
    logger.info("Pay with card card_id=%s amount=%.2f", card_id, request.amount)
    try:
        return card_service_singleton.pay_with_card(card_id, request.amount)
    except ValueError as ve:
        logger.exception("Validation error during payment")
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(ve))
    except RuntimeError as re:
        logger.exception("Runtime error during payment")
        raise _to_runtime_http_error(re)
    except Exception:
        logger.exception("Unexpected error during payment")
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail="Internal server error")
