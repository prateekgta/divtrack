'use client';

import React, { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import { authApi } from './api';

interface User { id: string; email: string; name: string; plan: string; monthlyExpenses?: number; }

interface AuthContextType {
  user: User | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string, name: string, securityQuestion1: string, securityAnswer1: string, securityQuestion2: string, securityAnswer2: string) => Promise<void>;
  logout: () => Promise<void>;
  isPro: boolean;
}

const AuthContext = createContext<AuthContextType>(null!);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = sessionStorage.getItem('refreshToken');
    if (token) {
      authApi.refresh(token).then(({ data }) => {
        (window as any).__accessToken = data.accessToken;
        sessionStorage.setItem('refreshToken', data.refreshToken);
        setUser(data.user);
      }).catch(() => {
        sessionStorage.removeItem('refreshToken');
      }).finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, []);

  const login = async (email: string, password: string) => {
    const { data } = await authApi.login({ email, password });
    (window as any).__accessToken = data.accessToken;
    sessionStorage.setItem('refreshToken', data.refreshToken);
    setUser(data.user);
  };

  const register = async (email: string, password: string, name: string, securityQuestion1: string, securityAnswer1: string, securityQuestion2: string, securityAnswer2: string) => {
    const { data } = await authApi.register({ email, password, name, securityQuestion1, securityAnswer1, securityQuestion2, securityAnswer2 });
    (window as any).__accessToken = data.accessToken;
    sessionStorage.setItem('refreshToken', data.refreshToken);
    setUser(data.user);
  };

  const logout = async () => {
    const rt = sessionStorage.getItem('refreshToken');
    if (rt) await authApi.logout(rt).catch(() => {});
    (window as any).__accessToken = null;
    sessionStorage.removeItem('refreshToken');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout, isPro: user?.plan === 'PRO' }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
