from typing import List
from models import Transaction
import logging

logger = logging.getLogger(__name__)


class TransactionService:
    def __init__(self):
        self.last_transactions = {}
        self.all_transactions = {}

        # populate sample data for account 1010 similar to Java
        self.last_transactions["1010"] = [
            Transaction(id="11", description="Payment of the bill 334398", type="outcome", recipientName="acme", recipientBankReference="0001", accountId="1010", paymentType="BankTransfer", amount=100.00, timestamp="2024-4-01T12:00:00Z"),
            Transaction(id="22", description="Payment of the bill 4613", type="outcome", recipientName="contoso", recipientBankReference="0002", accountId="1010", paymentType="CreditCard", amount=200.00, timestamp="2024-3-02T12:00:00Z"),
            Transaction(id="33", description="Payment of the bill 724563", type="outcome", recipientName="duff", recipientBankReference="0003", accountId="1010", paymentType="BankTransfer", amount=300.00, timestamp="2023-10-03T12:00:00Z"),
            Transaction(id="43", description="Payment of the bill 8898943", type="outcome", recipientName="wayne enterprises", recipientBankReference="0004", accountId="1010", paymentType="DirectDebit", amount=400.00, timestamp="2023-8-04T12:00:00Z"),
            Transaction(id="53", description="Payment of the bill 19dee", type="outcome", recipientName="oscorp", recipientBankReference="0005", accountId="1010", paymentType="BankTransfer", amount=500.00, timestamp="2023-4-05T12:00:00Z"),
        ]

        self.all_transactions["1010"] = [
            Transaction(id="11", description="payment of bill id with 0001", type="outcome", recipientName="acme", recipientBankReference="A012TABTYT156!", accountId="1010", paymentType="BankTransfer", amount=100.00, timestamp="2024-4-01T12:00:00Z"),
            Transaction(id="21", description="Payment of the bill 4200", type="outcome", recipientName="acme", recipientBankReference="0002", accountId="1010", paymentType="BankTransfer", amount=200.00, timestamp="2024-1-02T12:00:00Z"),
            Transaction(id="31", description="Payment of the bill 3743", type="outcome", recipientName="acme", recipientBankReference="0003", accountId="1010", paymentType="DirectDebit", amount=300.00, timestamp="2023-10-03T12:00:00Z"),
            Transaction(id="41", description="Payment of the bill 8921", type="outcome", recipientName="acme", recipientBankReference="0004", accountId="1010", paymentType="Transfer", amount=400.00, timestamp="2023-8-04T12:00:00Z"),
            Transaction(id="51", description="Payment of the bill 7666", type="outcome", recipientName="acme", recipientBankReference="0005", accountId="1010", paymentType="CreditCard", amount=500.00, timestamp="2023-4-05T12:00:00Z"),

            Transaction(id="12", description="Payment of the bill 5517", type="outcome", recipientName="contoso", recipientBankReference="0001", accountId="1010", paymentType="CreditCard", amount=100.00, timestamp="2024-3-01T12:00:00Z"),
            Transaction(id="22", description="Payment of the bill 682222", type="outcome", recipientName="contoso", recipientBankReference="0002", accountId="1010", paymentType="CreditCard", amount=200.00, timestamp="2023-1-02T12:00:00Z"),
            Transaction(id="32", description="Payment of the bill 94112", type="outcome", recipientName="contoso", recipientBankReference="0003", accountId="1010", paymentType="Transfer", amount=300.00, timestamp="2022-10-03T12:00:00Z"),
            Transaction(id="42", description="Payment of the bill 23122", type="outcome", recipientName="contoso", recipientBankReference="0004", accountId="1010", paymentType="Transfer", amount=400.00, timestamp="2022-8-04T12:00:00Z"),
            Transaction(id="52", description="Payment of the bill 171443", type="outcome", recipientName="contoso", recipientBankReference="0005", accountId="1010", paymentType="Transfer", amount=500.00, timestamp="2020-4-05T12:00:00Z"),
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
        return [t for t in transactions if t.recipientName and name_lower in t.recipientName.lower()]

    def get_last_transactions(self, account_id: str) -> List[Transaction]:
        logger.info("get_last_transactions called with account_id=%s", account_id)
        if not account_id:
            raise ValueError("AccountId is empty or null")
        if not account_id.isdigit():
            raise ValueError("AccountId is not a valid number")
        transactions = self.last_transactions.get(account_id)
        return transactions or []

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