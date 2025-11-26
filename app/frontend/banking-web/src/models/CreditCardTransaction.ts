export interface CreditCardTransaction {
  id: string;
  cardId: string;
  description: string;
  amount: number;
  timestamp: string;
  category: string;
  recipientName: string;
}
