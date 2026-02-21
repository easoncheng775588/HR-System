import React from 'react';
import { Card, Row, Col, Space, Typography } from 'antd';
import { UserOutlined, CheckCircleOutlined, BellOutlined } from '@ant-design/icons';

const { Title, Paragraph } = Typography;

const Dashboard = () => {
  return (
    <div className="page-container">
      <div className="page-title">
        欢迎使用招商永隆人事系统
      </div>
      <p style={{ textAlign: 'left', marginBottom: '30px', color: '#666', fontSize: 14 }}>
        您已成功登录系统，这是您的个人仪表盘
      </p>
      
      <div className="form-card">
        <Row gutter={[24, 24]} style={{ marginBottom: '24px' }}>
          <Col xs={24} md={8}>
            <Card bordered={true} style={{ borderTop: 'none', borderLeft: 'none', borderRight: 'none' }}>
              <div className="form-card-title">用户信息</div>
              <p>用户名：admin</p>
              <p>角色：系统管理员</p>
              <p>上次登录时间：2024-01-01 10:00:00</p>
            </Card>
          </Col>
          <Col xs={24} md={8}>
            <Card bordered={true} style={{ borderTop: 'none', borderLeft: 'none', borderRight: 'none' }}>
              <div className="form-card-title">待办事项</div>
              <p>待审核用人申请：0</p>
              <p>待面试：0</p>
              <p>待处理消息：0</p>
            </Card>
          </Col>
          <Col xs={24} md={8}>
            <Card bordered={true} style={{ borderTop: 'none', borderLeft: 'none', borderRight: 'none' }}>
              <div className="form-card-title">通知消息</div>
              <p>系统通知：系统维护公告</p>
              <p>人事通知：新员工入职</p>
              <p>招聘通知：面试安排</p>
            </Card>
          </Col>
        </Row>
        
        <div style={{ textAlign: 'left', marginTop: '32px' }}>
          <div className="form-card-title">系统功能</div>
          <Card bordered={true} style={{ borderTop: 'none', borderLeft: 'none', borderRight: 'none' }}>
            <p style={{ color: '#666' }}>请从左侧菜单选择您需要的功能</p>
          </Card>
        </div>
      </div>
      
      <div style={{ textAlign: 'center', marginTop: '30px', color: '#999', fontSize: '12px' }}>
        © 2024招商永隆人事系统
      </div>
    </div>
  );
};

export default Dashboard;