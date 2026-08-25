import React from 'react';
import { Bot, ShieldCheck, Sparkles, UserCheck } from 'lucide-react';
import { CustomerProfile } from '../types';

interface NavbarProps {
  profile: CustomerProfile | null;
  activeTab: string;
  setActiveTab: (tab: string) => void;
  onRefresh: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({
  profile,
  activeTab,
  setActiveTab
}) => {
  return (
    <header className="bg-slate-900/80 backdrop-blur border-b border-slate-800 sticky top-0 z-40">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo & Brand */}
          <div className="flex items-center space-x-3">
            <div className="h-10 w-10 rounded-xl bg-gradient-to-tr from-emerald-600 to-teal-400 flex items-center justify-center shadow-lg shadow-emerald-500/20">
              <Bot className="h-6 w-6 text-white" />
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <span className="text-lg font-bold bg-gradient-to-r from-white via-slate-100 to-slate-400 bg-clip-text text-transparent">
                  FinAssist
                </span>
                <span className="px-2 py-0.5 text-[10px] font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/30 rounded-full flex items-center gap-1">
                  <ShieldCheck className="h-3 w-3" /> GenAI Guarded
                </span>
              </div>
              <p className="text-xs text-slate-400">Secure Personal Banking Assistant</p>
            </div>
          </div>

          {/* Navigation Tabs */}
          <nav className="hidden md:flex items-center space-x-1">
            {[
              { id: 'dashboard', label: 'Overview' },
              { id: 'chat', label: 'AI Assistant', icon: Sparkles },
              { id: 'spending', label: 'Spending Insights' },
              { id: 'guardrails', label: 'Safety Sandbox', badge: 'Defense' },
            ].map((tab) => {
              const isActive = activeTab === tab.id;
              return (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`px-3.5 py-2 rounded-lg text-sm font-medium transition-all flex items-center space-x-2 ${
                    isActive
                      ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/30'
                      : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
                  }`}
                >
                  {tab.icon && <tab.icon className="h-4 w-4" />}
                  <span>{tab.label}</span>
                  {tab.badge && (
                    <span className="text-[10px] px-1.5 py-0.2 bg-purple-500/20 text-purple-300 border border-purple-500/30 rounded font-mono">
                      {tab.badge}
                    </span>
                  )}
                </button>
              );
            })}
          </nav>

          {/* Customer Profile Status */}
          <div className="flex items-center space-x-3">
            <div className="flex items-center space-x-2 bg-slate-800/60 border border-slate-700/60 px-3 py-1.5 rounded-lg">
              <UserCheck className="h-4 w-4 text-emerald-400" />
              <div className="text-left">
                <p className="text-xs font-medium text-slate-200">{profile?.fullName || 'Oliver Twist'}</p>
                <p className="text-[10px] text-slate-400 font-mono">{profile?.customerNumber || 'CUST-UK-1001'}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </header>
  );
};
