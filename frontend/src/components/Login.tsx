import React, { useState } from 'react';
import { Button, Card, Form, Input, Typography, message } from 'antd';
import { LockOutlined, UserOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { getErrorMessage } from '../utils/errorMessages';

const { Title, Paragraph, Text } = Typography;

const Login = () => {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { login } = useAuth();

  const onFinish = async (values) => {
    setLoading(true);
    try {
      const result = await login(values.username, values.password);
      if (result.success) {
        message.success(getErrorMessage('login.loginSuccess'));
        navigate('/dashboard', { replace: true });
      } else {
        message.error(result.message || getErrorMessage('login.loginFailed'));
      }
    } catch (error) {
      message.error(`${getErrorMessage('login.loginFailed')}：${error.message || ''}`);
      console.error('Login error:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: 16,
        background: '#f5f7fb',
      }}
    >
      <Card style={{ width: '100%', maxWidth: 420 }}>
        <Title level={3} style={{ marginBottom: 8, textAlign: 'center' }}>
          外包招聘管理系统
        </Title>
        <Paragraph style={{ textAlign: 'center', marginBottom: 24, color: 'rgba(0,0,0,0.65)' }}>
          欢迎登录系统
        </Paragraph>

        <Form name="login" onFinish={onFinish} autoComplete="off" layout="vertical">
          <Form.Item
            name="username"
            label="用户名"
            rules={[{ required: true, message: '请输入用户名' }]}
          >
            <Input
              name="username"
              prefix={<UserOutlined />}
              placeholder="请输入用户名"
              autoComplete="username"
            />
          </Form.Item>

          <Form.Item
            name="password"
            label="密码"
            rules={[{ required: true, message: '请输入密码' }]}
          >
            <Input.Password
              name="password"
              prefix={<LockOutlined />}
              placeholder="请输入密码"
              autoComplete="current-password"
            />
          </Form.Item>

          <Form.Item style={{ marginBottom: 8 }}>
            <Button type="primary" htmlType="submit" loading={loading} block>
              登录
            </Button>
          </Form.Item>
        </Form>

        <Text type="secondary" style={{ display: 'block', textAlign: 'center', marginTop: 8 }}>
          © 2026 外包招聘管理系统
        </Text>
      </Card>
    </div>
  );
};

export default Login;
