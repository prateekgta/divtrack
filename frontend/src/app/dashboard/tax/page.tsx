'use client';

import { useState, useEffect } from 'react';
import { dividendApi } from '@/lib/api';
import { useAuth } from '@/lib/auth-context';
import { useToast, StatCard, CardSkeleton } from '@/lib/ui';

export default function TaxPage() {
  const { isPro } = useAuth();
  const { toast } = useToast();
  const [advice, setAdvice] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isPro) { setLoading(false); return; }
    dividendApi.getTaxAdvice().then(({ data }) => {
      setAdvice(data);
    }).catch(() => {
      toast('Failed to load tax advice', 'error');
    }).finally(() => setLoading(false));
  }, [isPro]);

  if (!isPro) {
    return (
      <div className="max-w-xl mx-auto text-center py-16 space-y-6">
        <div className="text-6xl">🧭</div>
        <h1 className="text-2xl font-bold text-forest-800">Tax Bucket Optimizer</h1>
        <p className="text-earth-500">Optimize which stocks go in which account type to minimize taxes.</p>
        <div className="bg-white rounded-2xl border border-earth-100 p-8 shadow-sm space-y-4">
          <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-leaf-400 to-leaf-600 flex items-center justify-center text-white text-2xl mx-auto shadow-lg shadow-leaf-200/50">
            🌟
          </div>
          <p className="text-xl font-bold font-display text-forest-700">Pro Feature</p>
          <p className="text-sm text-earth-500 max-w-sm mx-auto">
            Upgrade to Pro and save hundreds in taxes every year by placing every stock in the right account.
          </p>
          <button className="btn-primary px-8">Upgrade to Pro — $5/mo</button>
        </div>
      </div>
    );
  }

  if (loading) return (
    <div className="space-y-6">
      <div className="h-8 w-56 rounded-xl bg-earth-200/70 animate-pulse" />
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <CardSkeleton /><CardSkeleton /><CardSkeleton />
      </div>
    </div>
  );

  const scoreColor = (s: number) => {
    if (s >= 80) return 'text-forest-600';
    if (s >= 50) return 'text-leaf-600';
    return 'text-red-500';
  };

  return (
    <div className="space-y-6 max-w-5xl">
      <div>
        <h1 className="text-2xl font-bold text-forest-800">Tax Bucket Optimizer</h1>
        <p className="text-earth-500 text-sm mt-0.5">Place every stock in the right account — save hundreds per year</p>
      </div>

      {advice && (
        <>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="relative bg-white rounded-2xl border border-earth-100 p-6 shadow-sm overflow-hidden group hover:shadow-lg transition-all">
              <div className="absolute inset-0 bg-gradient-to-br from-forest-500/5 to-forest-700/5" />
              <div className="relative text-center">
                <p className="text-sm text-earth-500 font-medium">Optimization Score</p>
                <p className={`text-4xl font-bold font-display mt-2 ${scoreColor(advice.optimizationScore)}`}>
                  {advice.optimizationScore}<span className="text-2xl text-earth-300 font-normal">/100</span>
                </p>
              </div>
            </div>
            <StatCard label="Sub-optimal Holdings" value={String(advice.suboptimalCount || 0)} icon="⚠️" color="leaf" />
            <StatCard label="Annual Tax Savings" value={`$${advice.annualTaxSavings || 0}`} icon="💰" color="forest" />
          </div>

          <div className="bg-white rounded-2xl border border-earth-100 p-6 shadow-sm">
            <h2 className="font-display text-lg font-bold text-forest-800 mb-4">Recommendations</h2>
            <div className="space-y-3">
              {advice.advices?.map((a: any, i: number) => (
                <div key={i} className="p-4 rounded-xl bg-earth-50/80 border border-earth-100 hover:border-leaf-200 transition-all duration-200 space-y-3">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2.5">
                      <span className="font-bold text-earth-800">{a.ticker}</span>
                      <span className="badge-leaf text-[10px]">{a.yieldPct}% yield</span>
                      <span className="text-xs text-earth-400">{a.type}</span>
                    </div>
                    <span className="text-sm font-semibold text-red-500">-${a.annualTaxImpact}/yr</span>
                  </div>
                  <div className="flex items-center gap-3 text-sm">
                    <span className="px-3 py-1.5 bg-earth-200 text-earth-700 rounded-lg font-medium">{a.currentAccount}</span>
                    <span className="text-earth-400">→</span>
                    <span className="px-3 py-1.5 bg-forest-100 text-forest-700 rounded-lg font-medium">{a.recommendedAccount}</span>
                    <span className="ml-auto text-xs text-forest-600 font-medium">
                      Save ${a.annualTaxImpact}/yr
                    </span>
                  </div>
                  <p className="text-xs text-earth-500 leading-relaxed">{a.reason}</p>
                </div>
              ))}
              {(!advice.advices || advice.advices.length === 0) && (
                <div className="text-center py-8 space-y-3">
                  <div className="text-5xl">🌟</div>
                  <p className="text-forest-700 font-medium">Your portfolio is already optimally placed</p>
                  <p className="text-sm text-earth-400">Great job! No changes needed.</p>
                </div>
              )}
            </div>
          </div>
        </>
      )}
    </div>
  );
}
