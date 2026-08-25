import React, { useState, useEffect } from 'react';
import { CustomerProfile, SpendingSummary, Transaction, ChatMessage } from './types';
import { bankingApi } from './services/api';
import { Navbar } from './components/Navbar';
import { DashboardOverview } from './components/DashboardOverview';
import { ChatDrawer } from './components/ChatDrawer';
import { SpendingInsights } from './components/SpendingInsights';
import { SafetyTestBench } from './components/SafetyTestBench';

export const App: React.FC = () => {
  const [activeTab, setActiveTab] = useState('dashboard');
  const [profile, setProfile] = useState<CustomerProfile | null>(null);
  const [summary, setSummary] = useState<SpendingSummary | null>(null);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: 'welcome-1',
      sender: 'ASSISTANT',
      text: 'Hello Oliver! I am **FinAssist**, your personal GenAI banking assistant. I can analyze your transactions, explain spending patterns (e.g. *Why did my spending increase?*), list recurring subscriptions, or answer banking policy questions.',
      timestamp: new Date().toISOString()
    }
  ]);
  const [isLoading, setIsLoading] = useState(false);

  const loadData = async () => {
    try {
      await bankingApi.login('oliver', 'Password123!');
      const [profData, sumData, txData] = await Promise.all([
        bankingApi.getProfile(),
        bankingApi.getSpendingSummary(),
        bankingApi.getTransactions()
      ]);
      setProfile(profData);
      setSummary(sumData);
      setTransactions(txData);
    } catch (e) {
      console.error('Data load error:', e);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleSendMessage = async (userMsgText: string) => {
    const userMsg: ChatMessage = {
      id: `user-${Date.now()}`,
      sender: 'USER',
      text: userMsgText,
      timestamp: new Date().toISOString()
    };
    setMessages(prev => [...prev, userMsg]);
    setIsLoading(true);

    try {
      const assistantMsg = await bankingApi.sendMessage(userMsgText);
      setMessages(prev => [...prev, assistantMsg]);
    } catch (error) {
      console.error('Chat error:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleOpenChatWithQuery = (query: string) => {
    setActiveTab('chat');
    handleSendMessage(query);
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col">
      <Navbar
        profile={profile}
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        onRefresh={loadData}
      />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {activeTab === 'dashboard' && (
          <DashboardOverview
            profile={profile}
            summary={summary}
            transactions={transactions}
            onOpenChatWithQuery={handleOpenChatWithQuery}
          />
        )}

        {activeTab === 'chat' && (
          <ChatDrawer
            messages={messages}
            onSendMessage={handleSendMessage}
            isLoading={isLoading}
          />
        )}

        {activeTab === 'spending' && (
          <SpendingInsights summary={summary} />
        )}

        {activeTab === 'guardrails' && (
          <SafetyTestBench />
        )}
      </main>

      <footer className="border-t border-slate-800/80 bg-slate-950 py-4 text-center text-xs text-slate-400">
        <p>
          FinAssist — Educational Portfolio Project with Synthetic Banking Data • Java 21 Spring Boot 3 &amp; Python FastAPI RAG
        </p>
      </footer>
    </div>
  );
};

export default App;
