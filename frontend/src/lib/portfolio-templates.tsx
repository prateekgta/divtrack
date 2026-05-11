'use client';

import { useState, useEffect } from 'react';
import { marketApi, portfolioApi } from './api';
import { useToast } from './ui';

interface Template {
  id: string; name: string; description: string;
  riskLevel: string; focusArea: string; budgetLabel: string;
  defaultBudget: number; holdingCount: number;
  estimatedYieldPct: number; estimatedMonthlyIncome: number;
  allocations: Allocation[];
}

interface Allocation {
  ticker: string; name: string; allocationPct: number;
  amount: number; shares: number; price: number;
  yieldPct: number; reason: string;
}

const RISK_COLORS: Record<string, string> = {
  'Low': 'bg-green-100 text-green-700',
  'Low-Medium': 'bg-leaf-100 text-leaf-700',
  'Medium': 'bg-amber-100 text-amber-700',
  'High': 'bg-orange-100 text-orange-700',
  'Very High': 'bg-red-100 text-red-700',
};

const FOCUS_ICONS: Record<string, string> = {
  'Income': '💰',
  'Growth + Income': '⚖️',
  'Dividend Growth': '🌱',
  'Growth': '🚀',
  'Monthly Income': '📅',
  'Crypto': '₿',
};

export function PortfolioTemplates({ onComplete }: { onComplete?: () => void }) {
  const { toast } = useToast();
  const [templates, setTemplates] = useState<Template[]>([]);
  const [loading, setLoading] = useState(true);
  const [expanded, setExpanded] = useState<string | null>(null);
  const [adopting, setAdopting] = useState<string | null>(null);
  const [budgets, setBudgets] = useState<Record<string, number>>({});

  useEffect(() => {
    marketApi.getTemplates().then(({ data }) => {
      setTemplates(data);
      const b: Record<string, number> = {};
      data.forEach((t: Template) => { b[t.id] = t.defaultBudget; });
      setBudgets(b);
      setLoading(false);
    }).catch(() => setLoading(false));
  }, []);

  const adoptTemplate = async (t: Template) => {
    setAdopting(t.id);
    const budget = budgets[t.id] || t.defaultBudget;
    try {
      const { data: detail } = await marketApi.getTemplate(t.id, budget);
      if (!detail?.allocations) { toast('Failed to load template details', 'error'); return; }

      const added: string[] = [];
      const failed: string[] = [];
      for (const a of detail.allocations) {
        if (a.amount <= 0) continue;
        const shares = (a.amount / a.price);
        try {
          await portfolioApi.addHolding({ ticker: a.ticker, shares: parseFloat(shares.toFixed(4)) });
          added.push(a.ticker);
        } catch {
          failed.push(a.ticker);
        }
      }

      if (added.length > 0) toast(`Adopted portfolio: ${added.join(', ')}`);
      if (failed.length > 0) toast(`Failed to add: ${failed.join(', ')}`, 'error');
      onComplete?.();
    } catch {
      toast('Failed to adopt template', 'error');
    }
    setAdopting(null);
  };

  if (loading) return (
    <div className="space-y-3">
      {[1,2,3].map(i => <div key={i} className="h-28 rounded-2xl bg-earth-100/50 animate-pulse" />)}
    </div>
  );

  if (templates.length === 0) return null;

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-lg font-bold text-forest-800">Portfolio Templates</h2>
          <p className="text-sm text-earth-500">Pre-built portfolios powered by live market data</p>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
        {templates.map(t => {
          const isExpanded = expanded === t.id;
          const isAdopting = adopting === t.id;
          return (
            <div key={t.id}
              className={`bg-white rounded-2xl border transition-all duration-200 cursor-pointer ${
                isExpanded ? 'border-forest-300 shadow-md ring-1 ring-forest-200' : 'border-earth-100 hover:border-earth-200 hover:shadow-sm'
              }`}
              onClick={() => setExpanded(isExpanded ? null : t.id)}>
              <div className="p-5">
                <div className="flex items-start justify-between mb-3">
                  <div className="flex items-center gap-2">
                    <span className="text-xl">{FOCUS_ICONS[t.focusArea] || '📋'}</span>
                    <div>
                      <p className="font-semibold text-earth-800 text-sm">{t.name}</p>
                      <span className={`inline-block text-[10px] font-medium px-2 py-0.5 rounded-full ${RISK_COLORS[t.riskLevel] || 'bg-earth-100 text-earth-600'}`}>
                        {t.riskLevel} Risk
                      </span>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className="font-bold text-forest-700 text-lg">${t.budgetLabel}</p>
                    <p className="text-[10px] text-earth-400">{t.holdingCount} holdings</p>
                  </div>
                </div>

                <div className="grid grid-cols-3 gap-2 mb-3">
                  <div className="bg-forest-50/60 rounded-xl p-2.5 text-center">
                    <p className="text-[10px] text-earth-500">Yield</p>
                    <p className="font-bold text-forest-700 text-sm">{(t.estimatedYieldPct * 100).toFixed(2)}%</p>
                  </div>
                  <div className="bg-leaf-50/60 rounded-xl p-2.5 text-center">
                    <p className="text-[10px] text-earth-500">Monthly</p>
                    <p className="font-bold text-leaf-700 text-sm">${t.estimatedMonthlyIncome.toFixed(2)}</p>
                  </div>
                  <div className="bg-amber-50/60 rounded-xl p-2.5 text-center">
                    <p className="text-[10px] text-earth-500">Annual</p>
                    <p className="font-bold text-amber-700 text-sm">${(t.estimatedMonthlyIncome * 12).toFixed(0)}</p>
                  </div>
                </div>

                {isExpanded && (
                  <div className="space-y-3 pt-2 border-t border-earth-100" onClick={e => e.stopPropagation()}>
                    <p className="text-xs text-earth-500 leading-relaxed">{t.description}</p>

                    <div className="space-y-1.5 max-h-48 overflow-y-auto pr-1">
                      {t.allocations.map(a => (
                        <div key={a.ticker}
                          className="flex items-center gap-3 p-2 rounded-xl bg-earth-50/50 hover:bg-forest-50/50 transition-colors">
                          <div className="w-7 h-7 rounded-lg bg-gradient-to-br from-forest-400 to-forest-600 flex items-center justify-center text-white text-[10px] font-bold shrink-0">
                            {Math.round(a.allocationPct)}%
                          </div>
                          <div className="flex-1 min-w-0">
                            <div className="flex items-center gap-2">
                              <p className="font-semibold text-earth-800 text-xs">{a.ticker}</p>
                              <span className="text-[10px] text-earth-400">{a.name}</span>
                            </div>
                            <p className="text-[10px] text-earth-500 truncate">{a.reason}</p>
                          </div>
                          <div className="text-right shrink-0">
                            <p className="text-xs font-semibold text-earth-800">${a.amount.toLocaleString()}</p>
                            <p className="text-[10px] text-earth-400">{a.yieldPct.toFixed(2)}%</p>
                          </div>
                        </div>
                      ))}
                    </div>

                    <div className="flex items-center gap-3 pt-2">
                      <div className="flex-1">
                        <label className="block text-[10px] font-medium text-earth-500 mb-1">Budget</label>
                        <div className="relative">
                          <span className="absolute left-2.5 top-1/2 -translate-y-1/2 text-earth-400 text-xs">$</span>
                          <input type="number" step="1000" min="1000"
                            className="input-field pl-6 py-1.5 text-sm"
                            value={budgets[t.id] || t.defaultBudget}
                            onChange={e => setBudgets(p => ({ ...p, [t.id]: parseInt(e.target.value) || t.defaultBudget }))}
                            onClick={e => e.stopPropagation()} />
                        </div>
                      </div>
                      <div className="flex items-end gap-2">
                        <button onClick={async (e) => {
                          e.stopPropagation();
                          try {
                            const { data } = await marketApi.getTemplate(t.id, budgets[t.id] || t.defaultBudget);
                            setTemplates(prev => prev.map(tmpl => tmpl.id === t.id ? ({
                              ...tmpl,
                              estimatedYieldPct: data.estimatedYieldPct,
                              estimatedMonthlyIncome: data.estimatedMonthlyIncome,
                              allocations: data.allocations,
                              budgetLabel: (budgets[t.id] || t.defaultBudget) >= 1000
                                ? `${((budgets[t.id] || t.defaultBudget) / 1000).toFixed(0)}k`
                                : String(budgets[t.id] || t.defaultBudget),
                            }) : tmpl));
                          } catch {}
                        }}
                          className="btn-ghost text-xs px-3 py-1.5">Refresh</button>
                        <button onClick={async (e) => {
                          e.stopPropagation();
                          await adoptTemplate(t);
                        }}
                          disabled={isAdopting}
                          className="btn-primary flex items-center gap-1.5 text-xs px-4 py-1.5">
                          {isAdopting ? <><span className="w-3 h-3 border-2 border-white/30 border-t-white rounded-full animate-spin" /> Adopting...</>
                            : <>🌱 Adopt</>}
                        </button>
                      </div>
                    </div>
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
