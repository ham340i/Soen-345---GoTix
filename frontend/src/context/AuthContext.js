import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { authAPI, usersAPI } from '../services/api';

const AuthContext = createContext(null);

// Helper: decode JWT payload
const decodeToken = (token) => {
  try {
    const parts = token.split('.');
    if (parts.length !== 3) return null;
    const decoded = JSON.parse(atob(parts[1]));
    return decoded;
  } catch (e) {
    return null;
  }
};

// Helper: check if token is expired
const isTokenExpired = (token) => {
  const decoded = decodeToken(token);
  if (!decoded || !decoded.exp) return true;
  return decoded.exp < Date.now() / 1000;
};

export function AuthProvider({ children }) {
  const [user, setUser]       = useState(null);
  const [token, setToken]     = useState(null);
  const [loading, setLoading] = useState(true);

  // Restore session from localStorage on mount + validate token expiry
  useEffect(() => {
    const savedToken = localStorage.getItem('token');
    const savedUser  = localStorage.getItem('user');
    if (savedToken && savedUser) {
      // Check if token is expired — if so, clear it
      if (isTokenExpired(savedToken)) {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
      } else {
        setToken(savedToken);
        setUser(JSON.parse(savedUser));
      }
    }
    setLoading(false);
  }, []);

  /** Fetch the full user profile (includes notificationPreference) and sync state. */
  const refreshUser = useCallback(async () => {
    try {
      const res = await usersAPI.getMe();
      const fullProfile = res.data;
      setUser(prev => {
        const merged = { ...prev, ...fullProfile };
        localStorage.setItem('user', JSON.stringify(merged));
        return merged;
      });
    } catch (e) {
      // Non-fatal: keep the existing user state
    }
  }, []);

  const login = useCallback(async (email, password) => {
    const res = await authAPI.login({ email, password });
    const { token: t, ...userData } = res.data;
    setToken(t);
    setUser(userData);
    localStorage.setItem('token', t);
    localStorage.setItem('user', JSON.stringify(userData));
    // Fetch full profile so notificationPreference is available
    setTimeout(refreshUser, 0);
    return userData;
  }, [refreshUser]);

  const register = useCallback(async (name, email, phoneNumber, password, notificationPreference) => {
    const res = await authAPI.register({ name, email, phoneNumber, password });
    const { token: t, ...userData } = res.data;
    setToken(t);
    setUser(userData);
    localStorage.setItem('token', t);
    localStorage.setItem('user', JSON.stringify(userData));
    // Persist the chosen preference right after registration
    if (notificationPreference) {
      try {
        await usersAPI.updateMe({ notificationPreference });
        userData.notificationPreference = notificationPreference;
        localStorage.setItem('user', JSON.stringify(userData));
      } catch (e) { /* non-fatal */ }
    }
    return userData;
  }, []);

  const logout = useCallback(() => {
    setToken(null);
    setUser(null);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  }, []);

  const isAdmin    = user?.role === 'ADMIN';
  const isLoggedIn = !!token;

  return (
    <AuthContext.Provider value={{ user, token, loading, isAdmin, isLoggedIn, login, register, logout, refreshUser }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
};
