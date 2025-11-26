import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { BarChart3, DollarSign, CreditCard, TrendingUp, ArrowUpRight, ArrowDownRight, Wallet, PieChart as PieChartIcon } from "lucide-react";
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, BarChart, Bar, PieChart, Pie, Cell } from "recharts";


import { useEffect, useState } from "react";
import { bffApi } from "@/mocks/bffApi";
import { DashboardSummary } from "@/models/Dashboard";


export default function Dashboard() {
  const [dashboardSummary, setDashboardSummary] = useState<DashboardSummary | null>(null);
  type RecentTransaction = {
	id: string;
	description: string;
	date: string;
	amount: number;
	type: 'income' | 'utility' | 'supplies' | 'other';
  };
  type AccountData = { month: string; balance: number };
  type ExpenseCategory = { name: string; value: number; color: string };
  const [recentTransactions, setRecentTransactions] = useState<RecentTransaction[]>([]);
  const [accountData, setAccountData] = useState<AccountData[]>([]);
  const [expenseCategories, setExpenseCategories] = useState<ExpenseCategory[]>([]);

  useEffect(() => {
	bffApi.getDashboardSummary().then(setDashboardSummary);
	// Mock recent transactions
	setRecentTransactions([
	  { id: '1', description: 'Payment Received', date: '2025-06-25', amount: 1500, type: 'income' },
	  { id: '2', description: 'Office Supplies', date: '2025-06-28', amount: -342.5, type: 'supplies' },
	  { id: '3', description: 'Utility Bill', date: '2025-06-27', amount: -120, type: 'utility' },
	  { id: '4', description: 'Software Subscription', date: '2025-06-26', amount: -299.99, type: 'other' },
	]);
	// Mock account balance trend
	setAccountData([
	  { month: 'Jan', balance: 25000 },
	  { month: 'Feb', balance: 27000 },
	  { month: 'Mar', balance: 22000 },
	  { month: 'Apr', balance: 30000 },
	  { month: 'May', balance: 26000 },
	  { month: 'Jun', balance: 28000 },
	]);
	// Mock expense categories
	setExpenseCategories([
	  { name: 'Utilities', value: 1200, color: '#3b82f6' },
	  { name: 'Supplies', value: 800, color: '#f59e0b' },
	  { name: 'Software', value: 600, color: '#10b981' },
	  { name: 'Other', value: 400, color: '#8b5cf6' },
	]);
  }, []);

  return (
	<div className="p-6 space-y-6 animate-fade-in">
	  <div className="flex items-center justify-between">
		<h1 className="text-3xl font-bold text-foreground">Dashboard Overview</h1>
		<div className="text-sm text-muted-foreground">
		  Last updated: {new Date().toLocaleDateString()}
		</div>
	  </div>

	  {/* Key Metrics */}
	  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
		<Card className="bg-card/50 backdrop-blur border-border/50 hover:bg-card/70 transition-all duration-200">
		  <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
			<CardTitle className="text-sm font-medium text-muted-foreground">Total Balance</CardTitle>
			<DollarSign className="h-4 w-4 text-primary" />
		  </CardHeader>
		  <CardContent>
			<div className="text-2xl font-bold text-foreground">
			  ${dashboardSummary?.totalBalance?.toLocaleString() ?? '--'}
			</div>
			<p className="text-xs text-green-500 flex items-center">
			  <ArrowUpRight className="h-3 w-3 mr-1" />
			  +12.5% from last month
			</p>
		  </CardContent>
		</Card>
		<Card className="bg-card/50 backdrop-blur border-border/50 hover:bg-card/70 transition-all duration-200">
		  <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
			<CardTitle className="text-sm font-medium text-muted-foreground">Total Credit Limit</CardTitle>
			<CreditCard className="h-4 w-4 text-primary" />
		  </CardHeader>
		  <CardContent>
			<div className="text-2xl font-bold text-foreground">
			  ${dashboardSummary?.totalCreditLimit?.toLocaleString() ?? '--'}
			</div>
			<p className="text-xs text-red-500 flex items-center">
			  <ArrowDownRight className="h-3 w-3 mr-1" />
			  +8.2% from last month
			</p>
		  </CardContent>
		</Card>
		<Card className="bg-card/50 backdrop-blur border-border/50 hover:bg-card/70 transition-all duration-200">
		  <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
			<CardTitle className="text-sm font-medium text-muted-foreground">Utilization Rate</CardTitle>
			<TrendingUp className="h-4 w-4 text-primary" />
		  </CardHeader>
		  <CardContent>
			<div className="text-2xl font-bold text-foreground">
			  {dashboardSummary?.utilizationRate?.toFixed(1) ?? '--'}%
			</div>
			<p className="text-xs text-green-500">Steady growth trend</p>
		  </CardContent>
		</Card>
	  </div>

			{/* New Features Overview */}
			<div className="grid grid-cols-1 md:grid-cols-2 gap-6">
				<Card className="bg-card/50 backdrop-blur border-border/50 hover:bg-card/70 transition-all duration-200">
					<CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
						<CardTitle className="text-sm font-medium text-muted-foreground">Credit Cards</CardTitle>
						<Wallet className="h-4 w-4 text-primary" />
					</CardHeader>
					<CardContent>
						<div className="text-2xl font-bold text-foreground">$3,651</div>
						<p className="text-xs text-muted-foreground">Total balance across 3 cards</p>
						<div className="mt-2 text-xs text-blue-600 hover:text-blue-800">
							<a href="/credit-cards" className="flex items-center">
								View cards <ArrowUpRight className="h-3 w-3 ml-1" />
							</a>
						</div>
					</CardContent>
				</Card>

				<Card className="bg-card/50 backdrop-blur border-border/50 hover:bg-card/70 transition-all duration-200">
					<CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
						<CardTitle className="text-sm font-medium text-muted-foreground">Investment Portfolio</CardTitle>
						<PieChartIcon className="h-4 w-4 text-primary" />
					</CardHeader>
					<CardContent>
						<div className="text-2xl font-bold text-foreground">$68,425</div>
						<p className="text-xs text-green-500 flex items-center">
							<ArrowUpRight className="h-3 w-3 mr-1" />
							+15.3% portfolio gain
						</p>
						<div className="mt-2 text-xs text-blue-600 hover:text-blue-800">
							<a href="/portfolio" className="flex items-center">
								View portfolio <ArrowUpRight className="h-3 w-3 ml-1" />
							</a>
						</div>
					</CardContent>
				</Card>
			</div>

			{/* Charts Row */}
			<div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
				<Card className="bg-card/50 backdrop-blur border-border/50">
					<CardHeader>
						<CardTitle className="text-foreground">Account Balance Trend</CardTitle>
					</CardHeader>
					<CardContent>
						<ResponsiveContainer width="100%" height={300}>
							<LineChart data={accountData}>
								<CartesianGrid strokeDasharray="3 3" stroke="#374151" />
								<XAxis dataKey="month" stroke="#9ca3af" />
								<YAxis stroke="#9ca3af" />
								<Tooltip
									contentStyle={{
										backgroundColor: 'hsl(var(--card))',
										border: '1px solid hsl(var(--border))',
										borderRadius: '8px'
									}}
								/>
								<Line
									type="monotone"
									dataKey="balance"
									stroke="hsl(var(--primary))"
									strokeWidth={3}
									dot={{ fill: 'hsl(var(--primary))' }}
								/>
							</LineChart>
						</ResponsiveContainer>
					</CardContent>
				</Card>

				<Card className="bg-card/50 backdrop-blur border-border/50">
					<CardHeader>
						<CardTitle className="text-foreground">Expense Categories</CardTitle>
					</CardHeader>
					<CardContent>
						<ResponsiveContainer width="100%" height={300}>
							<PieChart>
								<Pie
									data={expenseCategories}
									cx="50%"
									cy="50%"
									outerRadius={100}
									fill="#8884d8"
									dataKey="value"
									label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
								>
									{expenseCategories.map((entry, index) => (
										<Cell key={`cell-${index}`} fill={entry.color} />
									))}
								</Pie>
								<Tooltip
									contentStyle={{
										backgroundColor: 'hsl(var(--card))',
										border: '1px solid hsl(var(--border))',
										borderRadius: '8px'
									}}
								/>
							</PieChart>
						</ResponsiveContainer>
					</CardContent>
				</Card>
			</div>

			{/* Recent Transactions */}
			<Card className="bg-card/50 backdrop-blur border-border/50">
				<CardHeader>
					<CardTitle className="text-foreground">Recent Transactions</CardTitle>
				</CardHeader>
				<CardContent>
					<div className="space-y-4">
						{recentTransactions.map((transaction) => (
							<div key={transaction.id} className="flex items-center justify-between p-3 rounded-lg bg-muted/30 hover:bg-muted/50 transition-colors">
								<div className="flex items-center space-x-3">
									<div className={`w-2 h-2 rounded-full ${
										transaction.type === 'income' ? 'bg-green-500' :
										transaction.type === 'utility' ? 'bg-blue-500' :
										transaction.type === 'supplies' ? 'bg-yellow-500' : 'bg-purple-500'
									}`} />
									<div>
										<p className="font-medium text-foreground">{transaction.description}</p>
										<p className="text-sm text-muted-foreground">{transaction.date}</p>
									</div>
								</div>
								<div className={`text-right font-semibold ${
									transaction.amount > 0 ? 'text-green-500' : 'text-red-500'
								}`}>
									{transaction.amount > 0 ? '+' : ''}${Math.abs(transaction.amount).toFixed(2)}
								</div>
							</div>
						))}
					</div>
				</CardContent>
			</Card>
		</div>
	);
}
