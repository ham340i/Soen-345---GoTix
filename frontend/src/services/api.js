import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || '/api';

// ── Axios instance ────────────────────────────────────────────────────────────
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 10000,
});

// Attach JWT on every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Global error handling + auto-logout on 401
api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

// ── Auth ──────────────────────────────────────────────────────────────────────
export const authAPI = {
  register: (data) => api.post('/auth/register', data),
  login:    (data) => api.post('/auth/login', data),
};

// ── Events ────────────────────────────────────────────────────────────────────
export const eventsAPI = {
  getAll:    ()       => api.get('/events'),
  getById:   (id)     => api.get(`/events/${id}`),
  search:    (kw)     => api.get('/events/search', { params: { keyword: kw } }),
  filter:    (params) => api.get('/events/filter', { params }),
  create:    (data)   => api.post('/events', data),
  update:    (id, d)  => api.put(`/events/${id}`, d),
  cancel:    (id)     => api.delete(`/events/${id}`),
};

// ── Reservations ──────────────────────────────────────────────────────────────
export const reservationsAPI = {
  create:        (data)   => api.post('/reservations', data),
  getMy:         ()       => api.get('/reservations/my'),
  getById:       (id)     => api.get(`/reservations/${id}`),
  getByCode:     (code)   => api.get(`/reservations/code/${code}`),
  cancel:        (id)     => api.delete(`/reservations/${id}`),
  getByEvent:    (evId)   => api.get(`/reservations/event/${evId}`),
};

// ── Users ─────────────────────────────────────────────────────────────────────
export const usersAPI = {
  getMe:       ()       => api.get('/users/me'),
  updateMe:    (data)   => api.put('/users/me', data),
  getAll:      ()       => api.get('/users'),
  getById:     (id)     => api.get(`/users/${id}`),
  deactivate:  (id)     => api.delete(`/users/${id}`),
};

export default api;
