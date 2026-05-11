'use client';

import { useEffect, useState, createContext, useContext, useCallback, type ReactNode } from 'react';

/* ─── Toast ─── */
interface Toast { id: number; message: string; type: 'success' | 'error' | 'info' }
const ToastContext = createContext<{ toast: (msg: string, type?: Toast['type']) => void }>(null!);
export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([]);
  const toast = useCallback((message: string, type: Toast['type'] = 'success') => {
    const id = Date.now();
    setToasts(p => [...p, { id, message, type }]);
    setTimeout(() => setToasts(p => p.filter(t => t.id !== id)), 3500);
  }, []);
  return (
    <ToastContext.Provider value={{ toast }}>
      {children}
      <div className="fixed bottom-6 right-6 z-50 flex flex-col gap-2">
        {toasts.map(t => (
          <div key={t.id} className={`animate-slide-up px-5 py-3 rounded-2xl shadow-xl text-white text-sm font-medium backdrop-blur-sm ${
            t.type === 'success' ? 'bg-forest-600/95' : t.type === 'error' ? 'bg-red-600/95' : 'bg-earth-700/95'
          }`}>
            {t.type === 'success' && '✓ '}{t.type === 'error' && '✕ '}{t.message}
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}
export const useToast = () => useContext(ToastContext);

/* ─── Skeleton ─── */
export function Skeleton({ className = '' }: { className?: string }) {
  return <div className={`animate-pulse bg-earth-200/70 rounded-xl ${className}`} />;
}
export function CardSkeleton() {
  return (
    <div className="bg-white rounded-2xl border border-earth-100 p-6 space-y-4">
      <Skeleton className="h-4 w-24" />
      <Skeleton className="h-8 w-32" />
      <Skeleton className="h-3 w-20" />
    </div>
  );
}
export function TableSkeleton({ rows = 5 }: { rows?: number }) {
  return (
    <div className="space-y-3">
      {Array.from({ length: rows }).map((_, i) => (
        <div key={i} className="flex items-center gap-4 p-3">
          <Skeleton className="h-10 w-10 rounded-full" />
          <div className="flex-1 space-y-2">
            <Skeleton className="h-4 w-24" />
            <Skeleton className="h-3 w-16" />
          </div>
          <Skeleton className="h-4 w-20" />
        </div>
      ))}
    </div>
  );
}

/* ─── Stat Card ─── */
export function StatCard({ label, value, icon, sub, color = 'forest' }: {
  label: string; value: string; icon: string; sub?: string; color?: 'forest' | 'leaf' | 'earth'
}) {
  const colors = { forest: 'from-forest-500 to-forest-700', leaf: 'from-leaf-500 to-leaf-700', earth: 'from-earth-500 to-earth-700' };
  return (
    <div className="relative bg-white rounded-2xl border border-earth-100 p-5 overflow-hidden group hover:shadow-lg transition-all duration-300">
      <div className={`absolute inset-0 opacity-[0.03] bg-gradient-to-br ${colors[color]}`} />
      <div className="relative">
        <div className="flex items-start justify-between">
          <p className="text-sm text-earth-500 font-medium">{label}</p>
          <span className="text-xl">{icon}</span>
        </div>
        <p className={`text-2xl font-bold mt-1.5 text-${color === 'leaf' ? 'leaf' : 'forest'}-700`}>{value}</p>
        {sub && <p className="text-xs text-earth-400 mt-0.5">{sub}</p>}
      </div>
    </div>
  );
}

/* ─── Empty State ─── */
export function EmptyState({ icon, title, desc, action }: {
  icon: string; title: string; desc: string; action?: ReactNode
}) {
  return (
    <div className="flex flex-col items-center justify-center py-16 px-8 text-center">
      <div className="text-6xl mb-4 opacity-60">{icon}</div>
      <h3 className="text-lg font-semibold text-earth-700 mb-1">{title}</h3>
      <p className="text-sm text-earth-400 max-w-sm mb-4">{desc}</p>
      {action}
    </div>
  );
}

/* ─── Progress Bar ─── */
export function ProgressBar({ value, max = 100, color = 'forest' }: { value: number; max?: number; color?: string }) {
  const pct = Math.min((value / max) * 100, 100);
  return (
    <div className="h-2.5 bg-earth-100 rounded-full overflow-hidden">
      <div className={`h-full rounded-full transition-all duration-1000 ease-out bg-${color}-500`}
        style={{ width: `${pct}%` }} />
    </div>
  );
}

/* ─── Animated number counter ─── */
export function AnimatedNumber({ value, prefix = '', suffix = '', decimals = 0 }: {
  value: number; prefix?: string; suffix?: string; decimals?: number
}) {
  const [display, setDisplay] = useState(0);
  useEffect(() => {
    let start = 0;
    const dur = 800;
    const step = (value - start) / (dur / 16);
    let current = start;
    const raf = setInterval(() => {
      current += step;
      if ((step > 0 && current >= value) || (step < 0 && current <= value)) {
        setDisplay(value);
        clearInterval(raf);
      } else {
        setDisplay(current);
      }
    }, 16);
    return () => clearInterval(raf);
  }, [value]);
  return <>{prefix}{display.toLocaleString(undefined, { minimumFractionDigits: decimals, maximumFractionDigits: decimals })}{suffix}</>;
}
