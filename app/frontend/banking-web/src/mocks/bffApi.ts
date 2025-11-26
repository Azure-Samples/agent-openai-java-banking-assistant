// Mocked Backend-for-Frontend API for all business domains
import { CreditCard } from "@/models/CreditCard";
import { CreditCardTransaction } from "@/models/CreditCardTransaction";
import { Account } from "@/models/Account";
import { Payment } from "@/models/Payments";
import { PortfolioAsset } from "@/models/Portfolio";
import { DashboardSummary } from "@/models/Dashboard";


export const bffApi = {
 
  async getCreditCards(): Promise<CreditCard[]> {
    return [
      new CreditCard("1", "Business Platinum", "**** **** **** 1234", 2450.75, 15000, "2025-07-15", "active", "visa"),
      new CreditCard("2", "Corporate Gold", "**** **** **** 5678", 1200.50, 10000, "2025-07-20", "active", "mastercard"),
      new CreditCard("3", "Executive Black", "**** **** **** 9012", 0, 25000, "2025-07-25", "blocked", "amex")
    ];
  },
  async getCreditCardTransactions(): Promise<CreditCardTransaction[]> {
    return [
      {
        id: "1",
        cardId: "1",
        description: "Office Supplies Purchase",
        amount: -342.50,
        date: "2025-06-28",
        category: "Office",
        merchant: "Staples Inc."
      },
      {
        id: "2",
        cardId: "1",
        description: "Business Lunch",
        amount: -125.75,
        date: "2025-06-27",
        category: "Meals",
        merchant: "The Business Grill"
      },
      {
        id: "3",
        cardId: "2",
        description: "Software Subscription",
        amount: -299.99,
        date: "2025-06-26",
        category: "Software",
        merchant: "Adobe Creative Cloud"
      },
      {
        id: "4",
        cardId: "1",
        description: "Payment Received",
        amount: 1500.00,
        date: "2025-06-25",
        category: "Payment",
        merchant: "Account Payment"
      }
    ];
  },
  async getAccounts(): Promise<Account[]> {
    return [
      new Account("1", "Michael Carter", 52800, "Enterprise Checking", "Active"),
    ];
  },

  async getPayments(): Promise<Payment[]> {
    return [
      new Payment("1", "1", "2", 245.50, "2024-01-20", "pending"),
      new Payment("2", "1", "3", 89.99, "2024-01-18", "overdue"),
      new Payment("3", "1", "4", 3500.00, "2024-01-25", "pending"),
      new Payment("4", "1", "5", 450.00, "2024-01-22", "pending"),
      new Payment("5", "1", "6", 299.00, "2024-01-15", "paid"),
      new Payment("6", "1", "7", 850.00, "2024-01-28", "pending"),
    ];
  },

  async getPortfolio(): Promise<PortfolioAsset[]> {
    return [
      new PortfolioAsset("1", "Apple Inc.", "stock", 9262.5, 0.18),
      new PortfolioAsset("2", "Microsoft Corporation", "stock", 11367, 0.22),
      new PortfolioAsset("3", "Alphabet Inc.", "stock", 2142, 0.04),
      new PortfolioAsset("4", "Tesla Inc.", "stock", 6212.5, 0.12),
      new PortfolioAsset("5", "NVIDIA Corporation", "stock", 17512, 0.33),
    ];
  },

  async getDashboardSummary(): Promise<DashboardSummary> {
    return new DashboardSummary(52800, 35000, 15.2);
  },

  // --- Investment Portfolio ---
  async getPortfolioStocks() {
    // Simulate stocks with extra fields for demo
    return [
      {
        id: "1",
        symbol: "AAPL",
        name: "Apple Inc.",
        shares: 50,
        currentPrice: 185.25,
        purchasePrice: 150.00,
        purchaseDate: "2023-01-10",
        sector: "Technology"
      },
      {
        id: "2",
        symbol: "MSFT",
        name: "Microsoft Corporation",
        shares: 30,
        currentPrice: 320.10,
        purchasePrice: 250.00,
        purchaseDate: "2023-03-15",
        sector: "Technology"
      },
      {
        id: "3",
        symbol: "TSLA",
        name: "Tesla Inc.",
        shares: 10,
        currentPrice: 700.00,
        purchasePrice: 600.00,
        purchaseDate: "2023-05-20",
        sector: "Automotive"
      }
    ];
  },

  async getPortfolioTransactions() {
    return [
      {
        id: "t1",
        type: "buy",
        symbol: "AAPL",
        shares: 20,
        price: 150.00,
        date: "2023-01-10",
        total: 3000
      },
      {
        id: "t2",
        type: "buy",
        symbol: "MSFT",
        shares: 10,
        price: 250.00,
        date: "2023-03-15",
        total: 2500
      },
      {
        id: "t3",
        type: "buy",
        symbol: "TSLA",
        shares: 10,
        price: 600.00,
        date: "2023-05-20",
        total: 6000
      },
      {
        id: "t4",
        type: "buy",
        symbol: "AAPL",
        shares: 30,
        price: 160.00,
        date: "2023-08-01",
        total: 4800
      }
    ];
  },

  async getMarketTrends() {
    return [
      {
        symbol: "AAPL",
        name: "Apple Inc.",
        price: 185.25,
        change: 2.15,
        changePercent: 1.17,
        volume: 12000000
      },
      {
        symbol: "MSFT",
        name: "Microsoft Corporation",
        price: 320.10,
        change: -1.05,
        changePercent: -0.33,
        volume: 9500000
      },
      {
        symbol: "TSLA",
        name: "Tesla Inc.",
        price: 700.00,
        change: 10.00,
        changePercent: 1.45,
        volume: 8000000
      },
      {
        symbol: "GOOGL",
        name: "Alphabet Inc.",
        price: 2750.50,
        change: 15.30,
        changePercent: 0.56,
        volume: 6000000
      },
      {
        symbol: "NVDA",
        name: "NVIDIA Corporation",
        price: 950.75,
        change: 22.10,
        changePercent: 2.38,
        volume: 7200000
      },
      {
        symbol: "AMZN",
        name: "Amazon.com Inc.",
        price: 3400.00,
        change: -12.50,
        changePercent: -0.37,
        volume: 5400000
      },
      {
        symbol: "META",
        name: "Meta Platforms Inc.",
        price: 355.20,
        change: 5.60,
        changePercent: 1.60,
        volume: 4100000
      },
      {
        symbol: "JPM",
        name: "JPMorgan Chase & Co.",
        price: 160.80,
        change: 1.20,
        changePercent: 0.75,
        volume: 3800000
      },
      {
        symbol: "V",
        name: "Visa Inc.",
        price: 230.10,
        change: -0.90,
        changePercent: -0.39,
        volume: 2900000
      },
      {
        symbol: "DIS",
        name: "The Walt Disney Company",
        price: 145.60,
        change: 3.10,
        changePercent: 2.18,
        volume: 2500000
      }
    ];
  },

  // --- Transaction Analytics ---
  async getTransactionTrends() {
    return [
      { date: "2024-06-01", income: 12000, expenses: 8000 },
      { date: "2024-06-08", income: 15000, expenses: 9500 },
      { date: "2024-06-15", income: 11000, expenses: 7000 },
      { date: "2024-06-22", income: 17000, expenses: 12000 },
      { date: "2024-06-29", income: 14000, expenses: 9000 }
    ];
  },

  async getCategoryBreakdown() {
    return [
      { category: "Operations", amount: 3500, percentage: 35, trend: "+5%", color: "#3b82f6" },
      { category: "Utilities", amount: 2000, percentage: 20, trend: "-2%", color: "#f59e0b" },
      { category: "Equipment", amount: 1500, percentage: 15, trend: "+1%", color: "#10b981" },
      { category: "Software", amount: 1000, percentage: 10, trend: "+3%", color: "#8b5cf6" },
      { category: "Other", amount: 2000, percentage: 20, trend: "0%", color: "#f43f5e" }
    ];
  },

  async getCashFlowData() {
    return [
      { month: "Jan", inflow: 25000, outflow: 18000 },
      { month: "Feb", inflow: 27000, outflow: 19500 },
      { month: "Mar", inflow: 22000, outflow: 17000 },
      { month: "Apr", inflow: 30000, outflow: 21000 },
      { month: "May", inflow: 26000, outflow: 20000 },
      { month: "Jun", inflow: 28000, outflow: 22000 }
    ];
  },

  async getRecentAnalytics() {
    return [
      { metric: "Total Income", value: "$120,000", positive: true, change: "+8%" },
      { metric: "Total Expenses", value: "$85,000", positive: false, change: "+3%" },
      { metric: "Net Cash Flow", value: "$35,000", positive: null, change: "+5%" },
      { metric: "Avg. Transaction Size", value: "$1,200", positive: true, change: "+2%" }
    ];
  },

  // --- Bills ---
  async getBills() {
    return [
      { id: 1, name: "Electric Company", amount: 245.5, dueDate: "2024-01-20", status: "pending", category: "utilities", recurring: true },
      { id: 2, name: "Internet Service Provider", amount: 89.99, dueDate: "2024-01-18", status: "overdue", category: "utilities", recurring: true },
      { id: 3, name: "Office Rent", amount: 3500.0, dueDate: "2024-01-25", status: "pending", category: "real-estate", recurring: true },
      { id: 4, name: "Insurance Premium", amount: 450.0, dueDate: "2024-01-22", status: "pending", category: "insurance", recurring: true },
      { id: 5, name: "Software Subscription", amount: 299.0, dueDate: "2024-01-15", status: "paid", category: "software", recurring: true },
      { id: 6, name: "Equipment Lease", amount: 850.0, dueDate: "2024-01-28", status: "pending", category: "equipment", recurring: true }
    ];
  },

  async getMonthlyBillTrends() {
    return [
      { month: "Jan", utilities: 850, insurance: 450, software: 299, equipment: 850, rent: 3500 },
      { month: "Feb", utilities: 920, insurance: 450, software: 299, equipment: 850, rent: 3500 },
      { month: "Mar", utilities: 780, insurance: 450, software: 299, equipment: 850, rent: 3500 },
      { month: "Apr", utilities: 845, insurance: 450, software: 299, equipment: 850, rent: 3500 },
      { month: "May", utilities: 912, insurance: 450, software: 299, equipment: 850, rent: 3500 },
      { month: "Jun", utilities: 834, insurance: 450, software: 299, equipment: 850, rent: 3500 }
    ];
  }
};
