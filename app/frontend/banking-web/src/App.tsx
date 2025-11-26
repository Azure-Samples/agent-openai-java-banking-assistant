import type { ReactNode } from "react";
import {
  BrowserRouter,
  Navigate,
  Outlet,
  Route,
  Routes,
  useLocation,
} from "react-router-dom";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { Toaster } from "@/components/ui/toaster";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import Sidebar from "./components/Sidebar";
import Navigation from "./components/Navigation";
import AIAgent from "./components/AIAgent";
import Dashboard from "./pages/Dashboard";
import Payments from "./pages/Payments";
import CreditCardManagement from "./pages/CreditCardManagement";
import InvestmentPortfolio from "./pages/InvestmentPortfolio";
import TransactionAnalytics from "./pages/TransactionAnalytics";
import Account from "./pages/Account";
import Support from "./pages/Support";
import NotFound from "./pages/NotFound";
import Login from "./pages/Login";
import { AuthProvider, useAuth } from "@/context/AuthContext";
import { AgentResponseProvider } from "@/context/AgentResponseContext";

const queryClient = new QueryClient();

const ProtectedShell = () => (
  <AgentResponseProvider>
    <div className="min-h-screen bg-background flex flex-col w-full">
      <Navigation />
      <div className="flex flex-1">
        <Sidebar />
        <main className="flex-1 overflow-auto">
          <Outlet />
        </main>
      </div>
      <AIAgent />
    </div>
  </AgentResponseProvider>
);

const RequireAuth = ({ children }: { children: ReactNode }) => {
  const { user, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background">
        <p className="text-base text-slate-500">Restoring your workspace…</p>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return <>{children}</>;
};

const App = () => (
  <QueryClientProvider client={queryClient}>
    <TooltipProvider>
      <Toaster />
      <Sonner />
      <BrowserRouter>
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route
              element={
                <RequireAuth>
                  <ProtectedShell />
                </RequireAuth>
              }
            >
              <Route index element={<Dashboard />} />
              <Route path="payments" element={<Payments />} />
              <Route path="credit-cards" element={<CreditCardManagement />} />
              <Route path="portfolio" element={<InvestmentPortfolio />} />
              <Route path="analytics" element={<TransactionAnalytics />} />
              <Route path="account" element={<Account />} />
              <Route path="support" element={<Support />} />
              <Route path="*" element={<NotFound />} />
            </Route>
          </Routes>
        </AuthProvider>
      </BrowserRouter>
    </TooltipProvider>
  </QueryClientProvider>
);

export default App;
