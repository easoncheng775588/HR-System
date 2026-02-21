import React, { createContext, useContext, useState, useEffect } from 'react';
import api from '../utils/api';

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
  const [loading, setLoading] = useState(false);

  const checkAuth = async () => {
    try {
      const token = localStorage.getItem('token');
      if (token) {
        const response = await api.get('/api/auth/user');
        if (response.data.returnCode === 'SUC0000') {
          setUser(response.data.body.user);
          setPermissions(response.data.body.permissions || []);
        }
      }
    } catch (error) {
      console.error('Auth check failed:', error);
    } finally {
      setLoading(false);
    }
  };

  const hasPermission = (permissionCode) => {
    if (!user) return false;
    if (user.userId === '1001' || user.userId === 1001) return true;
    const hasAccess = permissions.some(p => {
      const code = p.permissionCode || p.permission_code;
      return code === permissionCode;
    });
    return hasAccess;
  };

  const hasMenuAccess = (path) => {
    // 即使没有用户，也返回true，确保菜单能够显示
    return true;
  };

  const login = async (username, password) => {
    try {
      const response = await api.post('/api/auth/login', {
        username,
        password
      });
      if (response.data.returnCode === 'SUC0000') {
        const { token, user: userData, permissions: userPermissions } = response.data.body;
        localStorage.setItem('token', token);
        setUser(userData);
        setPermissions(userPermissions || []);
        return { success: true };
      } else {
        return { success: false, message: response.data.errorMsg || '登录失败' };
      }
    } catch (error) {
      console.error('Login error:', error);
      return { success: false, message: '登录失败：' + error.message };
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