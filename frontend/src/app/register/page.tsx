'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useAuth } from '@/lib/auth-context';

export default function RegisterPage() {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { register } = useAuth();
  const router = useRouter();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await register(email, password, name);
      router.push('/dashboard');
    } catch (err: any) {
      setError(err.response?.data?.detail || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-4 bg-gradient-to-b from-forest-50 via-white to-earth-50">
      <div className="w-full max-w-md">
        <div className="bg-white rounded-3xl shadow-xl shadow-earth-200/30 border border-earth-100 p-8 space-y-6">
          <div className="text-center space-y-2">
            <div className="text-5xl mb-2">🌱</div>
            <h1 className="text-2xl font-display text-forest-800">Plant Your Forest</h1>
            <p className="text-earth-500 text-sm">Start tracking your dividend portfolio</p>
          </div>

          {error && (
            <div className="bg-red-50 border border-red-100 text-red-700 px-4 py-3 rounded-xl text-sm flex items-center gap-2">
              <span>⚠️</span>
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-earth-700 mb-1.5">Name</label>
              <input type="text" className="input-field" value={name}
                onChange={e => setName(e.target.value)} required placeholder="Your name" />
            </div>
            <div>
              <label className="block text-sm font-medium text-earth-700 mb-1.5">Email</label>
              <input type="email" className="input-field" value={email}
                onChange={e => setEmail(e.target.value)} required placeholder="you@example.com" />
            </div>
            <div>
              <label className="block text-sm font-medium text-earth-700 mb-1.5">Password</label>
              <input type="password" className="input-field" value={password}
                onChange={e => setPassword(e.target.value)} required minLength={8} placeholder="At least 8 characters" />
            </div>
            <button type="submit" disabled={loading} className="btn-primary w-full flex items-center justify-center gap-2">
              {loading ? (
                <><span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" /> Planting...</>
              ) : 'Start Growing'}
            </button>
          </form>

          <p className="text-center text-sm text-earth-500">
            Already have a forest?{' '}
            <Link href="/login" className="text-forest-600 hover:text-forest-700 font-semibold">Log in</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
