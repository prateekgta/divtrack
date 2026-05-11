'use client';

import Link from 'next/link';
import { useAuth } from '@/lib/auth-context';

export default function Home() {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-b from-forest-50 via-white to-earth-50">
        <div className="flex flex-col items-center gap-3">
          <div className="w-12 h-12 border-4 border-forest-200 border-t-forest-600 rounded-full animate-spin" />
          <p className="text-forest-600 font-medium animate-pulse">Growing your forest...</p>
        </div>
      </div>
    );
  }

  if (user) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-b from-forest-50 via-white to-earth-50">
        <div className="text-center space-y-5">
          <div className="text-6xl">🌳</div>
          <p className="text-xl text-earth-600">
            Welcome back, <span className="font-semibold text-forest-700">{user.name}</span>
          </p>
          <Link href="/dashboard" className="btn-primary inline-flex items-center gap-2 text-lg px-8 py-3">
            Enter Your Forest
            <span>→</span>
          </Link>
        </div>
      </div>
    );
  }

  const features = [
    { icon: '💵', title: 'Paycheck View', desc: 'See which bills your dividends cover each month' },
    { icon: '❄️', title: 'Snowball Simulator', desc: 'Watch your money grow year by year', pro: true },
    { icon: '🧭', title: 'Tax Optimizer', desc: 'Place every stock in the right account', pro: true },
  ];

  return (
    <div className="min-h-screen bg-gradient-to-b from-forest-50 via-white to-earth-50">
      <div className="max-w-5xl mx-auto px-6 py-16">
        <div className="flex flex-col items-center text-center py-20">
          <div className="text-7xl mb-6">🌳</div>
          <h1 className="text-5xl md:text-6xl font-display text-forest-800 mb-4">
            Your Dividend <span className="text-gradient">Forest</span>
          </h1>
          <p className="text-xl text-earth-600 max-w-xl leading-relaxed mb-10">
            Every stock is a tree. Every dividend is a leaf.{' '}
            <span className="font-semibold text-forest-600">Watch it grow.</span>
          </p>
          <div className="flex gap-4">
            <Link href="/register" className="btn-primary text-lg px-8 py-3 shadow-lg shadow-forest-200/50">
              Start Growing — Free
            </Link>
            <Link href="/login" className="btn-secondary text-lg px-8 py-3">
              Log In
            </Link>
          </div>
        </div>

        <div className="grid md:grid-cols-3 gap-6 mt-8">
          {features.map((f, i) => (
            <div key={i} className="card-hover text-center space-y-3 group">
              <div className="text-4xl group-hover:scale-110 transition-transform duration-300">{f.icon}</div>
              <div>
                <h3 className="font-display text-lg text-forest-800">{f.title}</h3>
                <p className="text-sm text-earth-500 mt-1">{f.desc}</p>
              </div>
              {f.pro && <span className="badge-leaf text-[10px]">PRO</span>}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
