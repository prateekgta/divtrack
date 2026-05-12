'use client';

import { useState, useEffect, useRef } from 'react';
import { marketApi } from './api';
import { StockDetail } from './stock-detail';

interface Stock {
  id: string; ticker: string; name: string; sector: string;
  price: number; yieldPct: number; dividendFrequency: string;
  previousClose: number; changePct: number; country: string; category: string;
  parValue: number | null; nonCumulative: boolean; tags: string | null;
  lastPriceUpdate: string | null;
}

function FreshnessDot({ lastPriceUpdate }: { lastPriceUpdate: string | null }) {
  if (!lastPriceUpdate) {
    return <span className="inline-block w-1.5 h-1.5 rounded-full bg-gray-300" title="Price never updated" />;
  }
  const updated = new Date(lastPriceUpdate).getTime();
  const now = Date.now();
  const hours = (now - updated) / (1000 * 60 * 60);
  if (hours <= 1) {
    return <span className="inline-block w-1.5 h-1.5 rounded-full bg-green-500" title="Updated within the last hour" />;
  }
  if (hours <= 24) {
    return <span className="inline-block w-1.5 h-1.5 rounded-full bg-amber-400" title={`Updated ${Math.round(hours)}h ago`} />;
  }
  return <span className="inline-block w-1.5 h-1.5 rounded-full bg-gray-400" title={`Updated ${Math.round(hours)}h ago - stale`} />;
}

const CATEGORIES = [
  { key: 'all', label: 'All Stocks', icon: '🌳' },
  { key: 'top', label: 'Top Gainers', icon: '📈' },
  { key: 'high_yield', label: 'High Yield', icon: '🔥' },
  { key: 'monthly_income', label: 'Monthly Income', icon: '📅' },
  { key: 'dividend_growth', label: 'Dividend Growth', icon: '🌱' },
  { key: 'core', label: 'Core Holdings', icon: '💎' },
  { key: 'growth', label: 'Growth Tech', icon: '🚀' },
  { key: 'income', label: 'Income', icon: '💰' },
  { key: 'international', label: 'International', icon: '🌍' },
];

export function StockBrowser({ onSelect }: { onSelect: (ticker: string, name: string) => void }) {
  const [stocks, setStocks] = useState<Stock[]>([]);
  const [topPerformers, setTopPerformers] = useState<Stock[]>([]);
  const [loading, setLoading] = useState(true);
  const [query, setQuery] = useState('');
  const [category, setCategory] = useState('all');
  const [detailTicker, setDetailTicker] = useState<string | null>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    Promise.all([
      marketApi.getStocks(),
      marketApi.getTopPerformers(15),
    ]).then(([stocksRes, topRes]) => {
      setStocks(stocksRes.data);
      setTopPerformers(topRes.data);
      setLoading(false);
    }).catch(() => setLoading(false));
  }, []);

  useEffect(() => { inputRef.current?.focus(); }, []);

  const filtered = () => {
    const q = query.toLowerCase();
    let list: Stock[];

    if (category === 'top') {
      list = topPerformers;
    } else if (category === 'all') {
      list = stocks;
    } else {
      list = stocks.filter(s => s.category === category);
    }

    if (!q) return list;
    return list.filter(s =>
      s.ticker.toLowerCase().includes(q) || s.name.toLowerCase().includes(q));
  };

  const displayStocks = filtered();

  return (
    <>
      <div className="space-y-4">
        <div className="relative">
          <span className="absolute left-3 top-1/2 -translate-y-1/2 text-earth-400 text-sm">🔍</span>
          <input ref={inputRef}
            className="input-field pl-9" placeholder="Search ticker or name..."
            value={query} onChange={e => setQuery(e.target.value)} />
        </div>

        <div className="flex gap-1.5 overflow-x-auto pb-1 scrollbar-none">
          {CATEGORIES.map(c => (
            <button key={c.key}
              className={`whitespace-nowrap px-3 py-1.5 rounded-lg text-xs font-semibold transition-all flex items-center gap-1.5 ${
                category === c.key
                  ? 'bg-forest-600 text-white shadow-sm'
                  : 'bg-earth-50 text-earth-600 hover:bg-earth-100'
              }`}
              onClick={() => setCategory(c.key)}>
              <span>{c.icon}</span> {c.label}
            </button>
          ))}
        </div>

        <div className="max-h-72 overflow-y-auto space-y-1 pr-1">
          {loading ? (
            <div className="flex items-center justify-center py-12">
              <div className="w-6 h-6 border-2 border-forest-200 border-t-forest-600 rounded-full animate-spin" />
            </div>
          ) : displayStocks.length === 0 ? (
            <p className="text-center text-earth-400 py-8 text-sm">No stocks found</p>
          ) : category === 'top' ? (
            <>
              <p className="text-[10px] text-earth-400 font-medium px-1 mb-1">Today's top movers</p>
              {displayStocks.map((s, i) => (
                <StockRow key={s.id} stock={s} rank={i + 1}
                  onSelect={() => onSelect(s.ticker, s.name)}
                  onDetail={() => setDetailTicker(s.ticker)} />
              ))}
            </>
          ) : (
            displayStocks.map(s => (
              <StockRow key={s.id} stock={s}
                onSelect={() => onSelect(s.ticker, s.name)}
                onDetail={() => setDetailTicker(s.ticker)} />
            ))
          )}
        </div>

        <p className="text-[10px] text-earth-400 text-center">Click 📊 for performance chart & simulator</p>
      </div>

      {detailTicker && (
        <StockDetail
          ticker={detailTicker}
          name={stocks.find(s => s.ticker === detailTicker)?.name || ''}
          onClose={() => setDetailTicker(null)}
        />
      )}
    </>
  );
}

function StockRow({ stock: s, onSelect, onDetail, rank }: {
  stock: Stock; onSelect: () => void; onDetail: () => void; rank?: number
}) {
  const pv = s.parValue;
  const hasPar = pv != null && pv > 0;
  const discount = hasPar ? ((pv! - s.price) / pv! * 100) : 0;
  const statedYield = hasPar && s.yieldPct > 0
    ? (s.yieldPct / (s.price / pv!)) : null;

  return (
    <button type="button" onClick={onSelect}
      className="w-full flex items-center gap-3 p-3 rounded-xl hover:bg-forest-50/80 border border-transparent hover:border-forest-100 transition-all duration-200 text-left group">
      {rank && <span className="text-xs font-bold text-earth-400 w-4 shrink-0">#{rank}</span>}
      <div className="w-9 h-9 rounded-lg bg-gradient-to-br from-forest-400 to-forest-600 flex items-center justify-center text-white text-sm shadow-sm shrink-0">
        {s.dividendFrequency === 'MONTHLY' ? '📅' : '📆'}
      </div>
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-1.5 flex-wrap">
          <p className="font-semibold text-earth-800 text-sm">{s.ticker}</p>
          <FreshnessDot lastPriceUpdate={s.lastPriceUpdate} />
          {s.nonCumulative && (
            <span className="text-[9px] px-1 py-0.5 rounded bg-amber-100 text-amber-700 font-semibold leading-none">NC</span>
          )}
          {s.changePct !== 0 && (
            <span className={`text-xs font-medium ${s.changePct >= 0 ? 'text-green-600' : 'text-red-500'}`}>
              {s.changePct >= 0 ? '▲' : '▼'} {Math.abs(s.changePct).toFixed(2)}%
            </span>
          )}
          {s.country !== 'US' && (
            <span className="text-[10px] px-1.5 py-0.5 rounded bg-earth-100 text-earth-500 font-medium">{s.country}</span>
          )}
        </div>
        <p className="text-xs text-earth-400 truncate">{s.name}</p>
      </div>
      <div className="text-right shrink-0 min-w-0">
        <p className="font-bold text-forest-700 text-sm">{s.yieldPct.toFixed(2)}%</p>
        <p className="text-xs text-earth-400">${s.price.toFixed(2)}</p>
        {hasPar && (
          <p className={`text-[10px] ${discount > 0 ? 'text-green-600' : 'text-red-400'}`}>
            Par ${s.parValue} {discount > 0 ? `${discount.toFixed(1)}% discount` : 'premium'}
          </p>
        )}
      </div>
      <button onClick={(e) => { e.stopPropagation(); onDetail(); }}
        className="opacity-0 group-hover:opacity-100 text-xs text-forest-600 hover:text-forest-700 font-medium px-2 py-1 rounded-lg hover:bg-forest-50 transition-all"
        title="View performance">📊</button>
    </button>
  );
}
