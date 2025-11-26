// Transaction Domain Model
export interface Transaction {
  id: string;
  type: "buy" | "sell";
  symbol: string;
  shares: number;
  price: number;
  date: string;
  total: number;
}
