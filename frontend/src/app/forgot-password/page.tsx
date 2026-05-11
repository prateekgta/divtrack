'use client';

import { useState } from 'react';
import Link from 'next/link';
import { authApi } from '@/lib/api';

type Step = 'email' | 'questions' | 'reset' | 'done';

export default function ForgotPasswordPage() {
  const [step, setStep] = useState<Step>('email');
  const [email, setEmail] = useState('');
  const [questions, setQuestions] = useState<string[]>([]);
  const [answers, setAnswers] = useState(['', '']);
  const [resetToken, setResetToken] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleEmailSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const { data } = await authApi.forgotPassword(email);
      setQuestions(data.questions);
      setStep('questions');
    } catch (err: any) {
      setError(err.response?.data?.detail || err.response?.data?.message || 'No account found with that email');
    }
    setLoading(false);
  };

  const handleAnswersSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!answers[0].trim() || !answers[1].trim()) {
      setError('Please answer both questions');
      return;
    }
    setError('');
    setLoading(true);
    try {
      const { data } = await authApi.verifySecurity(email, answers);
      setResetToken(data.resetToken);
      setStep('reset');
    } catch (err: any) {
      setError(err.response?.data?.detail || err.response?.data?.message || 'Incorrect answers. Try again.');
    }
    setLoading(false);
  };

  const handleResetSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (newPassword.length < 8) {
      setError('Password must be at least 8 characters');
      return;
    }
    setError('');
    setLoading(true);
    try {
      await authApi.resetPassword(resetToken, newPassword);
      setStep('done');
    } catch (err: any) {
      setError(err.response?.data?.detail || err.response?.data?.message || 'Reset failed. Token may have expired.');
    }
    setLoading(false);
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-4 bg-gradient-to-b from-forest-50 via-white to-earth-50">
      <div className="w-full max-w-md">
        <div className="bg-white rounded-3xl shadow-xl shadow-earth-200/30 border border-earth-100 p-8 space-y-6">
          {step !== 'done' && (
            <div className="text-center space-y-2">
              <div className="text-5xl mb-2">🔐</div>
              <h1 className="text-2xl font-display text-forest-800">
                {step === 'email' && 'Forgot Password'}
                {step === 'questions' && 'Security Questions'}
                {step === 'reset' && 'Reset Password'}
              </h1>
              <p className="text-earth-500 text-sm">
                {step === 'email' && 'Verify your identity to regain access'}
                {step === 'questions' && 'Answer your security questions'}
                {step === 'reset' && 'Choose a new password'}
              </p>
            </div>
          )}

          {/* Steps indicator */}
          {step !== 'done' && (
            <div className="flex items-center justify-center gap-2">
              {['email', 'questions', 'reset'].map((s, i) => (
                <div key={s} className="flex items-center gap-2">
                  <div className={`w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold ${
                    ['email', 'questions', 'reset'].indexOf(step) >= i
                      ? 'bg-forest-600 text-white'
                      : 'bg-earth-100 text-earth-400'
                  }`}>
                    {i + 1}
                  </div>
                  {i < 2 && <div className={`w-8 h-0.5 ${['email', 'questions', 'reset'].indexOf(step) > i ? 'bg-forest-600' : 'bg-earth-100'}`} />}
                </div>
              ))}
            </div>
          )}

          {error && (
            <div className="bg-red-50 border border-red-100 text-red-700 px-4 py-3 rounded-xl text-sm flex items-center gap-2">
              <span>⚠️</span>
              <span>{error}</span>
            </div>
          )}

          {/* Step 1: Enter Email */}
          {step === 'email' && (
            <form onSubmit={handleEmailSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-earth-700 mb-1.5">Email</label>
                <input type="email" className="input-field" value={email}
                  onChange={e => setEmail(e.target.value)} required placeholder="you@example.com" />
              </div>
              <button type="submit" disabled={loading}
                className="btn-primary w-full flex items-center justify-center gap-2 py-3">
                {loading ? <><span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" /> Checking...</>
                  : 'Continue'}
              </button>
              <p className="text-center text-sm text-earth-500">
                Remember your password?{' '}
                <Link href="/login" className="text-forest-600 hover:text-forest-700 font-semibold">Log in</Link>
              </p>
            </form>
          )}

          {/* Step 2: Answer Security Questions */}
          {step === 'questions' && (
            <form onSubmit={handleAnswersSubmit} className="space-y-4">
              <p className="text-xs text-earth-400 text-center">{email}</p>
              {questions.map((q, i) => (
                <div key={i}>
                  <label className="block text-sm font-medium text-earth-700 mb-1.5">{q}</label>
                  <input type="text" className="input-field" value={answers[i]}
                    onChange={e => { const a = [...answers]; a[i] = e.target.value; setAnswers(a); }}
                    required placeholder="Your answer" autoFocus={i === 0} />
                </div>
              ))}
              <div className="flex gap-3">
                <button type="button" onClick={() => { setStep('email'); setAnswers(['', '']); setError(''); }}
                  className="btn-ghost flex-1 py-3 text-sm">Back</button>
                <button type="submit" disabled={loading}
                  className="btn-primary flex-1 flex items-center justify-center gap-2 py-3 text-sm">
                  {loading ? <><span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" /> Verifying...</>
                    : 'Verify Answers'}
                </button>
              </div>
            </form>
          )}

          {/* Step 3: New Password */}
          {step === 'reset' && (
            <form onSubmit={handleResetSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-earth-700 mb-1.5">New Password</label>
                <input type="password" className="input-field" value={newPassword}
                  onChange={e => setNewPassword(e.target.value)} required placeholder="At least 8 characters"
                  minLength={8} autoFocus />
                <p className="text-xs text-earth-400 mt-1">Minimum 8 characters</p>
              </div>
              <button type="submit" disabled={loading}
                className="btn-primary w-full flex items-center justify-center gap-2 py-3">
                {loading ? <><span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" /> Resetting...</>
                  : 'Reset Password'}
              </button>
            </form>
          )}

          {/* Step 4: Done */}
          {step === 'done' && (
            <div className="text-center space-y-6 py-4">
              <div className="text-6xl">✅</div>
              <h1 className="text-2xl font-display text-forest-800">Password Reset!</h1>
              <p className="text-earth-500 text-sm">Your password has been changed successfully.</p>
              <Link href="/login" className="btn-primary inline-flex items-center gap-2 px-8 py-3">
                Log In with New Password
              </Link>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
