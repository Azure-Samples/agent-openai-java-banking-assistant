from typing import List, Optional
from models import Account, PaymentMethod, PaymentMethodSummary, Beneficiary
import logging

logger = logging.getLogger(__name__)


class AccountService:
    def __init__(self):
        self.accounts = {}
        self.payment_methods = {}
        # populate dummy data similar to Java version
        self.accounts["1000"] = Account(
            id="1000",
            userName="alice.user@contoso.com",
            accountHolderFullName="Alice User",
            currency="USD",
            activationDate="2022-01-01",
            balance="5000",
            paymentMethods=[
                PaymentMethodSummary(id="12345", type="Visa", activationDate="2022-01-01", expirationDate="2025-01-01"),
                PaymentMethodSummary(id="23456", type="BankTransfer", activationDate="2022-01-01", expirationDate="9999-01-01"),
            ],
        )

        self.accounts["1010"] = Account(
            id="1010",
            userName="bob.user@contoso.com",
            accountHolderFullName="Bob User",
            currency="EUR",
            activationDate="2022-01-01",
            balance="10000",
            paymentMethods=[
                PaymentMethodSummary(id="345678", type="BankTransfer", activationDate="2022-01-01", expirationDate="9999-01-01"),
                PaymentMethodSummary(id="55555", type="Visa", activationDate="2022-01-01", expirationDate="2026-01-01"),
            ],
        )

        self.accounts["1020"] = Account(
            id="1020",
            userName="charlie.user@contoso.com",
            accountHolderFullName="Charlie User",
            currency="EUR",
            activationDate="2022-01-01",
            balance="3000",
            paymentMethods=[
                PaymentMethodSummary(id="46748576", type="DirectDebit", activationDate="2022-02-01", expirationDate="9999-02-01"),
            ],
        )

        self.payment_methods["12345"] = PaymentMethod(id="12345", type="Visa", activationDate="2022-01-01", expirationDate="2025-01-01", availableBalance="500.00", cardNumber="1234567812345678")
        self.payment_methods["55555"] = PaymentMethod(id="55555", type="Visa", activationDate="2024-01-01", expirationDate="2028-01-01", availableBalance="350.00", cardNumber="637362551913266")
        self.payment_methods["23456"] = PaymentMethod(id="23456", type="BankTransfer", activationDate="2022-01-01", expirationDate="9999-01-01", availableBalance="5000.00", cardNumber=None)
        self.payment_methods["345678"] = PaymentMethod(id="345678", type="BankTransfer", activationDate="2022-01-01", expirationDate="9999-01-01", availableBalance="10000.00", cardNumber=None)

    def get_account_details(self, account_id: str) -> Optional[Account]:
        logger.info("Request to get_account_details with account_id: %s", account_id)
        if not account_id:
            raise ValueError("AccountId is empty or null")
        if not account_id.isdigit():
            raise ValueError("AccountId is not a valid number")
        return self.accounts.get(account_id)

    def get_payment_method_details(self, payment_method_id: str) -> Optional[PaymentMethod]:
        logger.info("Request to get_payment_method_details with payment_method_id: %s", payment_method_id)
        if not payment_method_id:
            raise ValueError("PaymentMethodId is empty or null")
        if not payment_method_id.isdigit():
            raise ValueError("PaymentMethodId is not a valid number")
        return self.payment_methods.get(payment_method_id)

    def get_registered_beneficiary(self, account_id: str) -> List[Beneficiary]:
        logger.info("Request to get_registered_beneficiary with account_id: %s", account_id)
        if not account_id:
            raise ValueError("AccountId is empty or null")
        if not account_id.isdigit():
            raise ValueError("AccountId is not a valid number")
        return [
            Beneficiary(id="1", fullName="Mike ThePlumber", bankCode="123456789", bankName="Intesa Sanpaolo"),
            Beneficiary(id="2", fullName="Jane TheElectrician", bankCode="987654321", bankName="UBS"),
        ]


class UserService:
    def __init__(self):
        self.accounts = {}
        self.accounts["alice.user@contoso.com"] = Account(id="1000", userName="alice.user@contoso.com", accountHolderFullName="Alice User", currency="USD", activationDate="2022-01-01", balance="5000", paymentMethods=None)
        self.accounts["bob.user@contoso.com"] = Account(id="1010", userName="bob.user@contoso.com", accountHolderFullName="Bob User", currency="EUR", activationDate="2022-01-01", balance="10000", paymentMethods=None)
        self.accounts["charlie.user@contoso.com"] = Account(id="1020", userName="charlie.user@contoso.com", accountHolderFullName="Charlie User", currency="EUR", activationDate="2022-01-01", balance="3000", paymentMethods=None)

    def get_accounts_by_user_name(self, user_name: str) -> List[Account]:
        # Return list with account if found, otherwise an empty list
        acc = self.accounts.get(user_name)
        return [acc] if acc is not None else []
