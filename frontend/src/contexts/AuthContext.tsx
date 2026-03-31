import React, { createContext, useCallback, useContext, useEffect, useState } from 'react';
import api from '../utils/api';
import type { ApiLikeError } from '../utils/errorHandler';

const AuthContext = createContext();

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [permissions, setPermissions] = useState([]);
  const [loading, setLoading] = useState(true);

  const checkAuth = useCallback(async () => {
    setLoading(true);
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        setUser(null);
        setPermissions([]);
        return;
      }

      const response = await api.get('/api/auth/user');
      if (response.data && response.data.returnCode === 'SUC0000') {
        setUser(response.data.body.user);
        setPermissions(response.data.body.permissions || []);
      } else {
        if (response.data?.returnCode === 'ERR0007' || response.data?.returnCode === 'ERR0006') {
          localStorage.removeItem('token');
        }
        setUser(null);
        setPermissions([]);
      }
    } catch (error) {
      const apiError = error as ApiLikeError;
      console.error('Auth check failed:', error);
      if (apiError.response?.status === 401) {
        localStorage.removeItem('token');
      }
      setUser(null);
      setPermissions([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    checkAuth();
  }, [checkAuth]);

  const hasPermission = (permissionCode) => {
    if (!user) return false;
    if (user.userId === '1001' || user.userId === 1001) return true;
    const hasAccess = permissions.some(p => {
      const code = p.permissionCode || p.permission_code;
      return code === permissionCode;
    });
    return hasAccess;
  };

  const hasMenuAccess = (_path) => {
    return true;
  };

  const login = async (username, password) => {
    try {
      const response = await api.post('/api/auth/login', {
        username,
        password
      });
      if (response.data && response.data.returnCode === 'SUC0000') {
        const { token, user: userData, permissions: userPermissions } = response.data.body;
        localStorage.setItem('token', token);
        setUser(userData);
        setPermissions(userPermissions || []);
        return { success: true };
      } else {
        return { success: false, message: response.data ? response.data.errorMsg : '鐧诲綍澶辫触' };
      }
    } catch (error) {
      const apiError = error as ApiLikeError;
      console.error('Login error:', error);
      return { success: false, message: apiError.message || '登录失败' };
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    setUser(null);
    setPermissions([]);
  };

  const value = {
    user,
    permissions,
    loading,
    hasPermission,
    hasMenuAccess,
    login,
    logout,
    checkAuth
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};
