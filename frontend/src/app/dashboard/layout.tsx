'use client';

import { useState } from 'react';
import { useAuth } from '@/lib/auth-context';
import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { useEffect } from 'react';

const navItems = [
  { href: '/dashboard', label: 'Forest', icon: '🌲' },
  { href: '/dashboard/paycheck', label: 'Paycheck', icon: '💵' },
  { href: '/dashboard/snowball', label: 'Snowball', icon: '❄️', pro: true },
  { href: '/dashboard/tax', label: 'Tax Optimizer', icon: '🧭', pro: true },
];

export default function DashboardLayout({ children }: { children: React.ReactNode }) {
  const { user, loading, logout, isPro } = useAuth();
  const pathname = usePathname();
  const router = useRouter();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  useEffect(() => {
    if (!loading && !user) router.push('/login');
  }, [loading, user, router]);

  if (loading) return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-b from-forest-50 via-white to-earth-50">
      <div className="flex flex-col items-center gap-3">
        <div className="w-10 h-10 border-4 border-forest-200 border-t-forest-600 rounded-full animate-spin" />
        <p className="text-forest-600 font-medium animate-pulse">Loading your forest...</p>
      </div>
    </div>
  );
  if (!user) return null;

  return (
    <div className="min-h-screen flex bg-earth-50/50">
      {/* Mobile overlay */}
      {sidebarOpen && (
        <div className="fixed inset-0 bg-black/20 z-20 md:hidden" onClick={() => setSidebarOpen(false)} />
      )}

      {/* Sidebar */}
      <aside className={`fixed md:sticky top-0 left-0 z-30 h-screen w-64 bg-white border-r border-earth-100 p-5 flex flex-col transition-transform duration-300 ${
        sidebarOpen ? 'translate-x-0' : '-translate-x-full md:translate-x-0'
      }`}>
        <div className="flex items-center justify-between mb-8">
          <Link href="/dashboard" className="flex items-center gap-2.5 text-forest-800" onClick={() => setSidebarOpen(false)}>
            <span className="text-2xl">🌳</span>
            <span className="font-display text-lg font-bold">DivTrack</span>
          </Link>
          <button className="md:hidden btn-ghost p-1" onClick={() => setSidebarOpen(false)}>✕</button>
        </div>

        <nav className="flex-1 space-y-1">
          {navItems.map(item => {
            const isActive = pathname === item.href;
            return (
              <Link key={item.href} href={item.href}
                onClick={() => setSidebarOpen(false)}
                className={`flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm transition-all duration-200 ${
                  isActive
                    ? 'bg-forest-50 text-forest-800 font-semibold shadow-sm'
                    : 'text-earth-600 hover:bg-earth-50 hover:text-earth-800'
                }`}>
                <span className="text-lg">{item.icon}</span>
                <span>{item.label}</span>
                {item.pro && !isPro && (
                  <span className="ml-auto badge-leaf text-[10px] font-semibold">PRO</span>
                )}
              </Link>
            );
          })}
        </nav>

        <div className="pt-4 border-t border-earth-100 space-y-3">
          <div className="px-3 py-2.5 bg-earth-50 rounded-xl">
            <p className="text-sm font-semibold text-earth-800">{user.name}</p>
            <div className="flex items-center gap-1.5 mt-0.5">
              {isPro ? (
                <span className="badge-forest text-[10px]"><span className="mr-0.5">🌟</span>Pro</span>
              ) : (
                <>
                  <span className="badge-earth text-[10px]">Free</span>
                  <Link href="/pricing" className="text-[11px] text-forest-600 hover:text-forest-700 font-medium hover:underline">
                    Upgrade
                  </Link>
                </>
              )}
            </div>
          </div>
          <button onClick={logout} className="btn-ghost w-full text-sm flex items-center justify-center gap-2">
            <span>🚪</span> Log Out
          </button>
        </div>
      </aside>

      {/* Main */}
      <div className="flex-1 flex flex-col min-h-screen">
        <header className="md:hidden sticky top-0 z-10 bg-white/80 backdrop-blur-md border-b border-earth-100 px-4 py-3 flex items-center gap-3">
          <button onClick={() => setSidebarOpen(true)} className="btn-ghost p-1 text-xl">☰</button>
          <span className="font-display font-bold text-forest-800">🌳 DivTrack</span>
        </header>

        <main className="flex-1 p-4 md:p-8 overflow-y-auto">
          {children}
        </main>
      </div>
    </div>
  );
}
