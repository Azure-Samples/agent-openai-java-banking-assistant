
import { useState, useEffect } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { CreditCard as CreditCardIcon, Plus, DollarSign, Calendar, TrendingUp } from "lucide-react";
import { CreditCard } from "@/models/CreditCard";
import { CreditCardTransaction } from "@/models/CreditCardTransaction";
import { bffApi } from "@/mocks/bffApi";
import { bffClient } from "@/api/bffClient";


const CreditCardManagement = () => {
  const [selectedCard, setSelectedCard] = useState<string>("");
  const [rechargeAmount, setRechargeAmount] = useState("");
  const [creditCards, setCreditCards] = useState<CreditCard[]>([]);
  const [transactions, setTransactions] = useState<CreditCardTransaction[]>([]);

  useEffect(() => {
    // Fetch credit cards and transactions from the mocked BFF API
    bffClient.getCards().then(setCreditCards);
    bffClient.getCardTransactions().then(setTransactions);
  }, []);

  const getCardTypeIcon = (circuit: string) => {
    switch (circuit) {
      case "visa":
        return <img src="/visa.svg" alt="Visa" className="w-12 h-12" />;
      case "mastercard":
        return "🔴";
      case "amex":
        return <img src="/amex.svg" alt="Amex" className="w-12 h-12" />;
      default:
        return "💳";
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case "active":
        return "bg-green-100 text-green-800";
      case "blocked":
        return "bg-red-100 text-red-800";
      case "expired":
        return "bg-gray-100 text-gray-800";
      default:
        return "bg-gray-100 text-gray-800";
    }
  };

  const maskCardNumber = (cardNumber: string) => {
    const lastFour = cardNumber.slice(-4);
    return `•••• •••• •••• ${lastFour}`;
  };

  const handleRecharge = () => {
    if (selectedCard && rechargeAmount) {
      // Here you would typically make an API call to recharge the card
      console.log(`Recharging card ${selectedCard} with $${rechargeAmount}`);
      setRechargeAmount("");
      setSelectedCard("");
    }
  };

  const filteredTransactions = selectedCard 
    ? transactions.filter(t => t.cardId === selectedCard)
    : transactions;

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Credit Card Management</h1>
          <p className="text-muted-foreground">
            Manage your credit cards, view balances, and track transactions
          </p>
        </div>
        <Dialog>
          <DialogTrigger asChild>
            <Button className="flex items-center gap-2">
              <Plus className="h-4 w-4" />
              Add New Card
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Add New Credit Card</DialogTitle>
              <DialogDescription>
                Enter your credit card details to add it to your account.
              </DialogDescription>
            </DialogHeader>
            <div className="grid gap-4 py-4">
              <div className="grid gap-2">
                <Label htmlFor="cardName">Card Name</Label>
                <Input id="cardName" placeholder="e.g., Business Platinum" />
              </div>
              <div className="grid gap-2">
                <Label htmlFor="cardNumber">Card Number</Label>
                <Input id="cardNumber" placeholder="1234 5678 9012 3456" />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="grid gap-2">
                  <Label htmlFor="expiry">Expiry Date</Label>
                  <Input id="expiry" placeholder="MM/YY" />
                </div>
                <div className="grid gap-2">
                  <Label htmlFor="cvv">CVV</Label>
                  <Input id="cvv" placeholder="123" />
                </div>
              </div>
            </div>
            <Button className="w-full">Add Credit Card</Button>
          </DialogContent>
        </Dialog>
      </div>

      {/* Overview Cards */}
      <div className="grid gap-4 md:grid-cols-3">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Balance</CardTitle>
            <DollarSign className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              ${creditCards.reduce((sum, card) => sum + card.balance, 0).toLocaleString()}
            </div>
            <p className="text-xs text-muted-foreground">
              Across {creditCards.length} active cards
            </p>
          </CardContent>
        </Card>
        
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Credit Limit</CardTitle>
            <CreditCardIcon className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              ${creditCards.reduce((sum, card) => sum + card.limit, 0).toLocaleString()}
            </div>
            <p className="text-xs text-muted-foreground">
              Available credit across all cards
            </p>
          </CardContent>
        </Card>
        
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Utilization Rate</CardTitle>
            <TrendingUp className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {((creditCards.reduce((sum, card) => sum + card.balance, 0) / 
                 creditCards.reduce((sum, card) => sum + card.limit, 0)) * 100).toFixed(1)}%
            </div>
            <p className="text-xs text-muted-foreground">
              Overall credit utilization
            </p>
          </CardContent>
        </Card>
      </div>

      <Tabs defaultValue="cards" className="space-y-4">
        <TabsList>
          <TabsTrigger value="cards">Credit Cards</TabsTrigger>
          <TabsTrigger value="transactions">Transactions</TabsTrigger>
          <TabsTrigger value="recharge">Recharge</TabsTrigger>
        </TabsList>

        <TabsContent value="cards" className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {creditCards.map((card) => (
              <Card key={card.id} className="relative overflow-hidden">
                <div className="absolute top-0 right-0 w-20 h-20 bg-gradient-to-br from-blue-500/10 to-purple-500/10 rounded-bl-3xl" />
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <div className="text-2xl flex items-center justify-center">{getCardTypeIcon(card.circuit)}</div>
                      <div>
                        <CardTitle className="text-lg">{card.name}</CardTitle>
                        <CardDescription>{maskCardNumber(card.number)}</CardDescription>
                      </div>
                    </div>
                    <Badge className={getStatusColor(card.status)}>
                      {card.status}
                    </Badge>
                  </div>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="flex justify-between items-center">
                    <div>
                      <p className="text-sm text-muted-foreground">Current Balance</p>
                      <p className="text-2xl font-bold">${card.balance.toLocaleString()}</p>
                    </div>
                    <div className="text-right space-y-3">
                      <div>
                        <p className="text-sm text-muted-foreground">Credit Limit</p>
                        <p className="text-lg font-semibold">${card.limit.toLocaleString()}</p>
                      </div>
                      {card.type === 'recharge' && card.rechargedAmount > 0 && (
                        <div>
                          <p className="text-sm text-muted-foreground">Recharged Amount</p>
                          <p className="text-lg font-semibold text-green-600">${card.rechargedAmount.toLocaleString()}</p>
                        </div>
                      )}
                    </div>
                  </div>
                  
                  <div className="w-full bg-gray-200 rounded-full h-2">
                    <div 
                      className="bg-blue-600 h-2 rounded-full" 
                      style={{ width: `${(card.balance / card.limit) * 100}%` }}
                    />
                  </div>
                  
                  <div className="flex items-center justify-between text-sm">
                    <div className="flex items-center gap-1">
                      <Calendar className="h-4 w-4 text-muted-foreground" />
                      <span>Expire: {new Date(card.expirationDate).toLocaleDateString()}</span>
                    </div>
                    <span className="text-muted-foreground">
                      {((card.balance / card.limit) * 100).toFixed(1)}% used
                    </span>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        </TabsContent>

        <TabsContent value="transactions" className="space-y-4">
          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <div>
                  <CardTitle>Recent Transactions</CardTitle>
                  <CardDescription>View and manage your credit card transactions</CardDescription>
                </div>
                <select 
                  className="rounded-md border border-input bg-background px-3 py-2"
                  value={selectedCard}
                  onChange={(e) => setSelectedCard(e.target.value)}
                >
                  <option value="">All Cards</option>
                  {creditCards.map((card) => (
                    <option key={card.id} value={card.id}>
                      {card.name} ({maskCardNumber(card.number)})
                    </option>
                  ))}
                </select>
              </div>
            </CardHeader>
            <CardContent>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Date</TableHead>
                    <TableHead>Description</TableHead>
                    <TableHead>Merchant</TableHead>
                    <TableHead>Category</TableHead>
                    <TableHead className="text-right">Amount</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filteredTransactions.map((transaction) => (
                    <TableRow key={transaction.id}>
                      <TableCell>{new Date(transaction.timestamp).toLocaleDateString()}</TableCell>
                      <TableCell className="font-medium">{transaction.description}</TableCell>
                      <TableCell>{transaction.recipientName}</TableCell>
                      <TableCell>
                        <Badge variant="outline">{transaction.category}</Badge>
                      </TableCell>
                      <TableCell className={`text-right font-medium ${
                        transaction.amount > 0 ? 'text-green-600' : 'text-red-600'
                      }`}>
                        {transaction.amount > 0 ? '+' : ''}${Math.abs(transaction.amount).toFixed(2)}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="recharge" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Recharge Credit Card</CardTitle>
              <CardDescription>
                Make a payment to reduce your credit card balance
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid gap-4">
                <div className="grid gap-2">
                  <Label htmlFor="card-select">Select Credit Card</Label>
                  <select 
                    id="card-select"
                    className="rounded-md border border-input bg-background px-3 py-2"
                    value={selectedCard}
                    onChange={(e) => setSelectedCard(e.target.value)}
                  >
                    <option value="">Choose a card...</option>
                    {creditCards.filter(card => card.status === 'active' && card.type === 'recharge').map((card) => (
                      <option key={card.id} value={card.id}>
                        {card.name} ({maskCardNumber(card.number)}) - Balance: ${card.balance.toFixed(2)}
                      </option>
                    ))}
                  </select>
                </div>
                
                <div className="grid gap-2">
                  <Label htmlFor="amount">Payment Amount</Label>
                  <Input
                    id="amount"
                    type="number"
                    placeholder="0.00"
                    value={rechargeAmount}
                    onChange={(e) => setRechargeAmount(e.target.value)}
                  />
                </div>
                
                <Button 
                  onClick={handleRecharge}
                  disabled={!selectedCard || !rechargeAmount}
                  className="w-full"
                >
                  Make Payment
                </Button>
              </div>
              
              {selectedCard && (
                <div className="mt-6 p-4 border rounded-lg bg-muted/50">
                  <h4 className="font-medium mb-2">Payment Summary</h4>
                  {(() => {
                    const card = creditCards.find(c => c.id === selectedCard);
                    const paymentAmount = parseFloat(rechargeAmount) || 0;
                    const newBalance = Math.max(0, (card?.balance || 0) - paymentAmount);
                    
                    return (
                      <div className="space-y-2 text-sm">
                        <div className="flex justify-between">
                          <span>Current Balance:</span>
                          <span>${card?.balance.toFixed(2)}</span>
                        </div>
                        <div className="flex justify-between">
                          <span>Payment Amount:</span>
                          <span>${paymentAmount.toFixed(2)}</span>
                        </div>
                        <hr />
                        <div className="flex justify-between font-medium">
                          <span>New Balance:</span>
                          <span>${newBalance.toFixed(2)}</span>
                        </div>
                      </div>
                    );
                  })()}
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
};

export default CreditCardManagement;
