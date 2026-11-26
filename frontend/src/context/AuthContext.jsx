import { createContext, useContext, useEffect, useState } from 'react';
import { authApi } from '../services/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('gb_token'));
  const [user, setUser] = useState(() => {
    try {
      const saved = localStorage.getItem('gb_user');
      return saved ? JSON.parse(saved) : null;
    } catch {
      return null;
    }
  });
  const [loading, setLoading] = useState(Boolean(localStorage.getItem('gb_token')));

  useEffect(() => {
    let active = true;
    if (!token) {
      setLoading(false);
      return;
    }
    authApi
      .me()
      .then((profile) => {
        if (active) {
          setUser(profile);
          localStorage.setItem('gb_user', JSON.stringify(profile));
        }
      })
      .catch(() => {
        if (active) {
          localStorage.removeItem('gb_token');
          localStorage.removeItem('gb_user');
          setToken(null);
          setUser(null);
        }
      })
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
  }, [token]);

  const saveSession = (authResponse) => {
    localStorage.setItem('gb_token', authResponse.token);
    localStorage.setItem('gb_user', JSON.stringify(authResponse.user));
    setToken(authResponse.token);
    setUser(authResponse.user);
  };

  const login = async (credentials) => {
    const data = await authApi.login(credentials);
    saveSession(data);
    return data;
  };

  const register = async (payload) => {
    const data = await authApi.register(payload);
    saveSession(data);
    return data;
  };

  const updateUser = (updatedUser) => {
    setUser(updatedUser);
    localStorage.setItem('gb_user', JSON.stringify(updatedUser));
  };

  const logout = () => {
    localStorage.removeItem('gb_token');
    localStorage.removeItem('gb_user');
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{
        token,
        user,
        loading,
        isAuthenticated: Boolean(token && user),
        login,
        register,
        logout,
        updateUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
