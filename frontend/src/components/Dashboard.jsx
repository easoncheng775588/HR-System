import React from 'react';
import { Card, Row, Col, Space, Typography } from 'antd';
import { UserOutlined, CheckCircleOutlined, BellOutlined } from '@ant-design/icons';
import { useAuth } from '../contexts/AuthContext';

const { Title, Paragraph } = Typography;

const Dashboard = () => {
  const { user } = useAuth();
  
  // 获取用户信息，默认值为admin以防未登录
  const username = user?.username || 'admin';
  const role = user?.position || '系统管理员';
  const lastLogin = user?.lastLoginTime || '2024-01-01 10:00:00';
  
  return (
    <div className="interview-scheduling">
      <h2>欢迎使用外包招聘管理系统</h2>
      <p style={{ textAlign: 'left', marginBottom: '12px', color: '#666', fontSize: 12 }}>
        您已成功登录系统，这是您的个人仪表盘
      </p>
      
      <div className="form-card">
        <Row gutter={[12, 12]} style={{ marginBottom: '12px' }}>
          <Col xs={24} md={8}>
            <Card>
              <div className="form-card-title">用户信息</div>
              <p>用户名：{username}</p>
              <p>角色：{role}</p>
              <p>上次登录时间：{lastLogin}</p>
            </Card>
          </Col>
          <Col xs={24} md={8}>
            <Card>
              <div className="form-card-title">待办事项</div>
              <p>待审核用人申请：0</p>
              <p>待面试：0</p>
              <p>待处理消息：0</p>
            </Card>
          </Col>
          <Col xs={24} md={8}>
            <Card>
              <div className="form-card-title">通知消息</div>
              <p>系统通知：系统维护公告</p>
              <p>人事通知：新员工入职</p>
              <p>招聘通知：面试安排</p>
            </Card>
          </Col>
        </Row>
        
        <div style={{ textAlign: 'left', marginTop: '12px' }}>
          <div className="form-card-title">系统功能</div>
          <Card>
            <p style={{ color: '#666' }}>请从左侧菜单选择您需要的功能</p>
          </Card>
        </div>
      </div>
      
      <div style={{ textAlign: 'center', marginTop: '8px', color: '#999', fontSize: '10px' }}>
        © 2026外包招聘管理系统
      </div>
    </div>
  );
};

export default Dashboard;