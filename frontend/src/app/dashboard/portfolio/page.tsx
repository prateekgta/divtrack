'use client';

import { useState, useEffect } from 'react';
import { portfolioApi } from '@/lib/api';
import { useAuth } from '@/lib/auth-context';
import { useToast, StatCard, CardSkeleton } from '@/lib/ui';

interface Holding {
  id: string;
  ticker: string;
  shares: number;
  avgCost: number;
  currentPrice: number;
  totalValue: number;
  annualDividend: number;
  dividendYield: number;
  dividendIncome: number;
}

export default function PortfolioPage() {
  const { isPro } = useAuth();
  const { toast } = useToast();
  const [holdings, setHoldings] = useState<Holding[]>([]);
  const [loading, setLoading] = useState(true);
  const [summary, setSummary] = useState<any>(null);

  useEffect(() => {
    const fetchPortfolio = async () => {
      try {
        const { data } = await portfolioApi.get();
        setHoldings(data.holdings || []);
        setSummary(data.summary || null);
      } catch {
        toast('Failed to load portfolio', 'error');
      }
      setLoading(false);
    };
    fetchPortfolio();
  }, []);

  if (!isPro) {
    return (
      <div className="max-w-xl mx-auto text-center py-16 space-y-6">
        <div className="text-6xl">📊</div>
        <h1 className="text-2xl font-bold text-forest-800">Portfolio</h1>
        <p className="text-earth-500">Track and manage your dividend portfolio.</p>
        <div className="bg-white rounded-2xl border border-earth-100 p-8 shadow-sm space-y-4">
          <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-leaf-400 to-leaf-600 flex items-center justify-center text-white text-2xl mx-auto shadow-lg shadow-leaf-200/50">
            🌟
          </div>
          <p className="text-xl font-bold font-display text-forest-700">Pro Feature</p>
          <p className="text-sm text-earth-500 max-w-sm mx-auto">
            Upgrade to Pro to track your dividend portfolio and monitor your holdings.
          </p>
          <button className="btn-primary px-8">Upgrade to Pro — $5/mo</button>
        </div>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="space-y-6 max-w-5xl">
        <div className="h-8 w-48 bg-earth-100 rounded-lg animate-pulse" />
        <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
          {[...Array(4)].map((_, i) => <CardSkeleton key={i} />)}
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {[...Array(6)].map((_, i) => <CardSkeleton key={i} />)}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6 max-w-5xl">
      <div>
        <h1 className="text-2xl font-bold text-forest-800">Portfolio</h1>
        <p className="text-earth-500 text-sm mt-0.5">Your dividend holdings at a glance</p>
      </div>

      {summary && (
        <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
          <StatCard label="Total Value" value={`$${(summary.totalValue || 0).toLocaleString()}`} icon="💰" color="forest" />
          <StatCard label="Annual Income" value={`$${(summary.annualIncome || 0).toLocaleString()}`} icon="💵" color="leaf" />
          <StatCard label="Avg Yield" value={`${(summary.avgYield || 0).toFixed(2)}%`} icon="📈" color="earth" />
          <StatCard label="Holdings" value={`${holdings.length}`} icon="📊" color="forest" />
        </div>
      )}

      {holdings.length === 0 ? (
        <div className="bg-white rounded-2xl border border-earth-100 p-12 shadow-sm text-center space-y-4">
          <div className="text-6xl">📦</div>
          <p className="text-earth-600 font-medium">No holdings yet</p>
          <p className="text-sm text-earth-400">Add your first dividend stock to start tracking</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {holdings.map((h) => (
            <div key={h.id} className="bg-white rounded-2xl border border-earth-100 p-5 shadow-sm hover:shadow-md transition-shadow">
              <div className="flex items-center justify-between mb-3">
                <span className="text-lg font-bold text-forest-800">{h.ticker}</span>
                <span className="text-xs font-medium text-earth-400 bg-earth-50 px-2 py-0.5 rounded-full">
                  {h.shares} shares
                </span>
              </div>
              <div className="space-y-2 text-sm">
                <div className="flex justify-between">
                  <span className="text-earth-500">Value</span>
                  <span className="font-semibold text-forest-700">${(h.totalValue || 0).toLocaleString()}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-earth-500">Yield</span>
                  <span className="font-semibold text-leaf-700">{(h.dividendYield || 0).toFixed(2)}%</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-earth-500">Annual Dividend</span>
                  <span className="font-semibold text-forest-600">${(h.annualDividend || 0).toLocaleString()}</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
