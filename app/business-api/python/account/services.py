from typing import List, Optional
from models import Account, PaymentMethod, PaymentMethodSummary, Beneficiary, Card
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
                PaymentMethodSummary(id="55555", type="Visa", name="Primary Platinum", activationDate="2024-03-01", expirationDate="2027-03-01"),
                PaymentMethodSummary(id="66666", type="Visa", name="Secondary Gold", activationDate="2025-11-01", expirationDate="2028-11-01"),
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

        self.payment_methods["12345"] = PaymentMethod(id="12345", type="Visa", activationDate="2022-01-01", expirationDate="2025-01-01", availableBalance=500.0, cardNumber="1234567812345678")
        self.payment_methods["55555"] = PaymentMethod(id="55555", type="Visa", activationDate="2024-01-01", expirationDate="2028-01-01", availableBalance=350.0, cardNumber="637362551913266")
        self.payment_methods["23456"] = PaymentMethod(id="23456", type="BankTransfer", activationDate="2022-01-01", expirationDate="9999-01-01", availableBalance=5000.0, cardNumber=None)
        self.payment_methods["345678"] = PaymentMethod(id="345678", type="BankTransfer", activationDate="2022-01-01", expirationDate="9999-01-01", availableBalance=10000.0, cardNumber=None)

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


class CardService:
    def __init__(self):
        self.cards: dict[str, Card] = {}
        self.cards_by_account: dict[str, List[Card]] = {}
        self._load_sample_cards()

    # helpers
    def _load_sample_cards(self) -> None:
        sample_cards = [
            ("1000", Card(id="card-1020", type="credit", name="Alice Corporate Platinum", activationDate="2023-01-01", expirationDate="2028-01-01", balance=1200.0, number="4111222233334444", limit=15000.0, status="active")),
            ("1000", Card(id="card-1021", type="credit", name="Alice Corporate Gold", activationDate="2022-06-01", expirationDate="2027-06-01", balance=750.0, number="4111222233335555", limit=5000.0, status="active")),
            ("1010", Card(id="55555", type="credit", circuit="visa", name="Primary Platinum", activationDate="2024-03-01", expirationDate="2027-03-01", balance=900.5, number="5111222233335555", limit=3000.0, status="active")),
            ("1010", Card(id="66666", type="recharge", circuit="visa", name="Virtual Gold", activationDate="2025-11-01", expirationDate="2028-11-01", balance=640.25, rechargedAmount=1200, number="5211222233336666", limit=2500.0, status="active")),
            ("1010", Card(id="77777", type="credit", circuit="amex", name="Executive Black", activationDate="2024-02-01", expirationDate="2029-02-01", balance=0, number="5311222233337777", limit=20000.0, status="blocked")),
        ]
        for account_id, card in sample_cards:
            self.cards[card.id] = card
            self.cards_by_account.setdefault(account_id, []).append(card)

    def _validate_account_id(self, account_id: str) -> None:
        if not account_id:
            raise ValueError("AccountId is empty or null")
        if not account_id.isdigit():
            raise ValueError("AccountId is not a valid number")

    def _require_card(self, card_id: str) -> Card:
        if not card_id:
            raise ValueError("CardId is empty or null")
        card = self.cards.get(card_id)
        if card is None:
            raise RuntimeError(f"Card {card_id} not found")
        return card

    # public surface
    def get_credit_cards(self, account_id: str) -> List[Card]:
        logger.info("Request to get_credit_cards with account_id: %s", account_id)
        self._validate_account_id(account_id)
        return self.cards_by_account.get(account_id, [])

    def get_card_details(self, card_id: str) -> Optional[Card]:
        logger.info("Request to get_card_details for card_id=%s", card_id)
        if not card_id:
            raise ValueError("CardId is empty or null")
        return self.cards.get(card_id)

    def recharge_card(self, card_id: str, amount: float) -> Card:
        logger.info("Request to recharge_card card_id=%s amount=%.2f", card_id, amount)
        if amount <= 0:
            raise ValueError("Amount must be greater than zero")
        card = self._require_card(card_id)
        if ( card.type != "recharge" ):
            raise RuntimeError("Only recharge cards can be recharged")
        #check if rechargedAmount + amount is <= credit limit
        recharged_amount = card.rechargedAmount or 0.0
        if recharged_amount + amount > (card.limit or 0.0):
            raise RuntimeError("Recharge amount exceeds credit limit")
        card.rechargedAmount = round(recharged_amount + amount, 2)
        card.balance = round((card.balance or 0.0) + amount, 2)
        return card

    def pay_with_card(self, card_id: str, amount: float) -> Card:
        logger.info("Request to pay_with_card card_id=%s amount=%.2f", card_id, amount)
        if amount <= 0:
            raise ValueError("Amount must be greater than zero")
        card = self._require_card(card_id)
        balance = card.balance or 0.0
        if balance < amount:
            raise RuntimeError("Insufficient available balance")
        card.balance = round(balance - amount, 2)
        return card


# shared singleton (cards are stored in memory here)
card_service_singleton = CardService()
