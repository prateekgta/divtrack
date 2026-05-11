'use client';

import { useState } from 'react';
import { useAuth } from '@/lib/auth-context';
import { useRouter } from 'next/navigation';
import Link from 'next/link';

const PLANS: {
  name: string; price: number; badge: string; badgeColor: string; buttonClass: string;
  popular?: boolean; features: { text: string; ok: boolean; pro?: boolean }[];
}[] = [
  {
    name: 'Free',
    price: 0,
    badge: '🌱',
    badgeColor: 'bg-earth-100 text-earth-700',
    buttonClass: 'btn-ghost',
    features: [
      { text: 'Up to 10 holdings', ok: true },
      { text: 'Stock browser with 155 stocks', ok: true },
      { text: 'Performance charts (1m–3y)', ok: true },
      { text: 'Investment simulator', ok: true },
      { text: 'Portfolio templates (preview)', ok: true },
      { text: 'Up to 3 price alerts', ok: true },
      { text: 'Snowball simulator', ok: false, pro: true },
      { text: 'Tax bucket optimizer', ok: false, pro: true },
      { text: 'Dividend paycheck calendar', ok: true },
    ],
  },
  {
    name: 'Pro',
    price: 5,
    badge: '🌟',
    badgeColor: 'bg-gradient-to-r from-amber-400 to-amber-500 text-white',
    buttonClass: 'btn-primary',
    popular: true,
    features: [
      { text: 'Unlimited holdings', ok: true },
      { text: 'Stock browser with 155 stocks', ok: true },
      { text: 'Performance charts (1m–3y)', ok: true },
      { text: 'Investment simulator', ok: true },
      { text: 'Portfolio templates (full adoption)', ok: true },
      { text: 'Unlimited price alerts', ok: true },
      { text: 'Snowball simulator', ok: true },
      { text: 'Tax bucket optimizer', ok: true },
      { text: 'Dividend paycheck calendar', ok: true },
      { text: 'Bill-to-dividend mapping', ok: true },
      { text: 'Priority support', ok: true },
    ],
  },
];

export default function PricingPage() {
  const { user, isPro } = useAuth();
  const router = useRouter();
  const [billing, setBilling] = useState<'monthly' | 'annual'>('monthly');

  return (
    <div className="min-h-screen bg-gradient-to-b from-forest-50 via-white to-earth-50">
      {/* Nav */}
      <nav className="flex items-center justify-between px-6 py-4 max-w-6xl mx-auto">
        <Link href="/dashboard" className="flex items-center gap-2 text-forest-800">
          <span className="text-2xl">🌳</span>
          <span className="font-display text-lg font-bold">DivTrack</span>
        </Link>
        <div className="flex items-center gap-3">
          {user ? (
            <Link href="/dashboard" className="btn-ghost text-sm">Dashboard</Link>
          ) : (
            <>
              <Link href="/login" className="btn-ghost text-sm">Log In</Link>
              <Link href="/register" className="btn-primary text-sm">Sign Up</Link>
            </>
          )}
        </div>
      </nav>

      {/* Header */}
      <div className="text-center pt-16 pb-12 px-4">
        <h1 className="text-4xl md:text-5xl font-bold font-display text-forest-800 mb-4">
          {isPro ? 'You\'re already Pro 🌟' : 'Grow your forest faster'}
        </h1>
        <p className="text-lg text-earth-500 max-w-xl mx-auto">
          {isPro
            ? 'Enjoy all Pro features. Your dividend snowball is rolling.'
            : 'Unlock the snowball simulator, tax optimizer, unlimited holdings, and more.'}
        </p>
      </div>

      {/* Plans */}
      <div className="max-w-5xl mx-auto px-4 pb-24">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 max-w-3xl mx-auto">
          {PLANS.map(plan => {
            const annualPrice = plan.price * 10; // 2 months free
            const effectiveMonthly = billing === 'annual' ? annualPrice / 12 : plan.price;

            return (
              <div key={plan.name}
                className={`relative bg-white rounded-3xl border-2 p-8 shadow-sm transition-all duration-300 ${
                  plan.popular ? 'border-forest-300 shadow-forest-200/20 shadow-lg scale-[1.02]' : 'border-earth-100'
                }`}>
                {plan.popular && (
                  <div className="absolute -top-3 left-1/2 -translate-x-1/2 px-4 py-1 rounded-full bg-gradient-to-r from-forest-500 to-forest-600 text-white text-xs font-semibold shadow-lg">
                    Most Popular
                  </div>
                )}

                <div className="flex items-center justify-between mb-6">
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="text-2xl">{plan.badge}</span>
                      <h2 className="text-xl font-bold text-earth-800">{plan.name}</h2>
                    </div>
                  </div>
                  {plan.price > 0 && (
                    <div className="text-right">
                      <p className="text-3xl font-bold text-forest-700">${effectiveMonthly}<span className="text-base text-earth-400 font-normal">/mo</span></p>
                      {billing === 'annual' && (
                        <p className="text-xs text-forest-600 font-medium">${annualPrice}/yr (save 17%)</p>
                      )}
                    </div>
                  )}
                </div>

                {plan.price === 0 && (
                  <p className="text-3xl font-bold text-earth-400 mb-6">Free</p>
                )}

                {plan.price > 0 && (
                  <div className="flex items-center gap-2 mb-6 bg-earth-50 rounded-xl p-1">
                    <button onClick={() => setBilling('monthly')}
                      className={`flex-1 py-2 rounded-lg text-sm font-semibold transition-all ${
                        billing === 'monthly' ? 'bg-white text-forest-700 shadow-sm' : 'text-earth-500 hover:text-earth-700'
                      }`}>
                      Monthly
                    </button>
                    <button onClick={() => setBilling('annual')}
                      className={`flex-1 py-2 rounded-lg text-sm font-semibold transition-all ${
                        billing === 'annual' ? 'bg-white text-forest-700 shadow-sm' : 'text-earth-500 hover:text-earth-700'
                      }`}>
                      Annual <span className="text-forest-600 text-[10px]">-17%</span>
                    </button>
                  </div>
                )}

                <div className="space-y-3 mb-8">
                  {plan.features.map((f, i) => (
                    <div key={i} className="flex items-center gap-3 text-sm">
                      {f.pro && !plan.name.includes('Pro') ? (
                        <span className="w-5 h-5 rounded-full bg-earth-100 flex items-center justify-center text-[10px] text-earth-400 shrink-0">★</span>
                      ) : (
                        <span className={`w-5 h-5 rounded-full flex items-center justify-center text-[10px] shrink-0 ${
                          f.ok ? 'bg-forest-100 text-forest-600' : 'bg-earth-100 text-earth-400'
                        }`}>
                          {f.ok ? '✓' : '✕'}
                        </span>
                      )}
                      <span className={f.ok ? 'text-earth-700' : 'text-earth-400'}>{f.text}</span>
                    </div>
                  ))}
                </div>

                {isPro && plan.name === 'Pro' ? (
                  <div className="w-full py-3 rounded-xl text-center text-sm font-semibold bg-forest-50 text-forest-700 border border-forest-200">
                    ✓ Current Plan
                  </div>
                ) : user && plan.name === 'Free' && !isPro ? (
                  <div className="w-full py-3 rounded-xl text-center text-sm font-semibold bg-earth-50 text-earth-600 border border-earth-200">
                    Current Plan
                  </div>
                ) : plan.name === 'Pro' && !isPro ? (
                  <button onClick={() => router.push('/register')}
                    className="btn-primary w-full py-3 text-sm">
                    Upgrade to Pro — ${effectiveMonthly}/mo
                  </button>
                ) : (
                  <button onClick={() => router.push(plan.price === 0 ? '/register' : '/pricing')}
                    className={`${plan.buttonClass} w-full py-3 text-sm`}>
                    {plan.price === 0 ? 'Get Started Free' : 'Upgrade'}
                  </button>
                )}
              </div>
            );
          })}
        </div>

        {/* Feature Comparison */}
        <div className="mt-16 max-w-4xl mx-auto">
          <h2 className="text-2xl font-bold text-forest-800 text-center mb-8">What you get with each plan</h2>
          <div className="bg-white rounded-3xl border border-earth-100 overflow-hidden shadow-sm">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-earth-100 bg-earth-50/80">
                  <th className="text-left py-4 px-6 text-xs font-semibold text-earth-500 uppercase">Feature</th>
                  <th className="text-center py-4 px-6 text-xs font-semibold text-earth-500 uppercase">Free</th>
                  <th className="text-center py-4 px-6 text-xs font-semibold text-forest-700 uppercase bg-forest-50/50">Pro</th>
                </tr>
              </thead>
              <tbody>
                {[
                  ['Holdings', '10 max', 'Unlimited'],
                  ['Stock Browser', '✓', '✓'],
                  ['Performance Charts', '✓', '✓'],
                  ['Investment Simulator', '✓', '✓'],
                  ['Portfolio Templates', 'Preview only', 'Full adoption'],
                  ['Price Alerts', '3 max', 'Unlimited'],
                  ['Snowball Simulator', '—', '✓'],
                  ['Tax Bucket Optimizer', '—', '✓'],
                  ['Bill-to-Dividend Mapping', '—', '✓'],
                  ['Priority Support', '—', '✓'],
                ].map(([feature, free, pro], i) => (
                  <tr key={i} className={`border-b border-earth-50 ${i % 2 === 0 ? 'bg-white' : 'bg-earth-50/30'}`}>
                    <td className="py-3.5 px-6 text-earth-700 font-medium">{feature}</td>
                    <td className="text-center py-3.5 px-6 text-earth-500">{free}</td>
                    <td className="text-center py-3.5 px-6 text-forest-700 font-semibold bg-forest-50/20">{pro}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* FAQ */}
        <div className="mt-16 max-w-2xl mx-auto">
          <h2 className="text-2xl font-bold text-forest-800 text-center mb-8">Frequently Asked</h2>
          <div className="space-y-4">
            {[
              { q: 'Can I cancel anytime?', a: 'Yes. No lock-in contracts. Cancel and keep access until the end of your billing period.' },
              { q: 'What payment methods do you accept?', a: 'Credit/debit cards via Stripe. Apple Pay and Google Pay coming soon.' },
              { q: 'Is my data secure?', a: 'Yes. All data encrypted at rest. JWT-based auth with refresh tokens. We never share your portfolio data.' },
              { q: 'How does the tax optimizer work?', a: 'It analyzes your holdings and recommends which account type (Taxable, Traditional IRA, Roth IRA) each stock should go in based on its yield, to minimize annual taxes.' },
              { q: 'What is the snowball simulator?', a: 'It projects your dividend growth year-by-year using your actual holdings, contribution rate, and reinvestment settings. Shows when you\'ll reach financial independence.' },
            ].map((faq, i) => (
              <details key={i} className="group bg-white rounded-2xl border border-earth-100 overflow-hidden">
                <summary className="px-6 py-4 cursor-pointer text-sm font-semibold text-earth-800 hover:text-forest-700 transition-colors list-none flex items-center justify-between">
                  {faq.q}
                  <span className="text-earth-400 group-open:rotate-180 transition-transform text-xs">▼</span>
                </summary>
                <div className="px-6 pb-4 text-sm text-earth-500 leading-relaxed">{faq.a}</div>
              </details>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
