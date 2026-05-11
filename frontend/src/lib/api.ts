import axios from 'axios';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

const api = axios.create({ baseURL: API_URL });

api.interceptors.request.use((config) => {
  if (typeof window !== 'undefined') {
    const token = (window as any).__accessToken;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
  }
  return config;
});

api.interceptors.response.use(
  (r) => r,
  async (error) => {
    if (error.response?.status === 401 && typeof window !== 'undefined') {
      const stored = sessionStorage.getItem('refreshToken');
      if (stored) {
        try {
          const { data } = await axios.post(`${API_URL}/api/auth/refresh`, { refreshToken: stored });
          (window as any).__accessToken = data.accessToken;
          sessionStorage.setItem('refreshToken', data.refreshToken);
          error.config.headers.Authorization = `Bearer ${data.accessToken}`;
          return axios(error.config);
        } catch {
          (window as any).__accessToken = null;
          sessionStorage.removeItem('refreshToken');
          window.location.href = '/login';
        }
      } else {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default api;

export const authApi = {
  register: (data: { email: string; password: string; name: string; securityQuestion1: string; securityAnswer1: string; securityQuestion2: string; securityAnswer2: string }) =>
    api.post('/api/auth/register', data),
  login: (data: { email: string; password: string }) =>
    api.post('/api/auth/login', data),
  refresh: (refreshToken: string) =>
    api.post('/api/auth/refresh', { refreshToken }),
  logout: (refreshToken: string) =>
    api.post('/api/auth/logout', { refreshToken }),
  forgotPassword: (email: string) =>
    api.post('/api/auth/forgot-password', { email }),
  verifySecurity: (email: string, answers: string[]) =>
    api.post('/api/auth/verify-security', { email, answers }),
  resetPassword: (resetToken: string, newPassword: string) =>
    api.post('/api/auth/reset-password', { resetToken, newPassword }),
};

export const portfolioApi = {
  get: () => api.get('/api/portfolio'),
  addHolding: (data: any) => api.post('/api/portfolio/holdings', data),
  removeHolding: (id: string) => api.delete(`/api/portfolio/holdings/${id}`),
  getAlerts: () => api.get('/api/portfolio/alerts'),
  createAlert: (data: any) => api.post('/api/portfolio/alerts', data),
  deleteAlert: (id: string) => api.delete(`/api/portfolio/alerts/${id}`),
};

export const marketApi = {
  getStocks: () => api.get('/api/market/stocks'),
  getStock: (ticker: string) => api.get(`/api/market/stocks/${ticker}`),
  search: (q: string) => api.get(`/api/market/search?q=${q}`),
  getPerformance: (ticker: string, range: string = '1y') =>
    api.get(`/api/market/${ticker}/performance?range=${range}`),
  simulate: (ticker: string, invested: number, buyDate: string) =>
    api.get(`/api/market/${ticker}/simulate?invested=${invested}&buyDate=${buyDate}`),
  getTopPerformers: (limit: number = 10) => api.get(`/api/market/top-performers?limit=${limit}`),
  getByCategory: (category: string) => api.get(`/api/market/by-category/${category}`),
  getTemplates: () => api.get('/api/market/templates'),
  getTemplate: (id: string, budget?: number) =>
    api.get(`/api/market/templates/${id}${budget ? `?budget=${budget}` : ''}`),
};

export const dividendApi = {
  getPaycheck: () => api.get('/api/dividend/paycheck'),
  getSnowball: (data: any) => api.post('/api/dividend/snowball', data),
  getTaxAdvice: () => api.get('/api/dividend/tax'),
  addBillMapping: (data: any) => api.post('/api/dividend/bills', data),
};
