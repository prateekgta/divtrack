'use client';

import { useState } from 'react';
import { dividendApi } from '@/lib/api';
import { useAuth } from '@/lib/auth-context';
import { useToast, StatCard, CardSkeleton } from '@/lib/ui';

export default function SnowballPage() {
  const { isPro } = useAuth();
  const { toast } = useToast();
  const [projection, setProjection] = useState<any>(null);
  const [contribution, setContribution] = useState('200');
  const [years, setYears] = useState('15');
  const [reinvest, setReinvest] = useState(true);
  const [loading, setLoading] = useState(false);

  const runProjection = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      const { data } = await dividendApi.getSnowball({
        monthlyContribution: parseFloat(contribution),
        reinvestDividends: reinvest,
        projectionYears: parseInt(years),
      });
      setProjection(data);
    } catch {
      toast('Failed to run projection', 'error');
    }
    setLoading(false);
  };

  if (!isPro) {
    return (
      <div className="max-w-xl mx-auto text-center py-16 space-y-6">
        <div className="text-6xl">❄️</div>
        <h1 className="text-2xl font-bold text-forest-800">Snowball Simulator</h1>
        <p className="text-earth-500">See how your dividend snowball grows over time — year by year.</p>
        <div className="bg-white rounded-2xl border border-earth-100 p-8 shadow-sm space-y-4">
          <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-leaf-400 to-leaf-600 flex items-center justify-center text-white text-2xl mx-auto shadow-lg shadow-leaf-200/50">
            🌟
          </div>
          <p className="text-xl font-bold font-display text-forest-700">Pro Feature</p>
          <p className="text-sm text-earth-500 max-w-sm mx-auto">
            Upgrade to Pro to unlock the snowball simulator and see exactly when you&apos;ll reach financial independence.
          </p>
          <button className="btn-primary px-8">Upgrade to Pro — $5/mo</button>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6 max-w-5xl">
      <div>
        <h1 className="text-2xl font-bold text-forest-800">Snowball Simulator</h1>
        <p className="text-earth-500 text-sm mt-0.5">Project your dividend growth year by year</p>
      </div>

      <form onSubmit={runProjection} className="bg-white rounded-2xl border border-earth-100 p-6 shadow-sm">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div>
            <label className="block text-xs font-semibold text-earth-600 mb-1">Monthly Contribution</label>
            <div className="relative">
              <span className="absolute left-3 top-1/2 -translate-y-1/2 text-earth-400 text-sm">$</span>
              <input type="number" className="input-field pl-7" value={contribution}
                onChange={e => setContribution(e.target.value)} />
            </div>
          </div>
          <div>
            <label className="block text-xs font-semibold text-earth-600 mb-1">Projection Years</label>
            <input type="number" className="input-field" value={years}
              onChange={e => setYears(e.target.value)} />
          </div>
          <div className="flex items-end pb-1">
            <label className="flex items-center gap-2.5 cursor-pointer group">
              <div className={`w-5 h-5 rounded-md border-2 flex items-center justify-center transition-all duration-200 ${
                reinvest ? 'bg-forest-600 border-forest-600' : 'border-earth-300 group-hover:border-earth-400'
              }`}>
                {reinvest && <span className="text-white text-xs">✓</span>}
              </div>
              <input type="checkbox" checked={reinvest}
                onChange={e => setReinvest(e.target.checked)} className="hidden" />
              <span className="text-sm text-earth-600">Reinvest dividends</span>
            </label>
          </div>
          <div className="flex items-end">
            <button type="submit" disabled={loading}
              className="btn-primary w-full flex items-center justify-center gap-2">
              {loading ? (
                <><span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" /> Calculating...</>
              ) : 'Run Projection'}
            </button>
          </div>
        </div>
      </form>

      {projection && (
        <>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
            <StatCard label="Current Income" value={`$${projection.currentMonthlyIncome}/mo`} icon="💵" color="forest" />
            <StatCard label="Portfolio Value" value={`$${(projection.currentPortfolioValue || 0).toLocaleString()}`} icon="💰" color="forest" />
            <StatCard label="Adding Monthly" value={`$${projection.monthlyContribution}`} icon="💧" color="leaf" />
            <StatCard label="FI Target Year" value={projection.targetYear || '—'} icon="🎯" color="earth" />
          </div>

          <div className="bg-white rounded-2xl border border-earth-100 p-6 shadow-sm overflow-hidden">
            <h2 className="font-display text-lg font-bold text-forest-800 mb-4">Year-by-Year Growth</h2>
            <div className="overflow-x-auto -mx-6">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-earth-100">
                    <th className="text-left py-3 px-6 text-xs font-semibold text-earth-500 uppercase tracking-wider">Year</th>
                    <th className="text-right py-3 px-6 text-xs font-semibold text-earth-500 uppercase tracking-wider">Portfolio Value</th>
                    <th className="text-right py-3 px-6 text-xs font-semibold text-earth-500 uppercase tracking-wider">Monthly Income</th>
                    <th className="text-right py-3 px-6 text-xs font-semibold text-earth-500 uppercase tracking-wider">Annual Contributions</th>
                    <th className="text-right py-3 px-6 text-xs font-semibold text-earth-500 uppercase tracking-wider">Annual Dividends</th>
                  </tr>
                </thead>
                <tbody>
                  {projection.years?.map((yr: any, i: number) => (
                    <tr key={yr.year}
                      className={`border-b border-earth-50 hover:bg-forest-50/50 transition-colors ${
                        i === projection.years.length - 1 ? 'bg-forest-50/30 font-semibold' : ''
                      }`}>
                      <td className="py-3.5 px-6 text-earth-800 font-medium">Year {yr.year}</td>
                      <td className="text-right py-3.5 px-6 text-forest-700 font-medium">
                        ${yr.portfolioValue?.toLocaleString()}
                      </td>
                      <td className="text-right py-3.5 px-6 text-leaf-700 font-medium">
                        ${yr.monthlyIncome}/mo
                      </td>
                      <td className="text-right py-3.5 px-6 text-earth-500">
                        ${yr.annualContributions?.toLocaleString()}
                      </td>
                      <td className="text-right py-3.5 px-6 text-forest-600 font-medium">
                        ${yr.annualDividends?.toLocaleString()}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </>
      )}

      {!projection && (
        <div className="bg-white rounded-2xl border border-earth-100 p-12 shadow-sm text-center space-y-4">
          <div className="text-6xl">❄️</div>
          <p className="text-earth-600 font-medium">Set your parameters and run the projection</p>
          <p className="text-sm text-earth-400">See how small monthly additions grow into a massive snowball</p>
        </div>
      )}
    </div>
  );
}
