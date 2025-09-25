from pydantic import BaseModel, Field
from typing import Optional


class Transaction(BaseModel):
    id: str
    description: Optional[str] = None
    type: Optional[str] = Field(None, description="income/outcome")
    recipientName: Optional[str] = None
    recipientBankReference: Optional[str] = None
    accountId: Optional[str] = None
    paymentType: Optional[str] = None
    amount: Optional[float] = None
    timestamp: Optional[str] = None


class Payment(BaseModel):
    description: Optional[str] = None
    recipientName: Optional[str] = None
    recipientBankCode: Optional[str] = None
    accountId: Optional[str] = None
    paymentMethodId: Optional[str] = None
    paymentType: Optional[str] = None
    amount: Optional[float] = None
    timestamp: Optional[str] = None
