'use client';

import { useState, useEffect } from 'react';
import { portfolioApi, marketApi } from '@/lib/api';
import { useToast, StatCard, CardSkeleton, EmptyState } from '@/lib/ui';
import { StockBrowser } from '@/lib/stock-browser';
import { StockDetail } from '@/lib/stock-detail';
import { PortfolioTemplates } from '@/lib/portfolio-templates';

export default function DashboardPage() {
  const { toast } = useToast();
  const [summary, setSummary] = useState<any>(null);
  const [showAdd, setShowAdd] = useState(false);
  const [ticker, setTicker] = useState('');
  const [shares, setShares] = useState('');
  const [adding, setAdding] = useState(false);
  const [selectedStockName, setSelectedStockName] = useState('');
  const [step, setStep] = useState<'browse' | 'shares'>('browse');
  const [detailTicker, setDetailTicker] = useState<string | null>(null);

  useEffect(() => { loadData(); }, []);

  const loadData = async () => {
    try {
      const portRes = await portfolioApi.get();
      setSummary(portRes.data);
    } catch {}
  };

  const addHolding = async (e: React.FormEvent) => {
    e.preventDefault();
    setAdding(true);
    try {
      await portfolioApi.addHolding({ ticker, shares: parseFloat(shares) });
      toast(`${ticker} planted! 🌱`);
      setTicker(''); setShares(''); setShowAdd(false);
      loadData();
    } catch (err: any) {
      toast(err.response?.data?.detail || 'Failed to add holding', 'error');
    } finally {
      setAdding(false);
    }
  };

  const removeHolding = async (id: string, ticker: string) => {
    try {
      await portfolioApi.removeHolding(id);
      toast(`${ticker} removed`);
      loadData();
    } catch {
      toast('Failed to remove', 'error');
    }
  };

  if (!summary) return (
    <div className="space-y-6">
      <div className="h-8 w-48 rounded-xl bg-earth-200/70 animate-pulse" />
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <CardSkeleton /><CardSkeleton /><CardSkeleton />
      </div>
      <div className="bg-white rounded-2xl border border-earth-100 p-6 space-y-3">
        {[1,2,3].map(i => <div key={i} className="h-14 rounded-xl bg-earth-100/50 animate-pulse" />)}
      </div>
    </div>
  );

  return (
    <div className="space-y-6 max-w-5xl">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-forest-800">Your Forest</h1>
          <p className="text-earth-500 text-sm mt-0.5">
            {summary.holdingCount} {summary.holdingCount === 1 ? 'tree' : 'trees'} planted
          </p>
        </div>
        <button onClick={() => setShowAdd(!showAdd)} className="btn-primary flex items-center gap-2">
          <span>+</span> Plant a Tree
        </button>
      </div>

      {showAdd && (
        <div className="bg-gradient-to-r from-forest-50 to-leaf-50 rounded-2xl border border-forest-100 p-5">
          {step === 'browse' ? (
            <div>
              <div className="flex items-center justify-between mb-3">
                <p className="text-xs font-semibold text-forest-700">Pick a stock to plant</p>
                <button type="button" onClick={() => { setShowAdd(false); setStep('browse'); setSelectedStockName(''); setTicker(''); setShares(''); }}
                  className="btn-ghost text-xs">Cancel</button>
              </div>
              <StockBrowser onSelect={(t, n) => {
                setTicker(t);
                setSelectedStockName(n);
                setStep('shares');
              }} />
            </div>
          ) : (
            <form onSubmit={addHolding} className="space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-xs font-semibold text-forest-700">Selected Stock</p>
                  <p className="text-lg font-bold text-earth-800">{ticker} <span className="font-normal text-earth-400 text-sm">{selectedStockName}</span></p>
                </div>
                <button type="button" onClick={() => { setStep('browse'); setTicker(''); setSelectedStockName(''); }}
                  className="btn-ghost text-xs">Change</button>
              </div>
              <div className="flex flex-col sm:flex-row gap-3 items-end">
                <div className="flex-1 w-full">
                  <label className="block text-xs font-semibold text-forest-700 mb-1">Shares</label>
                  <input type="number" step="0.001" className="input-field" value={shares}
                    onChange={e => setShares(e.target.value)} required placeholder="e.g. 100" autoFocus />
                </div>
                <button type="submit" disabled={adding} className="btn-primary w-full sm:w-auto flex items-center justify-center gap-2">
                  {adding ? <><span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" /> Planting...</> : '🌱 Plant'}
                </button>
                <button type="button" onClick={() => { setShowAdd(false); setStep('browse'); setSelectedStockName(''); setTicker(''); setShares(''); }}
                  className="btn-ghost text-sm">Cancel</button>
              </div>
            </form>
          )}
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <StatCard label="Forest Value" value={`$${(summary.totalValue || 0).toLocaleString()}`} icon="💰" />
        <StatCard label="Monthly Harvest" value={`$${(summary.totalMonthlyIncome || 0).toFixed(2)}`} icon="💵" color="leaf" sub="dividend income" />
        <StatCard label="Avg Yield" value={`${(summary.avgYieldPct || 0).toFixed(2)}%`} icon="📈" color="earth" />
      </div>

      <div className="bg-white rounded-2xl border border-earth-100 p-6 shadow-sm">
        <h2 className="font-display text-lg font-bold text-forest-800 mb-4">Your Trees</h2>
        {summary.holdings?.length === 0 ? (
          <EmptyState
            icon="🌱"
            title="Your forest is empty"
            desc="Start by planting your first tree. Add a stock you own to see your dividend income grow."
            action={<button onClick={() => setShowAdd(true)} className="btn-primary">Plant Your First Tree</button>}
          />
        ) : (
          <div className="space-y-2">
            {summary.holdings?.map((h: any) => (
              <div key={h.id}
                className="group flex items-center justify-between p-4 rounded-xl bg-earth-50/80 hover:bg-forest-50/80 border border-transparent hover:border-forest-100 transition-all duration-200">
                <div className="flex items-center gap-4">
                  <div className="w-11 h-11 rounded-xl bg-gradient-to-br from-forest-400 to-forest-600 flex items-center justify-center text-white text-lg shadow-sm shadow-forest-200/50">
                    🌲
                  </div>
                  <div>
                    <p className="font-semibold text-earth-800">{h.ticker}</p>
                    <p className="text-xs text-earth-500">
                      {Number(h.shares).toLocaleString()} shares @ ${Number(h.currentPrice).toFixed(2)}
                    </p>
                  </div>
                </div>
                <div className="flex items-center gap-5">
                  <div className="text-right">
                    <p className="font-semibold text-forest-700">${Number(h.monthlyIncome).toFixed(2)}<span className="text-xs text-earth-400 font-normal">/mo</span></p>
                    <p className="text-xs text-earth-500">{h.yieldPct}% yield</p>
                  </div>
                  <div className="flex items-center gap-1">
                    <button onClick={() => setDetailTicker(h.ticker)}
                      className="opacity-0 group-hover:opacity-100 text-xs text-forest-600 hover:text-forest-700 font-medium p-1.5 rounded-lg hover:bg-forest-50 transition-all"
                      title="View performance">📊</button>
                    <button onClick={() => removeHolding(h.id, h.ticker)}
                      className="opacity-0 group-hover:opacity-100 text-earth-400 hover:text-red-500 transition-all duration-200 text-sm font-medium">
                      ✕
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
      <div className="bg-white rounded-2xl border border-earth-100 p-6 shadow-sm">
        <PortfolioTemplates onComplete={loadData} />
      </div>

      {detailTicker && (
        <StockDetail
          ticker={detailTicker}
          name={summary?.holdings?.find((h: any) => h.ticker === detailTicker)?.ticker || detailTicker}
          onClose={() => setDetailTicker(null)}
        />
      )}
    </div>
  );
}
