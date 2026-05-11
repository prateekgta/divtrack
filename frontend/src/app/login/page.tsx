'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useAuth } from '@/lib/auth-context';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const router = useRouter();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(email, password);
      router.push('/dashboard');
    } catch {
      setError('Invalid email or password');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-4 bg-gradient-to-b from-forest-50 via-white to-earth-50">
      <div className="w-full max-w-md">
        <div className="bg-white rounded-3xl shadow-xl shadow-earth-200/30 border border-earth-100 p-8 space-y-6">
          <div className="text-center space-y-2">
            <div className="text-5xl mb-2">🌳</div>
            <h1 className="text-2xl font-display text-forest-800">Welcome Back</h1>
            <p className="text-earth-500 text-sm">Log in to your dividend forest</p>
          </div>

          {error && (
            <div className="bg-red-50 border border-red-100 text-red-700 px-4 py-3 rounded-xl text-sm flex items-center gap-2">
              <span>⚠️</span>
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-earth-700 mb-1.5">Email</label>
              <input type="email" className="input-field" value={email}
                onChange={e => setEmail(e.target.value)} required placeholder="you@example.com" />
            </div>
            <div>
              <label className="block text-sm font-medium text-earth-700 mb-1.5">Password</label>
              <input type="password" className="input-field" value={password}
                onChange={e => setPassword(e.target.value)} required placeholder="Enter your password" />
            </div>
            <button type="submit" disabled={loading} className="btn-primary w-full flex items-center justify-center gap-2">
              {loading ? (
                <><span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" /> Logging in...</>
              ) : 'Log In'}
            </button>
          </form>

          <p className="text-center text-sm text-earth-500">
            No forest yet?{' '}
            <Link href="/register" className="text-forest-600 hover:text-forest-700 font-semibold">Start growing</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
