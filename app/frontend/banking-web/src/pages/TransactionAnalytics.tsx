


import { useState, useEffect } from "react";
import { bffApi } from "@/mocks/bffApi";
import {
  Select, SelectTrigger, SelectValue, SelectContent, SelectItem
} from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
  Card, CardHeader, CardTitle, CardContent
} from "@/components/ui/card";
import { TrendingUp, TrendingDown, ArrowUpRight, ArrowDownRight, DollarSign, Filter } from "lucide-react";
import {
  ResponsiveContainer, AreaChart, CartesianGrid, XAxis, YAxis, Tooltip, Area, BarChart, Bar
} from "recharts";

interface TransactionTrend { date: string; income: number; expenses: number; }
interface CategoryBreakdown { category: string; amount: number; percentage: number; trend: string; color: string; }
interface CashFlow { month: string; inflow: number; outflow: number; }
interface RecentAnalytics { metric: string; value: string; positive: boolean | null; change: string; }

export default function TransactionAnalytics() {
  const [transactionTrends, setTransactionTrends] = useState<TransactionTrend[]>([]);
  const [categoryBreakdown, setCategoryBreakdown] = useState<CategoryBreakdown[]>([]);
  const [cashFlowData, setCashFlowData] = useState<CashFlow[]>([]);
  const [recentAnalytics, setRecentAnalytics] = useState<RecentAnalytics[]>([]);
  const [timeRange, setTimeRange] = useState('30d');
  const [category, setCategory] = useState('all');

  useEffect(() => {
    bffApi.getTransactionTrends().then(setTransactionTrends);
    bffApi.getCategoryBreakdown().then(setCategoryBreakdown);
    bffApi.getCashFlowData().then(setCashFlowData);
    bffApi.getRecentAnalytics().then(setRecentAnalytics);
  }, []);

  return (
    <div className="p-6 space-y-6 animate-fade-in">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold text-foreground">Transaction Analytics</h1>
        <div className="flex items-center space-x-3">
          <Select value={timeRange} onValueChange={setTimeRange}>
            <SelectTrigger className="w-32">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="7d">7 Days</SelectItem>
              <SelectItem value="30d">30 Days</SelectItem>
              <SelectItem value="90d">90 Days</SelectItem>
              <SelectItem value="1y">1 Year</SelectItem>
            </SelectContent>
          </Select>
          <Select value={category} onValueChange={setCategory}>
            <SelectTrigger className="w-40">
              <SelectValue placeholder="Category" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Categories</SelectItem>
              <SelectItem value="operations">Operations</SelectItem>
              <SelectItem value="utilities">Utilities</SelectItem>
              <SelectItem value="equipment">Equipment</SelectItem>
            </SelectContent>
          </Select>
          <Button variant="outline">
            <Filter className="h-4 w-4 mr-2" />
            Filters
          </Button>
        </div>
      </div>

      {/* Key Analytics Metrics */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        {recentAnalytics.map((metric, index) => (
          <Card key={index} className="bg-card/50 backdrop-blur border-border/50 hover:bg-card/70 transition-all duration-200">
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">{metric.metric}</CardTitle>
              {metric.positive === true && <TrendingUp className="h-4 w-4 text-green-500" />}
              {metric.positive === false && <TrendingDown className="h-4 w-4 text-red-500" />}
              {metric.positive === null && <DollarSign className="h-4 w-4 text-primary" />}
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-foreground">{metric.value}</div>
              <p className={`text-xs flex items-center ${
                metric.positive === true ? 'text-green-500' : 
                metric.positive === false ? 'text-red-500' : 'text-muted-foreground'
              }`}>
                {metric.positive === true && <ArrowUpRight className="h-3 w-3 mr-1" />}
                {metric.positive === false && <ArrowDownRight className="h-3 w-3 mr-1" />}
                {metric.change}
              </p>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Charts Row */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Transaction Trends */}
        <Card className="bg-card/50 backdrop-blur border-border/50">
          <CardHeader>
            <CardTitle className="text-foreground">Transaction Trends</CardTitle>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <AreaChart data={transactionTrends}>
                <CartesianGrid strokeDasharray="3 3" stroke="#374151" />
                <XAxis dataKey="date" stroke="#9ca3af" />
                <YAxis stroke="#9ca3af" />
                <Tooltip 
                  contentStyle={{ 
                    backgroundColor: 'hsl(var(--card))', 
                    border: '1px solid hsl(var(--border))',
                    borderRadius: '8px'
                  }}
                />
                <Area 
                  type="monotone" 
                  dataKey="income" 
                  stackId="1" 
                  stroke="#10b981" 
                  fill="#10b981" 
                  fillOpacity={0.6}
                />
                <Area 
                  type="monotone" 
                  dataKey="expenses" 
                  stackId="2" 
                  stroke="#f43f5e" 
                  fill="#f43f5e" 
                  fillOpacity={0.6}
                />
              </AreaChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        {/* Cash Flow Analysis */}
        <Card className="bg-card/50 backdrop-blur border-border/50">
          <CardHeader>
            <CardTitle className="text-foreground">Monthly Cash Flow</CardTitle>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={cashFlowData}>
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
                <Bar dataKey="inflow" fill="#10b981" />
                <Bar dataKey="outflow" fill="#f43f5e" />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      </div>

      {/* Category Breakdown */}
      <Card className="bg-card/50 backdrop-blur border-border/50">
        <CardHeader>
          <CardTitle className="text-foreground">Expense Categories Analysis</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-6">
            {categoryBreakdown.map((category, index) => (
              <div key={index} className="space-y-2">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-3">
                    <div 
                      className="w-4 h-4 rounded-full" 
                      style={{ backgroundColor: category.color }}
                    />
                    <span className="font-medium text-foreground">{category.category}</span>
                  </div>
                  <div className="flex items-center space-x-4">
                    <Badge 
                      variant="outline" 
                      className={category.trend.startsWith('+') ? 'text-green-500 border-green-500/50' : 'text-red-500 border-red-500/50'}
                    >
                      {category.trend}
                    </Badge>
                    <span className="font-semibold text-foreground">${category.amount.toLocaleString()}</span>
                  </div>
                </div>
                <div className="w-full bg-muted rounded-full h-2">
                  <div 
                    className="h-2 rounded-full transition-all duration-500" 
                    style={{ 
                      width: `${category.percentage}%`, 
                      backgroundColor: category.color 
                    }}
                  />
                </div>
                <div className="flex justify-between text-sm text-muted-foreground">
                  <span>{category.percentage}% of total expenses</span>
                  <span>Last 30 days</span>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
