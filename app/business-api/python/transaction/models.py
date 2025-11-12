from pydantic import BaseModel, Field
from typing import Optional


class Transaction(BaseModel):
    id: str
    description: Optional[str] = None
    # income/outcome
    type: Optional[str] = None
    recipientName: Optional[str] = None
    recipientBankReference: Optional[str] = None
    accountId: Optional[str] = None
    paymentType: Optional[str] = None
    amount: Optional[float] = None
    timestamp: Optional[str] = None
