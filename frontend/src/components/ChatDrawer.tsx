import React, { useState, useRef, useEffect } from 'react';
import { Send, Bot, User, Sparkles, Shield, ChevronDown, ChevronUp, Loader2 } from 'lucide-react';
import { ChatMessage } from '../types';
import { ExplainabilityCard } from './ExplainabilityCard';

interface ChatDrawerProps {
  messages: ChatMessage[];
  onSendMessage: (msg: string) => Promise<void>;
  isLoading: boolean;
}

const PRESET_QUERIES = [
  "Why did my spending increase?",
  "How much did I spend this month?",
  "What category did I spend the most on?",
  "What is the daily contactless limit?",
  "Show my recurring expenses"
];

export const ChatDrawer: React.FC<ChatDrawerProps> = ({
  messages,
  onSendMessage,
  isLoading
}) => {
  const [input, setInput] = useState('');
  const [expandedExplain, setExpandedExplain] = useState<Record<string, boolean>>({});
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, isLoading]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!input.trim() || isLoading) return;
    const query = input;
    setInput('');
    await onSendMessage(query);
  };

  const toggleExplain = (id: string) => {
    setExpandedExplain(prev => ({ ...prev, [id]: !prev[id] }));
  };

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-2xl flex flex-col h-[650px] shadow-2xl overflow-hidden">
      {/* Chat Header */}
      <div className="p-4 border-b border-slate-800 bg-slate-900/90 flex items-center justify-between">
        <div className="flex items-center space-x-2.5">
          <div className="h-8 w-8 rounded-lg bg-emerald-500/20 text-emerald-400 flex items-center justify-center border border-emerald-500/30">
            <Bot className="h-5 w-5" />
          </div>
          <div>
            <h3 className="font-semibold text-slate-100 text-sm">FinAssist GenAI Copilot</h3>
            <p className="text-[11px] text-slate-400">Grounded in transaction analytics &amp; banking policy RAG</p>
          </div>
        </div>
        <div className="flex items-center space-x-1 text-xs text-emerald-400 font-mono bg-emerald-500/10 px-2 py-0.5 rounded border border-emerald-500/20">
          <Shield className="h-3 w-3" />
          <span>FCA Guardrails Active</span>
        </div>
      </div>

      {/* Preset Quick Chips */}
      <div className="p-2.5 bg-slate-950/60 border-b border-slate-800/80 flex items-center gap-1.5 overflow-x-auto no-scrollbar">
        <span className="text-[11px] text-slate-500 flex items-center gap-1 pl-1 whitespace-nowrap">
          <Sparkles className="h-3 w-3 text-emerald-400" /> Suggestions:
        </span>
        {PRESET_QUERIES.map((q, i) => (
          <button
            key={i}
            onClick={() => onSendMessage(q)}
            disabled={isLoading}
            className="text-xs px-2.5 py-1 bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white rounded-full border border-slate-700/60 transition whitespace-nowrap"
          >
            {q}
          </button>
        ))}
      </div>

      {/* Messages Scroll Area */}
      <div className="flex-1 overflow-y-auto p-4 space-y-4">
        {messages.map((msg) => {
          const isUser = msg.sender === 'USER';
          const isBlocked = msg.guardrailStatus === 'INJECTION_BLOCKED';
          const isRefused = msg.guardrailStatus === 'ADVICE_REFUSED';
          const isExpanded = expandedExplain[msg.id] ?? true;

          return (
            <div key={msg.id} className={`flex flex-col ${isUser ? 'items-end' : 'items-start'}`}>
              <div className="flex items-start space-x-2 max-w-[88%]">
                {!isUser && (
                  <div className="h-7 w-7 rounded-lg bg-emerald-500/20 text-emerald-400 flex items-center justify-center border border-emerald-500/30 flex-shrink-0 mt-0.5">
                    <Bot className="h-4 w-4" />
                  </div>
                )}
                
                <div
                  className={`p-3.5 rounded-2xl text-sm leading-relaxed ${
                    isUser
                      ? 'bg-emerald-600 text-white rounded-br-none shadow-md'
                      : isBlocked
                      ? 'bg-rose-950/40 text-rose-200 border border-rose-800/60 rounded-bl-none'
                      : isRefused
                      ? 'bg-amber-950/40 text-amber-200 border border-amber-800/60 rounded-bl-none'
                      : 'bg-slate-800 text-slate-100 rounded-bl-none border border-slate-700/60 shadow-md'
                  }`}
                >
                  <div className="whitespace-pre-line">{msg.text}</div>

                  {/* Explainability Toggle */}
                  {!isUser && msg.explainability && (
                    <div className="mt-2 pt-2 border-t border-slate-700/50 flex items-center justify-between">
                      <button
                        onClick={() => toggleExplain(msg.id)}
                        className="text-[11px] font-medium text-emerald-400 hover:text-emerald-300 flex items-center space-x-1"
                      >
                        <span>Why this answer? (Explainability &amp; Grounding)</span>
                        {isExpanded ? <ChevronUp className="h-3 w-3" /> : <ChevronDown className="h-3 w-3" />}
                      </button>
                      <span className="text-[10px] text-slate-400 font-mono">
                        {new Date(msg.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </span>
                    </div>
                  )}

                  {/* Embedded Explainability Card */}
                  {!isUser && isExpanded && msg.explainability && (
                    <ExplainabilityCard explainability={msg.explainability} />
                  )}
                </div>

                {isUser && (
                  <div className="h-7 w-7 rounded-lg bg-slate-700 text-slate-300 flex items-center justify-center border border-slate-600 flex-shrink-0 mt-0.5">
                    <User className="h-4 w-4" />
                  </div>
                )}
              </div>
            </div>
          );
        })}

        {isLoading && (
          <div className="flex items-start space-x-2">
            <div className="h-7 w-7 rounded-lg bg-emerald-500/20 text-emerald-400 flex items-center justify-center border border-emerald-500/30">
              <Bot className="h-4 w-4" />
            </div>
            <div className="bg-slate-800 p-3 rounded-2xl rounded-bl-none border border-slate-700 flex items-center space-x-2 text-slate-400 text-xs">
              <Loader2 className="h-4 w-4 animate-spin text-emerald-400" />
              <span>Analyzing transactions &amp; verifying safety guardrails...</span>
            </div>
          </div>
        )}

        <div ref={messagesEndRef} />
      </div>

      {/* Chat Input */}
      <form onSubmit={handleSubmit} className="p-3 bg-slate-900 border-t border-slate-800 flex items-center space-x-2">
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="Ask anything (e.g. 'Why did I spend more this month?', 'What is my contactless limit?')..."
          disabled={isLoading}
          className="flex-1 bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-slate-100 placeholder-slate-500 focus:outline-none focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500 transition"
        />
        <button
          type="submit"
          disabled={isLoading || !input.trim()}
          className="bg-emerald-600 hover:bg-emerald-500 disabled:opacity-50 text-white px-4 py-2.5 rounded-xl transition flex items-center justify-center shadow-lg shadow-emerald-600/20"
        >
          <Send className="h-4 w-4" />
        </button>
      </form>
    </div>
  );
};
