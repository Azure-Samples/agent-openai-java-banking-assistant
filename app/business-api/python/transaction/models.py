from pydantic import BaseModel, Field
from typing import Optional


class Transaction(BaseModel):
    id: str
    description: Optional[str] = None
    #deposits, withdrawals, transfers, and payments
    type: Optional[str] = None
    # income/outcome
    flowType: Optional[str] = None
    recipientName: Optional[str] = None
    recipientBankReference: Optional[str] = None
    accountId: Optional[str] = None
    #BankTransfer,DirectDebit,CreditCard
    paymentType: Optional[str] = None
    amount: Optional[float] = None
    timestamp: Optional[str] = None
    cardId: Optional[str] = None
    category: Optional[str] = None
    #paid, pending, failed
    status: Optional[str] = None

