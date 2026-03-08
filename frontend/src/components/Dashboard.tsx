import React from 'react';
import { Card, Col, Row, Typography, Space, Tag } from 'antd';
import { useAuth } from '../contexts/AuthContext';

const { Title, Paragraph, Text } = Typography;

const Dashboard = () => {
  const { user } = useAuth();
  const username = user?.username || 'admin';
  const role = user?.position || '系统管理员';
  const lastLogin = user?.lastLoginTime || '-';

  return (
    <div className="app-page">
      <Title level={3} className="app-page-title">欢迎使用外包招聘管理系统</Title>
      <Paragraph className="app-page-subtitle">您已成功登录系统，可通过左侧导航进入各业务模块。</Paragraph>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={8}>
          <Card title="用户信息" bordered>
            <Space direction="vertical" size={8}>
              <Text>用户名：{username}</Text>
              <Text>角色：{role}</Text>
              <Text>上次登录：{lastLogin}</Text>
            </Space>
          </Card>
        </Col>

        <Col xs={24} lg={8}>
          <Card title="待办事项" bordered>
            <Space direction="vertical" size={8}>
              <Text>待审核用人申请：0</Text>
              <Text>待安排面试：0</Text>
              <Text>待处理消息：0</Text>
            </Space>
          </Card>
        </Col>

        <Col xs={24} lg={8}>
          <Card title="系统提示" bordered>
            <Space direction="vertical" size={8}>
              <Tag color="blue">系统通知：系统维护公告</Tag>
              <Tag color="green">人事通知：新员工入职</Tag>
              <Tag color="purple">招聘通知：面试安排</Tag>
            </Space>
          </Card>
        </Col>
      </Row>

      <div className="app-footer-note">? 2026 外包招聘管理系统</div>
    </div>
  );
};

export default Dashboard;

