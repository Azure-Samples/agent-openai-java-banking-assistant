// BFFClient for real REST API calls
import { CreditCard } from "@/models/CreditCard";
import { CreditCardTransaction } from "@/models/CreditCardTransaction";
import { Payment } from "@/models/Payments";
import { UserProfile } from "@/models/UserProfile";

const MOCK_USER_PROFILE: UserProfile = {
  id: "user-12",
  name: "Michael Carter",
  email: "michael.carter@enterprisebank.com",
  accountId: "1010",
  avatar: "/avatar.svg",
  role: "Head of Treasury",
  team: "Enterprise Finance"
};

const MOCK_SESSION_TOKEN = "mock-session-token-2025";

const withDelay = <T>(response: T, delay = 200) =>
  new Promise<T>((resolve) => setTimeout(() => resolve(response), delay));

const BACKEND_URI = import.meta.env.VITE_BACKEND_URI ? import.meta.env.VITE_BACKEND_URI : "";




export class BFFClient {
  private baseUrl: string;
  private sessionToken: string | null = null;

  constructor(baseUrl?: string) {
    this.baseUrl = baseUrl || "/api";
  }

  async login(credentials: { email: string; password: string }) {
    if (!credentials.email || !credentials.password) {
      throw new Error("Please provide both email and password.");
    }

    await withDelay(null);
    this.sessionToken = MOCK_SESSION_TOKEN;

    return {
      token: MOCK_SESSION_TOKEN,
      userId: MOCK_USER_PROFILE.id,
      expiresIn: 3600
    };
  }

  async getUserProfile(): Promise<UserProfile> {
   
    //call user profile with session token

    return withDelay(MOCK_USER_PROFILE);
  }

  async getPayments(): Promise<Payment[]> {
    const transactionAPIUrl = `${this.baseUrl}/transactions`;
    
    const accountId = (await this.getUserProfile()).accountId;
    const response = await fetch(`${transactionAPIUrl}/${accountId}?transaction_type=payment`);
    if (!response.ok) {
      throw new Error("Failed to fetch payments");
    }
    const data = await response.json();
    // Optionally map to Payment instances if needed
    return data;
  }

  async getCards(): Promise<CreditCard[]> {
    const accountsAPIUrl = `${this.baseUrl}/accounts`;
    
    const accountId = (await this.getUserProfile()).accountId;
    const response = await fetch(`${accountsAPIUrl}/${accountId}/cards`);
    if (!response.ok) {
      throw new Error("Failed to fetch payments");
    }
    const data = await response.json();
    // Optionally map to Payment instances if needed
    return data;
  }

  async getCardTransactions(cardId: string | null): Promise<CreditCardTransaction[]> {
    const transactionAPIUrl = `${this.baseUrl}/transactions`;
    
    const accountId = (await this.getUserProfile()).accountId;
    const cardIdParam = cardId ? `&card_id=${cardId}` : "";
    const response = await fetch(`${transactionAPIUrl}/${accountId}?transaction_type=payment&payment_type=CreditCard${cardIdParam}`);
    if (!response.ok) {
      throw new Error("Failed to fetch payments");
    }
    const data = await response.json();
    // Optionally map to Payment instances if needed
    return data;
  }


}

export const bffClient = new BFFClient(BACKEND_URI);
