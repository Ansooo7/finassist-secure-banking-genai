import React from 'react';
import { CustomerProfile, SpendingSummary, Transaction } from '../types';
import {
  Wallet,
  TrendingUp,
  TrendingDown,
  CreditCard,
  ArrowUpRight,
  ArrowDownLeft,
  Calendar,
  Sparkles,
  ShieldCheck,
  Tag
} from 'lucide-react';

interface DashboardOverviewProps {
  profile: CustomerProfile | null;
  summary: SpendingSummary | null;
  transactions: Transaction[];
  onOpenChatWithQuery: (q: string) => void;
}

export const DashboardOverview: React.FC<DashboardOverviewProps> = ({
  profile,
  summary,
  transactions,
  onOpenChatWithQuery
}) => {
  const currencySymbol = profile?.currency === 'GBP' ? '£' : '$';

  return (
    <div className="space-y-6">
      {/* Top Banner Alert */}
      <div className="bg-gradient-to-r from-slate-900 via-slate-800 to-slate-900 border border-slate-700/60 p-4 rounded-2xl flex flex-col md:flex-row items-start md:items-center justify-between gap-4 shadow-lg">
        <div className="flex items-center space-x-3">
          <div className="p-2.5 bg-emerald-500/10 rounded-xl border border-emerald-500/30 text-emerald-400">
            <Sparkles className="h-6 w-6" />
          </div>
          <div>
            <h2 className="text-base font-semibold text-slate-100">
              Welcome back, {profile?.firstName || 'Oliver'}!
            </h2>
            <p className="text-xs text-slate-400">
              Your August spending increased by {summary?.percentageChange || 31.9}% (+{currencySymbol}{summary?.spendDelta?.toFixed(2) || '688.50'}). FinAssist is ready to analyze.
            </p>
          </div>
        </div>
        <button
          onClick={() => onOpenChatWithQuery('Why did my spending increase?')}
          className="px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-xs font-semibold flex items-center space-x-2 transition shadow-lg shadow-emerald-600/20"
        >
          <Sparkles className="h-4 w-4" />
          <span>Ask AI: Why Did Spending Increase?</span>
        </button>
      </div>

      {/* Metric Cards Grid */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        {/* Total Balance */}
        <div className="bg-slate-900 border border-slate-800 p-5 rounded-2xl shadow-md">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-medium">Available Balance</span>
            <Wallet className="h-4 w-4 text-emerald-400" />
          </div>
          <div className="text-2xl font-bold text-slate-100 font-mono">
            {currencySymbol}{profile?.totalBalance ? profile.totalBalance.toLocaleString('en-GB', { minimumFractionDigits: 2 }) : '15,420.50'}
          </div>
          <div className="text-[11px] text-slate-400 mt-2 flex items-center gap-1.5 font-mono">
            <CreditCard className="h-3 w-3" />
            <span>Acc: {profile?.accounts[0]?.accountNumber || '12345678'} ({profile?.accounts[0]?.sortCode || '20-45-14'})</span>
          </div>
        </div>

        {/* Current Month Spend */}
        <div className="bg-slate-900 border border-slate-800 p-5 rounded-2xl shadow-md">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-medium">August 2026 Spend</span>
            <TrendingUp className="h-4 w-4 text-rose-400" />
          </div>
          <div className="text-2xl font-bold text-slate-100 font-mono">
            {currencySymbol}{summary?.currentMonthSpend ? summary.currentMonthSpend.toLocaleString('en-GB', { minimumFractionDigits: 2 }) : '2,843.98'}
          </div>
          <div className="text-[11px] text-rose-400 mt-2 flex items-center gap-1 font-medium">
            <TrendingUp className="h-3 w-3" />
            <span>+{summary?.percentageChange || 31.9}% vs July</span>
          </div>
        </div>

        {/* Previous Month Spend */}
        <div className="bg-slate-900 border border-slate-800 p-5 rounded-2xl shadow-md">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-medium">July 2026 Spend</span>
            <Calendar className="h-4 w-4 text-blue-400" />
          </div>
          <div className="text-2xl font-bold text-slate-100 font-mono">
            {currencySymbol}{summary?.previousMonthSpend ? summary.previousMonthSpend.toLocaleString('en-GB', { minimumFractionDigits: 2 }) : '2,155.48'}
          </div>
          <div className="text-[11px] text-slate-400 mt-2">
            Baseline comparison month
          </div>
        </div>

        {/* Top Spending Category */}
        <div className="bg-slate-900 border border-slate-800 p-5 rounded-2xl shadow-md">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-medium">Top Spending Category</span>
            <Tag className="h-4 w-4 text-amber-400" />
          </div>
          <div className="text-2xl font-bold text-amber-300">
            {summary?.topCategory || 'Dining'}
          </div>
          <div className="text-[11px] text-slate-400 mt-2 font-mono">
            {currencySymbol}{summary?.topCategoryAmount?.toFixed(2) || '590.00'} total
          </div>
        </div>
      </div>

      {/* Main Content Grid: Transactions & Quick Insights */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Recent Transactions List (2 Cols) */}
        <div className="lg:col-span-2 bg-slate-900 border border-slate-800 rounded-2xl p-5 shadow-md">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="font-semibold text-slate-100 text-sm">Recent Transactions (Isolated Context)</h3>
              <p className="text-xs text-slate-400">Strictly filtered to authenticated customer records</p>
            </div>
            <span className="text-xs font-mono bg-slate-800 px-2.5 py-1 rounded text-slate-300 border border-slate-700">
              {transactions.length} Records
            </span>
          </div>

          <div className="divide-y divide-slate-800/80">
            {transactions.slice(0, 7).map((t) => {
              const isCredit = t.direction === 'CREDIT';
              return (
                <div key={t.id} className="py-3.5 flex items-center justify-between hover:bg-slate-800/30 px-2 rounded-lg transition">
                  <div className="flex items-center space-x-3">
                    <div
                      className={`h-9 w-9 rounded-xl flex items-center justify-center ${
                        isCredit
                          ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                          : 'bg-slate-800 text-slate-300 border border-slate-700'
                      }`}
                    >
                      {isCredit ? <ArrowDownLeft className="h-4 w-4" /> : <ArrowUpRight className="h-4 w-4" />}
                    </div>
                    <div>
                      <p className="text-sm font-medium text-slate-200">{t.merchantName}</p>
                      <div className="flex items-center space-x-2 text-[11px] text-slate-400">
                        <span className="px-1.5 py-0.2 bg-slate-800 rounded text-slate-300 border border-slate-700/60 font-mono">
                          {t.category}
                        </span>
                        <span>•</span>
                        <span>{new Date(t.transactionTime).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })}</span>
                        {t.isRecurring && (
                          <>
                            <span>•</span>
                            <span className="text-purple-400 font-mono">Recurring</span>
                          </>
                        )}
                      </div>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className={`font-mono font-semibold text-sm ${isCredit ? 'text-emerald-400' : 'text-slate-100'}`}>
                      {isCredit ? '+' : '-'}{currencySymbol}{t.amount.toFixed(2)}
                    </p>
                    <p className="text-[10px] text-slate-500 uppercase">{t.currency}</p>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* AI Quick Prompts Card (1 Col) */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 shadow-md flex flex-col justify-between">
          <div>
            <div className="flex items-center space-x-2 text-emerald-400 mb-3">
              <Sparkles className="h-5 w-5" />
              <h3 className="font-semibold text-sm">Natural Language Analytics</h3>
            </div>
            <p className="text-xs text-slate-400 mb-4 leading-relaxed">
              Ask FinAssist complex questions about your synthetic spending, policy limits, or subscription trends:
            </p>

            <div className="space-y-2">
              {[
                { title: "Spending Increase", query: "Why did my spending increase?" },
                { title: "Highest Expense", query: "What category did I spend the most on?" },
                { title: "Recurring Subscriptions", query: "Show my recurring expenses" },
                { title: "Contactless Limit Policy", query: "What is the daily contactless limit?" },
                { title: "Fraud Dispute Policy", query: "How do I report a fraudulent transaction?" }
              ].map((item, idx) => (
                <button
                  key={idx}
                  onClick={() => onOpenChatWithQuery(item.query)}
                  className="w-full text-left p-2.5 rounded-xl bg-slate-800/80 hover:bg-slate-800 border border-slate-700/60 hover:border-emerald-500/40 text-xs text-slate-200 transition group flex items-center justify-between"
                >
                  <span>{item.query}</span>
                  <ArrowUpRight className="h-3.5 w-3.5 text-slate-500 group-hover:text-emerald-400 transition" />
                </button>
              ))}
            </div>
          </div>

          <div className="mt-6 p-3 bg-emerald-950/20 border border-emerald-800/30 rounded-xl flex items-center space-x-2 text-[11px] text-emerald-300">
            <ShieldCheck className="h-4 w-4 flex-shrink-0" />
            <span>Zero-hallucination guarantee: All responses are verified against SQL telemetry and RAG FAQs.</span>
          </div>
        </div>
      </div>
    </div>
  );
};
