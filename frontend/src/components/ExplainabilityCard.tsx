import React from 'react';
import { ExplainabilityDTO } from '../types';
import { ShieldCheck, Database, BookOpen, CheckCircle2, TrendingUp, TrendingDown } from 'lucide-react';

interface ExplainabilityCardProps {
  explainability?: ExplainabilityDTO;
}

export const ExplainabilityCard: React.FC<ExplainabilityCardProps> = ({ explainability }) => {
  if (!explainability) return null;

  return (
    <div className="mt-3 p-3.5 bg-slate-900/90 rounded-xl border border-slate-800 text-xs space-y-3 shadow-inner">
      {/* Header */}
      <div className="flex items-center justify-between border-b border-slate-800 pb-2">
        <div className="flex items-center space-x-1.5 text-emerald-400 font-semibold">
          <Database className="h-3.5 w-3.5" />
          <span>Factual Grounding & Attribution</span>
        </div>
        <div className="flex items-center space-x-2">
          <span className="px-2 py-0.5 bg-emerald-500/10 text-emerald-300 border border-emerald-500/30 rounded font-mono text-[10px]">
            Intent: {explainability.intent_detected} ({Math.round(explainability.intent_confidence * 100)}%)
          </span>
        </div>
      </div>

      {/* Data Points Used */}
      {explainability.data_points_used && explainability.data_points_used.length > 0 && (
        <div>
          <p className="text-[11px] font-medium text-slate-400 mb-1 flex items-center gap-1">
            <CheckCircle2 className="h-3 w-3 text-emerald-400" /> Underlying Data Points Used:
          </p>
          <ul className="space-y-1 pl-4 list-disc text-slate-300 font-mono text-[11px]">
            {explainability.data_points_used.map((dp, idx) => (
              <li key={idx}>{dp}</li>
            ))}
          </ul>
        </div>
      )}

      {/* Variance Breakdown */}
      {explainability.variance_breakdown && explainability.variance_breakdown.length > 0 && (
        <div>
          <p className="text-[11px] font-medium text-slate-400 mb-1.5">Category Variance Decomposition:</p>
          <div className="grid grid-cols-2 sm:grid-cols-3 gap-1.5">
            {explainability.variance_breakdown.slice(0, 6).map((item, i) => {
              const isIncrease = item.delta_amount >= 0;
              return (
                <div key={i} className="p-1.5 bg-slate-800/80 rounded border border-slate-700/60 flex items-center justify-between">
                  <span className="text-slate-300 truncate max-w-[80px]">{item.category}</span>
                  <span className={`font-mono font-medium flex items-center text-[10px] ${isIncrease ? 'text-rose-400' : 'text-emerald-400'}`}>
                    {isIncrease ? <TrendingUp className="h-2.5 w-2.5 mr-0.5" /> : <TrendingDown className="h-2.5 w-2.5 mr-0.5" />}
                    {isIncrease ? '+' : ''}£{Math.abs(item.delta_amount).toFixed(2)}
                  </span>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* Retrieved FAQ Sources */}
      {explainability.retrieved_faq_sources && explainability.retrieved_faq_sources.length > 0 && (
        <div>
          <p className="text-[11px] font-medium text-slate-400 mb-1 flex items-center gap-1">
            <BookOpen className="h-3 w-3 text-blue-400" /> Verified RAG Policy Sources:
          </p>
          <div className="space-y-1.5">
            {explainability.retrieved_faq_sources.map((src, i) => (
              <div key={i} className="p-2 bg-blue-950/20 border border-blue-800/40 rounded-lg">
                <div className="flex items-center justify-between">
                  <span className="font-medium text-blue-300">{src.title}</span>
                  <span className="text-[10px] text-blue-400 font-mono">
                    Match: {Math.round(src.similarity_score * 100)}% ({src.doc_id})
                  </span>
                </div>
                <p className="text-[10px] text-slate-400 mt-0.5 line-clamp-2">{src.content_snippet}</p>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Guardrails Verification Telemetry */}
      {explainability.guardrail_checks && (
        <div className="pt-2 border-t border-slate-800 flex flex-wrap gap-1.5">
          {Object.entries(explainability.guardrail_checks).map(([guard, stat], i) => (
            <span
              key={i}
              className={`text-[9px] px-2 py-0.5 rounded font-mono border flex items-center gap-1 ${
                stat.startsWith('PASSED')
                  ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
                  : 'bg-amber-500/10 text-amber-300 border-amber-500/30'
              }`}
            >
              <ShieldCheck className="h-2.5 w-2.5" />
              {guard}: {stat.split(' ')[0]}
            </span>
          ))}
        </div>
      )}
    </div>
  );
};
