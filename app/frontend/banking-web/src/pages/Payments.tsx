import { useState, useEffect } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { Clock, DollarSign, CheckCircle, CreditCard } from "lucide-react";
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, TooltipProps } from "recharts";
import { bffClient } from "@/api/bffClient";
import { Payment } from "@/models/Payments";
import { useAgentResponseHandler } from "@/context/AgentResponseContext";

export default function Payments() {
  const [filter, setFilter] = useState<'all' | 'pending' | 'paid'>('all');
  const [searchTerm, setSearchTerm] = useState('');
  const [paymentsData, setPaymentsData] = useState<Payment[]>([]);
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  useEffect(() => {
    bffClient.getPayments().then(setPaymentsData);
  }, [refreshTrigger]);

  useAgentResponseHandler(() => {
    console.log("Refresh update "+ refreshTrigger);
    setRefreshTrigger((prev) => prev + 1);
  });

  const isPendingStatus = (status: string) => status === 'pending' || status === 'overdue';
  const normalizeStatus = (status: string) => (isPendingStatus(status) ? 'pending' : status);

  const filteredPayments = paymentsData.filter(payment => {
    const matchesFilter =
      filter === 'all' ||
      (filter === 'pending' && isPendingStatus(payment.status)) ||
      (filter === 'paid' && payment.status === 'paid');
    // For demo, just filter by id as name is not present in Payment model
    const matchesSearch = payment.description.toLowerCase().includes(searchTerm.toLowerCase()) || payment.recipientName.toLowerCase().includes(searchTerm.toLowerCase());
    return matchesFilter && matchesSearch;
  });

  const getStatusColor = (status: string) => {
    switch (normalizeStatus(status)) {
      case 'paid': return 'bg-green-500/20 text-green-500 border-green-500/50';
      case 'pending': return 'bg-yellow-500/20 text-yellow-500 border-yellow-500/50';
      default: return 'bg-green-500/20 text-green-500 border-green-500/50';
    }
  };

  const getStatusIcon = (status: string) => {
    switch (normalizeStatus(status)) {
      case 'paid': return <CheckCircle className="h-4 w-4" />;
      default: return <Clock className="h-4 w-4" />;
    }
  };

  const totalPending = paymentsData.filter(payment => isPendingStatus(payment.status)).reduce((sum, payment) => sum + payment.amount, 0);
  const totalPaid = paymentsData.filter(payment => payment.status === 'paid').reduce((sum, payment) => sum + payment.amount, 0);

  const now = new Date();
  const currentMonth = now.getMonth();
  const currentYear = now.getFullYear();

  const paymentsThisMonth = paymentsData.filter(payment => {
    const paymentDate = new Date(payment.timestamp);
    return paymentDate.getMonth() === currentMonth && paymentDate.getFullYear() === currentYear;
  });

  const monthLabel = new Intl.DateTimeFormat(undefined, { month: 'long', year: 'numeric' }).format(now);

  type PaymentLinePoint = {
    id: string;
    amount: number;
    label: string;
    displayTime: string;
    tooltipDate: string;
  };

  const paymentLineData: PaymentLinePoint[] = paymentsThisMonth
    .slice()
    .sort((a, b) => new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime())
    .map(payment => {
      const paymentDate = new Date(payment.timestamp);
      return {
        id: payment.id,
        amount: payment.amount,
        label: payment.description,
        displayTime: paymentDate.toLocaleString(undefined, {
          month: 'short',
          day: 'numeric',
          hour: 'numeric',
          minute: '2-digit'
        }),
        tooltipDate: paymentDate.toLocaleString()
      };
    });

  const renderPaymentTooltip = ({ active, payload }: TooltipProps<number, string>) => {
    if (!active || !payload || payload.length === 0) {
      return null;
    }

    const point = payload[0].payload as PaymentLinePoint;

    return (
      <div className="rounded-lg border border-border/60 bg-card/80 p-3 text-sm text-foreground">
        <p className="font-semibold">{point.tooltipDate}</p>
        <p className="text-xs text-muted-foreground">{point.label}</p>
        <p className="text-base font-bold text-foreground">${point.amount.toFixed(2)}</p>
      </div>
    );
  };

  return (
    <div className="p-6 space-y-6 animate-fade-in">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold text-foreground">Payments</h1>
        <Button className="bg-primary hover:bg-primary/90">
          <CreditCard className="h-4 w-4 mr-2" />
          Make Payment
        </Button>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card className="bg-card/50 backdrop-blur border-border/50">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">Total Pending</CardTitle>
            <Clock className="h-4 w-4 text-yellow-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-foreground">${totalPending.toFixed(2)}</div>
            <p className="text-xs text-muted-foreground">{filteredPayments.filter(p => isPendingStatus(p.status)).length} payments pending</p>
          </CardContent>
        </Card>

        <Card className="bg-card/50 backdrop-blur border-border/50">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">Paid This Month</CardTitle>
            <CheckCircle className="h-4 w-4 text-green-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-green-500">${totalPaid.toFixed(2)}</div>
            <p className="text-xs text-muted-foreground">{filteredPayments.filter(p => p.status === 'paid').length} payment completed</p>
          </CardContent>
        </Card>

        <Card className="bg-card/50 backdrop-blur border-border/50">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">Monthly Budget</CardTitle>
            <DollarSign className="h-4 w-4 text-primary" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-foreground">$6,250</div>
            <p className="text-xs text-muted-foreground">Average monthly payments</p>
          </CardContent>
        </Card>
      </div>

      {/* Payments List and Chart */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Payments List */}
        <Card className="bg-card/50 backdrop-blur border-border/50">
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle className="text-foreground">Current Payments</CardTitle>
              <div className="flex space-x-2">
                <Button
                  variant={filter === 'all' ? 'default' : 'outline'}
                  size="sm"
                  onClick={() => setFilter('all')}
                >
                  All
                </Button>
                <Button
                  variant={filter === 'pending' ? 'default' : 'outline'}
                  size="sm"
                  onClick={() => setFilter('pending')}
                >
                  Pending
                </Button>
                <Button
                  variant={filter === 'paid' ? 'default' : 'outline'}
                  size="sm"
                  onClick={() => setFilter('paid')}
                >
                  Paid
                </Button>
              </div>
            </div>
            <Input
              placeholder="Search payments..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="max-w-sm"
            />
          </CardHeader>
          <CardContent>
            <div className="space-y-4 max-h-96 overflow-y-auto">
              {filteredPayments.map((payment) => {
                const statusLabel = normalizeStatus(payment.status);
                const paymentTypeLabel = payment.paymentType ?? 'Unknown';
                const showCardId = payment.paymentType === 'CreditCard' && payment.cardId;
                return (
                  <div key={payment.id} className="flex items-center justify-between p-4 rounded-lg bg-muted/30 hover:bg-muted/50 transition-colors">
                      <div className="flex items-center space-x-3">
                      {getStatusIcon(payment.status)}
                      <div>
                        <p className="font-medium text-foreground">{payment.description}</p>
                        <p className="text-sm text-muted-foreground">Date: {payment.timestamp}</p>
                        <p className="text-sm text-muted-foreground">Method: {paymentTypeLabel}</p>
                        <p className="text-sm text-muted-foreground">Recipient: {payment.recipientName}</p>
                        {showCardId && (
                          <p className="text-xs uppercase tracking-wide text-muted-foreground">Card ID: {payment.cardId}</p>
                        )}
                      </div>
                      </div>
                    <div className="text-right space-y-1">
                      <p className="font-semibold text-foreground">${payment.amount.toFixed(2)}</p>
                      <Badge className={getStatusColor(payment.status)}>
                        {statusLabel}
                      </Badge>
                    </div>
                  </div>
                );
              })}
            </div>
          </CardContent>
        </Card>

        {/* Monthly Trends Chart */}
        <Card className="bg-card/50 backdrop-blur border-border/50">
          <CardHeader>
            <CardTitle className="text-foreground">Monthly Payment Trend — {monthLabel}</CardTitle>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={350}>
              {paymentLineData.length > 0 ? (
                <LineChart data={paymentLineData} margin={{ top: 24, right: 8, bottom: 8, left: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#374151" />
                  <XAxis dataKey="displayTime" stroke="#9ca3af" tick={{ fontSize: 12, fill: '#9ca3af' }} />
                  <YAxis stroke="#9ca3af" tickFormatter={(value) => `$${value}`} />
                  <Tooltip content={renderPaymentTooltip} cursor={{ stroke: '#10b981', strokeWidth: 2, opacity: 0.5 }} />
                  <Line
                    type="monotone"
                    dataKey="amount"
                    stroke="#10b981"
                    strokeWidth={2}
                    dot={{ r: 4, strokeWidth: 2, stroke: '#10b981' }}
                    activeDot={{ r: 6 }}
                  />
                </LineChart>
              ) : (
                <div className="text-muted-foreground text-center py-8">No payments captured for {monthLabel} yet.</div>
              )}
            </ResponsiveContainer>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
