from typing import List, Optional
from pydantic import BaseModel, Field


class PaymentMethodSummary(BaseModel):
    id: str
    type: str
    activationDate: Optional[str] = None
    expirationDate: Optional[str] = None


class PaymentMethod(BaseModel):
    id: str
    type: str
    activationDate: Optional[str] = None
    expirationDate: Optional[str] = None
    availableBalance: Optional[str] = None
    cardNumber: Optional[str] = None


class Beneficiary(BaseModel):
    id: str
    fullName: str
    bankCode: str
    bankName: str


class Account(BaseModel):
    id: str
    userName: str
    accountHolderFullName: str
    currency: str
    activationDate: Optional[str] = None
    balance: Optional[str] = None
    paymentMethods: Optional[List[PaymentMethodSummary]] = None
