'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useAuth } from '@/lib/auth-context';

const COMMON_QUESTIONS = [
  'What is your pet name?',
  'What city were you born in?',
  'What is your mother maiden name?',
  'What elementary school did you attend?',
  'What was the make of your first car?',
  'What is your favorite book?',
];

export default function RegisterPage() {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [q1, setQ1] = useState(COMMON_QUESTIONS[0]);
  const [a1, setA1] = useState('');
  const [q2, setQ2] = useState(COMMON_QUESTIONS[1]);
  const [a2, setA2] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { register } = useAuth();
  const router = useRouter();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await register(email, password, name, q1, a1, q2, a2);
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

            <hr className="border-earth-200" />
            <p className="text-xs text-earth-500 -mb-2">Security questions — used to recover your account</p>

            <div>
              <label className="block text-sm font-medium text-earth-700 mb-1.5">Security Question 1</label>
              <select className="input-field" value={q1} onChange={e => setQ1(e.target.value)}>
                {COMMON_QUESTIONS.map(q => <option key={q} value={q}>{q}</option>)}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-earth-700 mb-1.5">Answer 1</label>
              <input type="text" className="input-field" value={a1}
                onChange={e => setA1(e.target.value)} required placeholder="Your answer" />
            </div>
            <div>
              <label className="block text-sm font-medium text-earth-700 mb-1.5">Security Question 2</label>
              <select className="input-field" value={q2} onChange={e => setQ2(e.target.value)}>
                {COMMON_QUESTIONS.map(q => <option key={q} value={q}>{q}</option>)}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-earth-700 mb-1.5">Answer 2</label>
              <input type="text" className="input-field" value={a2}
                onChange={e => setA2(e.target.value)} required placeholder="Your answer" />
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
