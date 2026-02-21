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
      background: '#ffffff'
    }}>
      <div style={{ width: '100%', maxWidth: '1200px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <div style={{ 
          width: '400px',
          background: '#fff',
          borderRadius: '8px',
          boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
          padding: '40px',

        }}>
          <div style={{ textAlign: 'center', marginBottom: '30px' }}>
            <h1 style={{
              fontSize: '24px',
              fontWeight: 'bold',
              color: '#1f2937',
              margin: '0 0 10px 0'
            }}>
              招商永隆人事系统
            </h1>
            <p style={{
              color: '#666',
              fontSize: '14px',
              margin: '0'
            }}>
              欢迎登录人事管理系统
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
            >
              <Input 
                prefix={<UserOutlined />} 
                placeholder="用户名" 
                size="large"
              />
            </Form.Item>

            <Form.Item
              name="password"
              rules={[{ required: true, message: '请输入密码' }]}
            >
              <Input.Password 
                prefix={<LockOutlined />} 
                placeholder="密码" 
                size="large"
              />
            </Form.Item>

            <Form.Item>
              <Button 
                type="primary" 
                htmlType="submit" 
                loading={loading}
                block
                size="large"
                style={{ background: '#6366f1', borderColor: '#6366f1' }}
              >
                登录
              </Button>
            </Form.Item>
          </Form>
          
          <div style={{ 
            textAlign: 'center', 
            color: '#999', 
            fontSize: '12px',
            marginTop: '20px',
            borderTop: '1px solid #e8e8e8',
            paddingTop: '15px'
          }}>
            <p style={{ margin: '0' }}>© 2024 招商永隆银行 版权所有</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;