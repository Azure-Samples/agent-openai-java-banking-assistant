// Credit Card Domain Model
export type CreditCardStatus = "active" | "blocked" | "expired";
export type CreditCardType = "credit" | "debit" | "recharge";
export type CreditCardCircuit = "visa" | "mastercard" | "amex";

export class CreditCard {
  constructor(
    public id: string,
    public name: string,
    public number: string,
    public balance: number,
    public rechargedAmount: number,
    public limit: number,
    public expirationDate: string,
    public status: CreditCardStatus,
    public circuit: CreditCardCircuit,
    public type: CreditCardType,
  ) {}
}
