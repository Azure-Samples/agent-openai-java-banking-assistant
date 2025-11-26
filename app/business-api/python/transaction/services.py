from typing import List, Optional
from models import Transaction
import logging
from datetime import datetime

logger = logging.getLogger(__name__)


class TransactionService:
    def __init__(self):
        self.last_transactions = {}
        self.all_transactions = {}

        # populate sample data for account 1010 similar to Java
        self.last_transactions["1010"] = [
            Transaction(
            id="11",
            description="Home power bill 334398",
            type="payment",
            flowType="outcome",
            recipientName="ACME",
            recipientBankReference="0001",
            accountId="1010",
            paymentType="BankTransfer",
            amount=160.40,
            timestamp=datetime.now().isoformat() + "Z",
            category="Utilities",
            status="pending"
            ),
            Transaction(
            id="22",
            description="Payment for office supply services",
            type="payment",
            flowType="outcome",
            recipientName="Contoso Services",
            recipientBankReference="0002",
            accountId="1010",
            paymentType="CreditCard",
            cardId="card-8421",
            amount=215.00,
            timestamp="2025-03-02T12:00:00Z",
            category="Supply services",
            status="paid"
            ),
            Transaction(
            id="33",
            description="Business Lunch with customer",
            type="payment",
            flowType="outcome",
            recipientName="Duff",
            accountId="1010",
            paymentType="CreditCard",
            cardId="card-8421",
            amount=134.10,
            timestamp="2025-10-03T12:00:00Z",
            category="Meals",
            status="paid"
            ),
            Transaction(
            id="43",
            description="card withdrawal at atm 00987",
            type="withdrawal",
            flowType="outcome",
            accountId="1010",
            paymentType="DirectDebit",
            cardId="card-3311",
            amount=150.00,
            timestamp="2025-8-04T12:00:00Z",
            category="Insurance",
            ),
            Transaction(
            id="53",
            description="Refund for invoice 19dee",
            type="deposit",
            flowType="income",
            recipientName="oscorp",
            recipientBankReference="0005",
            accountId="1010",
            paymentType="BankTransfer",
            amount=522.00,
            timestamp="2025-4-05T12:00:00Z",
            category="Refunds",
            cardId="card-0098",
            )
        ]

        self.all_transactions["1010"] = [
            Transaction(
            id="373737",
            description="Home power bill 334398",
            type="payment",
            flowType="outcome",
            recipientName="ACME",
            recipientBankReference="0001",
            accountId="1010",
            paymentType="BankTransfer",
            amount=160.40,
            timestamp=datetime.now().isoformat() + "Z",
            category="Utilities",
            status="pending"
            ),
            Transaction(
            id="232334",
            description="Payment for office supply services",
            type="payment",
            flowType="outcome",
            recipientName="Contoso",
            recipientBankReference="0002",
            accountId="1010",
            paymentType="CreditCard",
            cardId="55555",
            amount=215.00,
            timestamp="2025-03-02T12:00:00Z", 
            category="Supply services",
            status="paid"
            ),
            Transaction(
            id="3321432",
            description="Business Lunch with customer",
            type="payment",
            flowType="outcome",
            recipientName="Duff",
            accountId="1010",
            paymentType="CreditCard",
            cardId="66666",
            amount=134.10,
            timestamp="2025-10-03T12:00:00Z",
            category="Meals",
            status="paid"
            ),
            Transaction(
            id="99584",
            description="Card withdrawal at atm 00987",
            type="withdrawal",
            flowType="outcome",
            accountId="1010",
            paymentType="DirectDebit",
            amount=150.00,
            cardId="card-3311",
            timestamp="2025-8-04T12:00:00Z",
            category="Insurance",
            ),
            Transaction(
            id="99477",
            description="Refund for invoice 19dee",
            type="deposit",
            flowType="income",
            recipientName="oscorp",
            recipientBankReference="0005",
            accountId="1010",
            paymentType="BankTransfer",
            amount=522.00,
            timestamp="2025-4-05T12:00:00Z",
            category="Refunds",
            cardId="card-0098",
            ),
            Transaction(
                id="388373",
                description="Gas supply invoice 173645AB435 ",
                type="payment",
                flowType="outcome",
                recipientName="ACME",
                recipientBankReference="A012TABTYT156!",
                accountId="1010",
                paymentType="BankTransfer",
                amount=100.00,
                timestamp=datetime.now().isoformat() + "Z",
                category="Utilities",
                status="pending"
            ),
            Transaction(
                id="337733",
                description="Plumbing and other services. Bill 998877",
                type="payment",
                flowType="outcome",
                recipientName="ACME",
                recipientBankReference="0002",
                accountId="1010",
                paymentType="BankTransfer",
                amount=323.00,
                timestamp=datetime.now().isoformat() + "Z",
                category="Subscriptions",
                status="pending"
            ),
            Transaction(
                id="884995",
                description="Office Air conditioners. Invoice 355TRA1423FFSSS",
                type="payment",
                flowType="outcome",
                recipientName="Contoso Services",
                recipientBankReference="0003",
                accountId="1010",
                paymentType="DirectDebit",
                amount=300.00,
                timestamp="2025-10-03T12:00:00Z",
                category="Services",
                status="paid"
            
            ),
            Transaction(
                id="304984",
                description="Insurance monthly payment. Ref:12365GSHT",
                type="transfer",
                flowType="outcome",
                recipientName="ACME",
                recipientBankReference="0004",
                accountId="1010",
                paymentType="Transfer",
                amount=320.00,
                timestamp="2025-8-04T12:00:00Z",
                category="Savings",
            ),
            Transaction(
                id="3946373",
                description="Metro and Bus subscription 2023-AB56",
                type="payment",
                flowType="outcome",
                recipientName="Speedy Subways",
                recipientBankReference="0005",
                accountId="1010",
                paymentType="CreditCard",
                cardId="66666",
                amount=410.00,
                timestamp="2025-04-05T12:00:00Z",
                category="Retail",
                status="paid"
            ),

            Transaction(
                id="2004764",
                description="Medical eyes checkup payment. Ref: MZ23-5567",
                type="payment",
                flowType="outcome",
                recipientName="Contoso Health",
                recipientBankReference="0001",
                accountId="1010",
                paymentType="CreditCard",
                cardId="66666",
                amount=230.00,
                timestamp="2025-11-01T12:00:00Z",
                category="Health",
                status="paid"
            ),
            Transaction(
                id="49950598",
                description="Payment of the bill 682222",
                type="payment",
                flowType="outcome",
                recipientName="Contoso Services",
                recipientBankReference="0002",
                accountId="1010",
                paymentType="CreditCard",
                cardId="55555",
                amount=200.00,
                timestamp="2025-11-02T12:00:00Z",
                category="Rent",
                status="paid"
            ),
            Transaction(
                id="488624",
                description="Monthly Salary - StartUp.com",
                type="deposit",
                flowType="income",
                accountId="1010",
                paymentType="Transfer",
                amount=3000.00,
                timestamp="2025-10-03T12:00:00Z",
                category="Payroll",
            ),
            Transaction(
                id="3004853",
                description="Stocks vesting accreditation. www.traderepublic.com - FY25Q3",
                type="deposit",
                flowType="income",
                accountId="1010",
                paymentType="Transfer",
                amount=400.00,
                timestamp="2025-8-04T12:00:00Z",
                category="Investment",
            ),
            Transaction(
                id="3994853",
                description="Withdrawal at ATM 345516",
                type="withdrawal",
                flowType="outcome",
                accountId="1010",
                paymentType="Transfer",
                cardId="card-3311",
                amount=500.00,
                timestamp="2025-11-05T12:00:00Z",
                category="Education",
            ),
        ]

    def get_transactions_by_recipient_name(self, account_id: str, name: str) -> List[Transaction]:
        logger.info("get_transactions_by_recipient_name called with account_id=%s, name=%s", account_id, name)
        if not account_id:
            raise ValueError("AccountId is empty or null")
        if not account_id.isdigit():
            raise ValueError("AccountId is not a valid number")
        transactions = self.all_transactions.get(account_id)
        if transactions is None:
            return []
        name_lower = name.lower() if name else ""
        filtered = [t for t in transactions if t.recipientName and name_lower in t.recipientName.lower()]
        return sorted(filtered, key=lambda t: t.timestamp, reverse=True)

    def get_transactions(self, account_id: str) -> List[Transaction]:
        logger.info("get_last_transactions called with account_id=%s", account_id)
        if not account_id:
            raise ValueError("AccountId is empty or null")
        if not account_id.isdigit():
            raise ValueError("AccountId is not a valid number")
        transactions = self.last_transactions.get(account_id)
        if not transactions:
            return []
        return sorted(transactions, key=lambda t: t.timestamp, reverse=True)

    def get_transactions_by_type(
        self,
        account_id: str ,
        payment_type: Optional[str] = None,
        transaction_type: Optional[str] = None,
        card_id: Optional[str] = None,
    ) -> List[Transaction]:
        logger.info(
            "get_transactions_by_payment_type called with account_id=%s, payment_type=%s, transaction_type=%s, card_id=%s",
            account_id,
            payment_type,
            transaction_type,
            card_id,
        )
        if not account_id:
            raise ValueError("AccountId is empty or null")
        if not account_id.isdigit():
            raise ValueError("AccountId is not a valid number")
        transactions = self.all_transactions.get(account_id)
        if transactions is None:
            return []
        filtered = transactions
        if transaction_type:
            filtered = [t for t in filtered if t.type == transaction_type]
        if payment_type:
            filtered = [t for t in filtered if t.paymentType == payment_type]
        if card_id:
            filtered = [t for t in filtered if t.cardId == card_id]
        return sorted(filtered, key=lambda t: t.timestamp, reverse=True)

    def notify_transaction(self, account_id: str, transaction: Transaction) -> None:
        logger.info("notify_transaction called with account_id=%s, transaction=%s", account_id, transaction)
        if not account_id:
            raise ValueError("AccountId is empty or null")
        if not account_id.isdigit():
            raise ValueError("AccountId is not a valid number")
        all_list = self.all_transactions.get(account_id)
        if all_list is None:
            raise RuntimeError(f"Cannot find all transactions for account id: {account_id}")
        all_list.append(transaction)

        last_list = self.last_transactions.get(account_id)
        if last_list is None:
            raise RuntimeError(f"Cannot find last transactions for account id: {account_id}")
        last_list.append(transaction)

# create a single service instance (in-memory sample data lives here)
transaction_service_singleton = TransactionService()