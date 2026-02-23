import React, { useState } from 'react';
import { Form, Input, Button, Card, message } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

const Login = () => {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { login } = useAuth();

  const onFinish = async (values) => {
    setLoading(true);
    try {
      const result = await login(values.username, values.password);
      
      if (result.success) {
        message.success('登录成功');
        navigate('/dashboard', { replace: true });
      } else {
        message.error(result.message || '登录失败');
      }
    } catch (error) {
      message.error('登录失败：' + error.message);
      console.error('Login error:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      minHeight: '100vh',
      background: '#f8f9fa'
    }}>
      <div style={{ width: '100%', maxWidth: '1200px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <div style={{ 
          width: '400px',
          background: '#fff',
          borderRadius: '12px',
          boxShadow: '0 0 20px rgba(0,0,0,0.08)',
          padding: '48px',
          border: '1px solid #f0f0f0'
        }}>
          <div style={{ textAlign: 'center', marginBottom: '32px' }}>
            <h1 style={{
              fontSize: '24px',
              fontWeight: '500',
              color: '#2d3748',
              margin: '0 0 12px 0',
              lineHeight: '1.4'
            }}>
              外包招聘管理系统
            </h1>
            <p style={{
              color: '#4a5568',
              fontSize: '14px',
              margin: '0'
            }}>
              欢迎登录外包招聘管理系统
            </p>
          </div>
          
          <Form
            name="login"
            onFinish={onFinish}
            autoComplete="off"
          >
            <Form.Item
              name="username"
              rules={[{ required: true, message: '请输入用户名' }]}
              style={{ marginBottom: '20px' }}
            >
              <Input 
                prefix={<UserOutlined style={{ color: '#40a9ff', marginRight: '8px' }} />} 
                placeholder="用户名" 
                size="large"
                style={{ 
                  borderRadius: '8px',
                  height: '40px',
                  border: '1px solid #e5e7eb',
                  display: 'flex',
                  alignItems: 'center'
                }}
              />
            </Form.Item>

            <Form.Item
              name="password"
              rules={[{ required: true, message: '请输入密码' }]}
              style={{ marginBottom: '24px' }}
            >
              <Input.Password 
                prefix={<LockOutlined style={{ color: '#40a9ff', marginRight: '8px' }} />} 
                placeholder="密码" 
                size="large"
                style={{ 
                  borderRadius: '8px',
                  height: '40px',
                  border: '1px solid #e5e7eb',
                  display: 'flex',
                  alignItems: 'center'
                }}
              />
            </Form.Item>

            <Form.Item>
              <Button 
                type="primary" 
                htmlType="submit" 
                loading={loading}
                block
                size="large"
                style={{ 
                  background: '#40a9ff', 
                  borderColor: '#40a9ff',
                  borderRadius: '8px',
                  height: '40px',
                  fontSize: '16px',
                  fontWeight: '400',
                  boxShadow: 'none'
                }}
              >
                登录
              </Button>
            </Form.Item>
          </Form>
          
          <div style={{ 
            textAlign: 'center', 
            color: '#718096', 
            fontSize: '12px',
            marginTop: '24px',
            borderTop: '1px solid #e5e7eb',
            paddingTop: '20px'
          }}>
            <p style={{ margin: '0' }}>© 2024 外包招聘管理系统 版权所有</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;