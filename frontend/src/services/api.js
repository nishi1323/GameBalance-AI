import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('gb_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      const path = window.location.pathname;
      if (path !== '/login' && path !== '/register') {
        localStorage.removeItem('gb_token');
        localStorage.removeItem('gb_user');
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export const extractErrorMessage = (error, fallback = 'Something went wrong. Please try again.') => {
  const data = error?.response?.data;
  if (data?.validationErrors) {
    const firstKey = Object.keys(data.validationErrors)[0];
    if (firstKey) return data.validationErrors[firstKey];
  }
  return data?.message || error?.message || fallback;
};

export const authApi = {
  register: (payload) => api.post('/auth/register', payload).then((r) => r.data),
  login: (payload) => api.post('/auth/login', payload).then((r) => r.data),
  me: () => api.get('/auth/me').then((r) => r.data),
  updateProfile: (payload) => api.put('/auth/profile', payload).then((r) => r.data),
};

export const dashboardApi = {
  getOverview: () => api.get('/dashboard/overview').then((r) => r.data),
};

export const gamingApi = {
  getStats: () => api.get('/gaming/stats').then((r) => r.data),
  getActive: () => api.get('/gaming/active').then((r) => r.data),
  startSession: (payload) => api.post('/gaming/start', payload).then((r) => r.data),
  stopSession: (id, payload = {}) => api.post(`/gaming/stop/${id}`, payload).then((r) => r.data),
  deleteSession: (id) => api.delete(`/gaming/${id}`).then((r) => r.data),
};

export const riskApi = {
  analyze: (payload) => api.post('/risk/analyze', payload).then((r) => r.data),
  getHistory: () => api.get('/risk/history').then((r) => r.data),
  getLatest: () => api.get('/risk/latest').then((r) => r.data),
};

export const studyApi = {
  getSummary: () => api.get('/study').then((r) => r.data),
  createTask: (payload) => api.post('/study', payload).then((r) => r.data),
  updateTask: (id, payload) => api.put(`/study/${id}`, payload).then((r) => r.data),
  toggleTask: (id) => api.patch(`/study/${id}/toggle`).then((r) => r.data),
  deleteTask: (id) => api.delete(`/study/${id}`).then((r) => r.data),
};

export const plannerApi = {
  getOverview: () => api.get('/planner').then((r) => r.data),
  createPlan: (payload) => api.post('/planner/plans', payload).then((r) => r.data),
  deletePlan: (id) => api.delete(`/planner/plans/${id}`).then((r) => r.data),
  updateSettings: (payload) => api.put('/planner/settings', payload).then((r) => r.data),
};

export const sleepApi = {
  getSummary: () => api.get('/sleep').then((r) => r.data),
  createRecord: (payload) => api.post('/sleep', payload).then((r) => r.data),
  deleteRecord: (id) => api.delete(`/sleep/${id}`).then((r) => r.data),
};

export const chatApi = {
  getHistory: () => api.get('/chat/history').then((r) => r.data),
  sendMessage: (message) => api.post('/chat/message', { message }).then((r) => r.data),
  clearHistory: () => api.delete('/chat/history').then((r) => r.data),
};

export default api;
