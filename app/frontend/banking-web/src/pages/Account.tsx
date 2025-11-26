
import { useEffect, useState } from "react";
import { Card } from "@/components/ui/card";
import { bffApi } from "@/mocks/bffApi";
import { Account as AccountModel } from "@/models/Account";
export default function Account() {
  const [accounts, setAccounts] = useState<AccountModel[]>([]);

  useEffect(() => {
    bffApi.getAccounts().then(setAccounts);
  }, []);

  const account = accounts[0];

  return (
    <div className="p-8 max-w-6xl mx-auto space-y-8">
      <h1 className="text-3xl font-bold mb-2 text-slate-900">Account Overview</h1>
      <p className="text-slate-600 mb-6 text-base">Access your essential account details, codes, agreements, and policy information. For any questions, please contact <a href="mailto:support@bankwise.com" className="text-blue-600 underline">support@bankwise.com</a>.</p>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        {/* General Information */}
        <Card className="p-8 shadow-xl border border-slate-200 flex-1">
          <section>
            <h2 className="text-xl font-semibold mb-2 text-slate-800 flex items-center gap-2">
              General Information
            </h2>
            <div className="text-slate-700 text-base leading-relaxed">
              <div className="mb-2"><span className="font-medium">Account Holder:</span> {account?.name}</div>
              <div className="mb-2"><span className="font-medium">Account Type:</span> {account?.type}</div>
              <div className="mb-2"><span className="font-medium">Status:</span> <span className="text-green-600 font-semibold">{account?.status}</span></div>
              <div><span className="font-medium">Opened:</span> January 15, 2022</div>
            </div>
          </section>
        </Card>
        {/* Account Codes */}
        <Card className="p-8 shadow-xl border border-slate-200 flex-1">
          <section>
            <h2 className="text-xl font-semibold mb-2 text-slate-800 flex items-center gap-2">
              Account Codes
            </h2>
            <div className="grid grid-cols-1 gap-4 text-slate-700">
              <div><span className="font-medium">Account Number:</span> <span className="font-mono tracking-wider">1234 5678 9012 3456</span></div>
              <div><span className="font-medium">SWIFT Code:</span> <span className="font-mono tracking-wider">BWAIUS33</span></div>
              <div><span className="font-medium">IBAN:</span> <span className="font-mono tracking-wider">US00BWAI12345678901234</span></div>
              <div><span className="font-medium">Routing Number:</span> <span className="font-mono tracking-wider">021000021</span></div>
            </div>
          </section>
        </Card>
        {/* Agreements */}
        <Card className="p-8 shadow-xl border border-slate-200 flex-1">
          <section>
            <h2 className="text-xl font-semibold mb-2 text-slate-800 flex items-center gap-2">
              Agreements
            </h2>
            <ul className="list-disc ml-6 text-slate-700 space-y-2">
              <li>
                <a href="#" className="text-blue-600 underline font-medium">Terms of Service</a> — Outlines your rights and responsibilities as an account holder.
              </li>
              <li>
                <a href="#" className="text-blue-600 underline font-medium">Electronic Communications Agreement</a> — Details how we deliver important information electronically.
              </li>
              <li>
                <a href="#" className="text-blue-600 underline font-medium">Fee Schedule</a> — Provides information on account-related fees.
              </li>
            </ul>
          </section>
        </Card>
        {/* Policy */}
        <Card className="p-8 shadow-xl border border-slate-200 flex-1">
          <section>
            <h2 className="text-xl font-semibold mb-2 text-slate-800 flex items-center gap-2">
              Privacy & Security Policy
            </h2>
            <div className="text-slate-700 text-base leading-relaxed">
              <p className="mb-2">Your privacy and security are our top priorities. We use industry-leading encryption and security practices to protect your data and financial information.</p>
              <p className="mb-2">We do <span className="font-semibold">not</span> share your information with third parties without your explicit consent. You can review our full policy below:</p>
              <a href="#" className="text-blue-600 underline font-medium">View Privacy Policy</a>
            </div>
          </section>
        </Card>
      </div>
    </div>
  );
}
