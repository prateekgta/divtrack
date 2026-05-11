'use client';

import { useState, useEffect } from 'react';
import { marketApi } from './api';
import {
  LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer,
  CartesianGrid,
} from 'recharts';

interface Props {
  ticker: string;
  name: string;
  onClose: () => void;
}

type Range = '1m' | '6m' | '1y' | '2y' | '3y';

const RANGES: { key: Range; label: string }[] = [
  { key: '1m', label: '1M' },
  { key: '6m', label: '6M' },
  { key: '1y', label: '1Y' },
  { key: '2y', label: '2Y' },
  { key: '3y', label: '3Y' },
];

export function StockDetail({ ticker, name, onClose }: Props) {
  const [range, setRange] = useState<Range>('1y');
  const [data, setData] = useState<any>(null);
  const [sim, setSim] = useState<any>(null);
  const [simInvested, setSimInvested] = useState('10000');
  const [simDate, setSimDate] = useState('2024-01-01');
  const [loading, setLoading] = useState(true);
  const [simLoading, setSimLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    marketApi.getPerformance(ticker, range).then(({ data }) => {
      setData(data);
      setLoading(false);
    }).catch(() => setLoading(false));
  }, [ticker, range]);

  const runSim = async () => {
    setSimLoading(true);
    try {
      const { data: r } = await marketApi.simulate(ticker, parseFloat(simInvested), simDate);
      setSim(r);
    } catch {}
    setSimLoading(false);
  };

  const changePct = data?.totalReturnPct;
  const isPositive = changePct >= 0;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-sm" onClick={onClose}>
      <div className="bg-white rounded-3xl shadow-2xl border border-earth-100 w-full max-w-2xl max-h-[90vh] overflow-y-auto" onClick={e => e.stopPropagation()}>
        <div className="p-6 space-y-6">
          <div className="flex items-start justify-between">
            <div>
              <div className="flex items-center gap-2">
                <h2 className="text-2xl font-bold text-earth-800">{ticker}</h2>
                <span className="text-sm text-earth-400 font-normal">{name}</span>
              </div>
            </div>
            <button onClick={onClose} className="btn-ghost p-2 text-earth-400 hover:text-earth-600 text-lg">✕</button>
          </div>

          {loading ? (
            <div className="flex items-center justify-center py-16">
              <div className="w-8 h-8 border-3 border-forest-200 border-t-forest-600 rounded-full animate-spin" />
            </div>
          ) : data ? (
            <>
              <div className="flex items-center gap-4">
                <div className="flex gap-2">
                  {RANGES.map(r => (
                    <button key={r.key}
                      className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                        range === r.key
                          ? 'bg-forest-600 text-white shadow-sm'
                          : 'bg-earth-50 text-earth-600 hover:bg-earth-100'
                      }`}
                      onClick={() => setRange(r.key)}>
                      {r.label}
                    </button>
                  ))}
                </div>
                {changePct !== undefined && (
                  <div className={`ml-auto text-right ${isPositive ? 'text-green-600' : 'text-red-500'}`}>
                    <span className="text-lg font-bold">{isPositive ? '+' : ''}{changePct.toFixed(2)}%</span>
                    <p className="text-xs text-earth-400">total return</p>
                  </div>
                )}
              </div>

              {data.history?.length > 1 ? (
                <div className="bg-earth-50/50 rounded-2xl p-4">
                  <ResponsiveContainer width="100%" height={280}>
                    <LineChart data={data.history}>
                      <CartesianGrid strokeDasharray="3 3" stroke="#e0ccaa" strokeOpacity={0.3} />
                      <XAxis dataKey="date" tick={{ fontSize: 11, fill: '#8B6914' }}
                        tickFormatter={(v) => v.slice(5, 10)} />
                      <YAxis domain={['auto', 'auto']} tick={{ fontSize: 11, fill: '#8B6914' }}
                        tickFormatter={(v) => `$${v}`} width={60} />
                      <Tooltip
                        contentStyle={{ borderRadius: 12, border: '1px solid #e0ccaa', boxShadow: '0 4px 12px rgba(0,0,0,0.08)' }}
                        labelFormatter={(v) => `Date: ${v}`}
                        formatter={(value: number) => [`$${value.toFixed(2)}`, 'Price']} />
                      <Line type="monotone" dataKey="close" stroke="#16a34a" strokeWidth={2}
                        dot={false} activeDot={{ r: 4, fill: '#16a34a' }} />
                    </LineChart>
                  </ResponsiveContainer>
                </div>
              ) : (
                <div className="py-12 text-center text-earth-400 text-sm">Not enough price data available</div>
              )}

              <div className="bg-gradient-to-r from-forest-50 to-leaf-50 rounded-2xl border border-forest-100 p-5 space-y-4">
                <h3 className="font-semibold text-forest-800 text-sm">Investment Simulator</h3>
                <p className="text-xs text-earth-500">See how much your investment would be worth today.</p>
                <div className="flex flex-col sm:flex-row gap-3">
                  <div className="flex-1">
                    <label className="block text-xs font-medium text-earth-600 mb-1">Amount Invested</label>
                    <div className="relative">
                      <span className="absolute left-3 top-1/2 -translate-y-1/2 text-earth-400 text-sm">$</span>
                      <input type="number" className="input-field pl-7" value={simInvested}
                        onChange={e => setSimInvested(e.target.value)} />
                    </div>
                  </div>
                  <div className="flex-1">
                    <label className="block text-xs font-medium text-earth-600 mb-1">Buy Date</label>
                    <input type="date" className="input-field" value={simDate}
                      onChange={e => setSimDate(e.target.value)} max={new Date().toISOString().slice(0, 10)} />
                  </div>
                  <div className="flex items-end">
                    <button onClick={runSim} disabled={simLoading}
                      className="btn-primary px-5 py-2.5 text-sm">
                      {simLoading ? '...' : 'Simulate'}
                    </button>
                  </div>
                </div>

                {sim && (
                  <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 pt-2">
                    <div className="bg-white/70 rounded-xl p-3 text-center">
                      <p className="text-xs text-earth-400">Invested</p>
                      <p className="font-bold text-earth-800 mt-0.5">${Number(sim.invested).toLocaleString()}</p>
                    </div>
                    <div className="bg-white/70 rounded-xl p-3 text-center">
                      <p className="text-xs text-earth-400">Current Value</p>
                      <p className="font-bold text-forest-700 mt-0.5">${Number(sim.currentValue).toLocaleString(undefined, { minimumFractionDigits: 2 })}</p>
                    </div>
                    <div className="bg-white/70 rounded-xl p-3 text-center">
                      <p className="text-xs text-earth-400">Est. Dividends</p>
                      <p className="font-bold text-leaf-700 mt-0.5">${Number(sim.estimatedDividends).toLocaleString(undefined, { minimumFractionDigits: 2 })}</p>
                    </div>
                    <div className="bg-white/70 rounded-xl p-3 text-center">
                      <p className="text-xs text-earth-400">Total Return</p>
                      <p className={`font-bold mt-0.5 ${sim.totalReturnPct >= 0 ? 'text-green-600' : 'text-red-500'}`}>
                        {sim.totalReturnPct >= 0 ? '+' : ''}{Number(sim.totalReturnPct).toFixed(2)}%
                      </p>
                    </div>
                  </div>
                )}
              </div>
            </>
          ) : (
            <div className="py-12 text-center text-earth-400 text-sm">Failed to load data</div>
          )}
        </div>
      </div>
    </div>
  );
}
