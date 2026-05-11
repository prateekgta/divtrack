'use client';

import { useState, useEffect } from 'react';
import { dividendApi } from '@/lib/api';
import { useToast, StatCard, CardSkeleton, EmptyState, ProgressBar, AnimatedNumber } from '@/lib/ui';

export default function PaycheckPage() {
  const { toast } = useToast();
  const [milestone, setMilestone] = useState<any>(null);
  const [showAddBill, setShowAddBill] = useState(false);
  const [ticker, setTicker] = useState('');
  const [billName, setBillName] = useState('');
  const [billAmount, setBillAmount] = useState('');
  const [adding, setAdding] = useState(false);

  useEffect(() => { loadData(); }, []);

  const loadData = async () => {
    try {
      const { data } = await dividendApi.getPaycheck();
      setMilestone(data);
    } catch {}
  };

  const addBill = async (e: React.FormEvent) => {
    e.preventDefault();
    setAdding(true);
    try {
      await dividendApi.addBillMapping({ ticker, billName, billAmount: parseFloat(billAmount) });
      toast(`"${billName}" mapped to ${ticker}`);
      setTicker(''); setBillName(''); setBillAmount(''); setShowAddBill(false);
      loadData();
    } catch (err: any) {
      toast(err.response?.data?.detail || 'Failed to add bill', 'error');
    } finally {
      setAdding(false);
    }
  };

  if (!milestone) return (
    <div className="space-y-6">
      <div className="h-8 w-56 rounded-xl bg-earth-200/70 animate-pulse" />
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <CardSkeleton /><CardSkeleton /><CardSkeleton />
      </div>
    </div>
  );

  const hasExpenses = milestone.monthlyExpenses > 0;

  return (
    <div className="space-y-6 max-w-4xl">
      <div>
        <h1 className="text-2xl font-bold text-forest-800">Your Dividend Paycheck</h1>
        <p className="text-earth-500 text-sm mt-0.5">See exactly what your portfolio pays you — and what it covers</p>
      </div>

      <div className="relative bg-gradient-to-br from-forest-500 to-forest-700 rounded-3xl p-8 text-white overflow-hidden">
        <div className="absolute inset-0 opacity-10">
          <div className="absolute top-4 left-4 text-6xl">🌳</div>
          <div className="absolute bottom-4 right-4 text-4xl">🍃</div>
          <div className="absolute top-1/2 right-1/4 text-3xl">🌿</div>
        </div>
        <div className="relative text-center space-y-2">
          <p className="text-forest-100 text-sm font-medium">Your portfolio pays you</p>
          <p className="text-5xl md:text-6xl font-bold font-display">
            $<AnimatedNumber value={milestone.totalMonthlyIncome || 0} decimals={2} />
            <span className="text-2xl text-forest-200 font-normal">/mo</span>
          </p>
          <p className="text-forest-200 text-lg">
            $<AnimatedNumber value={milestone.totalAnnualIncome || 0} decimals={0} suffix="/yr" />
          </p>
        </div>
      </div>

      {hasExpenses && (
        <>
          <div className="bg-white rounded-2xl border border-earth-100 p-6 shadow-sm space-y-5">
            <div className="flex items-center justify-between">
              <h2 className="font-display text-lg font-bold text-forest-800">Freedom Progress</h2>
              <span className="text-3xl font-bold font-display text-forest-600">
                <AnimatedNumber value={milestone.coverageRatio || 0} decimals={1} suffix="%" />
              </span>
            </div>
            <ProgressBar value={milestone.coverageRatio || 0} />
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 pt-2">
              {[
                { label: 'Monthly Expenses', value: milestone.monthlyExpenses, color: 'text-earth-800' },
                { label: 'Monthly Income', value: milestone.totalMonthlyIncome, color: 'text-forest-700' },
                { label: 'Gap to Cover', value: milestone.gapToFreedom, color: 'text-leaf-600' },
                { label: milestone.nextMilestoneName, value: milestone.nextMilestoneGap, color: 'text-forest-600' },
              ].map((item, i) => (
                <div key={i} className="text-center p-3 bg-earth-50 rounded-xl">
                  <p className="text-xs text-earth-500 mb-0.5">{item.label}</p>
                  <p className={`text-lg font-bold font-display ${item.color}`}>
                    ${Number(item.value || 0).toFixed(2)}
                  </p>
                </div>
              ))}
            </div>
          </div>

          <div className="bg-white rounded-2xl border border-earth-100 p-6 shadow-sm">
            <div className="flex items-center justify-between mb-4">
              <h2 className="font-display text-lg font-bold text-forest-800">Bill Coverage</h2>
              <button onClick={() => setShowAddBill(!showAddBill)} className="btn-secondary text-xs flex items-center gap-1">
                <span>+</span> Add Bill
              </button>
            </div>

            {showAddBill && (
              <form onSubmit={addBill} className="flex flex-col sm:flex-row gap-2 mb-5 p-4 bg-gradient-to-r from-earth-50 to-leaf-50 rounded-xl border border-earth-100">
                <input className="input-sm flex-1" placeholder="Ticker (e.g. STRC)"
                  value={ticker} onChange={e => setTicker(e.target.value.toUpperCase())} required />
                <input className="input-sm flex-1" placeholder="Bill name (e.g. Netflix)"
                  value={billName} onChange={e => setBillName(e.target.value)} required />
                <input type="number" className="input-sm w-full sm:w-24" placeholder="$"
                  value={billAmount} onChange={e => setBillAmount(e.target.value)} required />
                <button type="submit" disabled={adding} className="btn-primary text-sm">
                  {adding ? 'Adding...' : 'Add'}
                </button>
              </form>
            )}

            <div className="space-y-3">
              {milestone.billCoverages?.map((bc: any, i: number) => (
                <div key={i} className="p-4 rounded-xl bg-earth-50/80 hover:bg-forest-50/80 border border-transparent hover:border-forest-100 transition-all duration-200">
                  <div className="flex items-center justify-between mb-2">
                    <div className="flex items-center gap-2">
                      <span className="font-semibold text-earth-800">{bc.billName}</span>
                      <span className="text-xs text-earth-400">· {bc.coveredByStock}</span>
                    </div>
                    <span className="text-sm font-medium text-earth-600">${Number(bc.billAmount).toFixed(2)}/mo</span>
                  </div>
                  <ProgressBar value={bc.coveragePct || 0} color={bc.coveragePct >= 100 ? 'forest' : 'leaf'} />
                  <div className="flex justify-between mt-1.5 text-xs">
                    <span className={bc.coveragePct >= 100 ? 'text-forest-600 font-medium' : 'text-leaf-600 font-medium'}>
                      {bc.coveragePct >= 100 ? '✓ Fully covered' : `${bc.coveragePct.toFixed(1)}% covered`}
                    </span>
                    <span className="text-earth-400">
                      ${Number(bc.coveredAmount).toFixed(2)} of ${Number(bc.billAmount).toFixed(2)}
                    </span>
                  </div>
                </div>
              ))}
              {(!milestone.billCoverages || milestone.billCoverages.length === 0) && (
                <EmptyState
                  icon="📋"
                  title="No bills mapped"
                  desc="Add your monthly bills to see which dividends cover them."
                  action={<button onClick={() => setShowAddBill(true)} className="btn-primary">Add Your First Bill</button>}
                />
              )}
            </div>
          </div>
        </>
      )}

      {!hasExpenses && (
        <EmptyState
          icon="💵"
          title="Track your freedom progress"
          desc="Set your monthly expenses to see how your dividends cover your bills."
          action={<button onClick={() => setShowAddBill(true)} className="btn-primary">Add Your First Monthly Bill</button>}
        />
      )}
    </div>
  );
}
