import React, { useState, useEffect } from 'react';
import { Table, Button, Card, Modal, Form, Input, message, Space, Tag, Drawer, Descriptions } from 'antd';
import { CheckCircleOutlined, CloseOutlined, EyeOutlined } from '@ant-design/icons';
import api from '../utils/api';

const ApprovalManagement = () => {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [approvalModalVisible, setApprovalModalVisible] = useState(false);
  const [viewDrawerVisible, setViewDrawerVisible] = useState(false);
  const [selectedRecord, setSelectedRecord] = useState(null);
  const [approvalType, setApprovalType] = useState('');
  const [form] = Form.useForm();
  const [isMobile, setIsMobile] = useState(false);

  useEffect(() => {
    fetchPendingApprovals();
    handleResize();
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  const handleResize = () => {
    setIsMobile(window.innerWidth < 768);
  };

  const fetchPendingApprovals = async () => {
    setLoading(true);
    try {
      const response = await api.get('/api/recruitment-request/approval/pending');
      if (response.data.returnCode === 'SUC0000') {
        setData(response.data.body || []);
      } else {
        message.error('获取待审批列表失败：' + response.data.errorMsg);
      }
    } catch (error) {
      message.error('获取待审批列表失败，请稍后重试');
      console.error('Fetch error:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = (record) => {
    setSelectedRecord(record);
    setApprovalType('approve');
    setApprovalModalVisible(true);
    form.resetFields();
  };

  const handleReject = (record) => {
    setSelectedRecord(record);
    setApprovalType('reject');
    setApprovalModalVisible(true);
    form.resetFields();
  };

  const handleView = (record) => {
    setSelectedRecord(record);
    setViewDrawerVisible(true);
  };

  const handleApprovalModalOk = async () => {
    try {
      const values = await form.validateFields();
      const url = `/api/recruitment-request/${selectedRecord.recruitmentRequestId}/${approvalType}`;
      
      const params = {
        approvalUserId: '1001',
        approvalUserName: '系统用户',
        approvalComment: values.approvalComment
      };

      const response = await api.post(url, params);
      
      if (response.data.returnCode === 'SUC0000') {
        message.success(approvalType === 'approve' ? '审批同意' : '审批拒绝');
        setApprovalModalVisible(false);
        form.resetFields();
        fetchPendingApprovals();
        // 触发待办事项更新
        localStorage.setItem('approvalCompleted', 'true');
      } else {
        message.error('操作失败：' + response.data.errorMsg);
      }
    } catch (error) {
      message.error('操作失败，请稍后重试');
      console.error('Approval error:', error);
    }
  };

  const handleApprovalModalCancel = () => {
    setApprovalModalVisible(false);
    form.resetFields();
  };

  const handleViewDrawerClose = () => {
    setViewDrawerVisible(false);
    setSelectedRecord(null);
  };

  const getCategoryText = (category) => {
    const categoryMap = {
      'technical': '技术',
      'business': '业务',
      'operation': '运营',
      'other': '其他'
    };
    return categoryMap[category] || category;
  };

  const getPlatformText = (platform) => {
    const platformMap = {
      'frontend': '前端',
      'backend': '后端',
      'mobile': '移动端',
      'ai': '人工智能'
    };
    return platformMap[platform] || platform;
  };

  const getTypeText = (type) => {
    const typeMap = {
      'fulltime': '全职',
      'parttime': '兼职',
      'intern': '实习生'
    };
    return typeMap[type] || type;
  };

  const getLevelText = (level) => {
    const levelMap = {
      'junior': '初级',
      'senior': '中级',
      'advanced': '高级',
      'expert': '专家'
    };
    return levelMap[level] || level;
  };

  const columns = [
    {
      title: '申请标题',
      dataIndex: 'requestTitle',
      key: 'requestTitle',
      width: isMobile ? 120 : 200,
      ellipsis: true
    },
    {
      title: '岗位/班组',
      dataIndex: 'positionOrTeam',
      key: 'positionOrTeam',
      width: isMobile ? 100 : 150,
      ellipsis: true,
      responsive: ['md', 'lg', 'xl', 'xxl']
    },
    {
      title: '所属分类',
      dataIndex: 'category',
      key: 'category',
      width: isMobile ? 80 : 100,
      render: getCategoryText,
      responsive: ['md', 'lg', 'xl', 'xxl']
    },
    {
      title: '补充人数',
      dataIndex: 'supplementCount',
      key: 'supplementCount',
      width: isMobile ? 80 : 100,
      align: 'center',
      responsive: ['md', 'lg', 'xl', 'xxl']
    },
    {
      title: '建议级别',
      dataIndex: 'proposedLevel',
      key: 'proposedLevel',
      width: isMobile ? 80 : 100,
      render: getLevelText,
      responsive: ['lg', 'xl', 'xxl']
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: isMobile ? 120 : 180,
      render: (text) => text ? new Date(text).toLocaleString('zh-CN') : '-',
      responsive: ['md', 'lg', 'xl', 'xxl']
    },
    {
      title: '操作',
      key: 'action',
      width: isMobile ? 180 : 240,
      render: (text, record) => (
        <Space size="small">
          <Button 
            type="link" 
            icon={<EyeOutlined />}
            onClick={() => handleView(record)}
            size={isMobile ? 'small' : 'middle'}
          >
            查看
          </Button>
          <Button 
            type="link" 
            icon={<CheckCircleOutlined />}
            onClick={() => handleApprove(record)}
            style={{ color: '#52c41a' }}
            size={isMobile ? 'small' : 'middle'}
          >
            同意
          </Button>
          <Button 
            type="link" 
            icon={<CloseOutlined />}
            onClick={() => handleReject(record)}
            danger
            size={isMobile ? 'small' : 'middle'}
          >
            拒绝
          </Button>
        </Space>
      )
    }
  ];

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto', padding: isMobile ? '10px' : '20px', overflow: 'hidden' }}>
      <Card title="待审批列表" extra={<Tag color="blue">共 {data.length} 条待审批</Tag>}>
        <div style={{ overflow: 'auto' }}>
          <Table
            dataSource={data}
            loading={loading}
            rowKey="recruitmentRequestId"
            columns={columns}
            scroll={{ x: isMobile ? 800 : 1200 }}
            pagination={{
              pageSize: isMobile ? 5 : 10,
              showSizeChanger: !isMobile,
              showTotal: (total) => `共 ${total} 条记录`,
              simple: isMobile
            }}
            size={isMobile ? 'small' : 'middle'}
          />
        </div>
      </Card>

      <Modal
        title={approvalType === 'approve' ? '审批同意' : '审批拒绝'}
        open={approvalModalVisible}
        onOk={handleApprovalModalOk}
        onCancel={handleApprovalModalCancel}
        okText="确定"
        cancelText="取消"
        okButtonProps={{ 
          style: approvalType === 'approve' 
            ? { background: '#52c41a', borderColor: '#52c41a' } 
            : { background: '#ff4d4f', borderColor: '#ff4d4f' }
        }}
      >
        <Form
          form={form}
          layout="vertical"
        >
          <Form.Item
            name="approvalComment"
            label="审批意见"
            rules={[{ required: true, message: '请输入审批意见' }]}
          >
            <Input.TextArea 
              rows={4} 
              placeholder="请输入审批意见"
              size={isMobile ? 'small' : 'middle'}
            />
          </Form.Item>
        </Form>
      </Modal>

      <Drawer
        title="申请详情"
        placement="right"
        onClose={handleViewDrawerClose}
        open={viewDrawerVisible}
        width={isMobile ? '100%' : 600}
        closeIcon={<CloseOutlined />}
      >
        {selectedRecord && (
          <Descriptions column={1} bordered size={isMobile ? 'small' : 'default'}>
            <Descriptions.Item label="申请标题">{selectedRecord.requestTitle}</Descriptions.Item>
            <Descriptions.Item label="岗位/班组">{selectedRecord.positionOrTeam}</Descriptions.Item>
            <Descriptions.Item label="所属分类">{getCategoryText(selectedRecord.category)}</Descriptions.Item>
            <Descriptions.Item label="技术平台">{getPlatformText(selectedRecord.technicalPlatform)}</Descriptions.Item>
            <Descriptions.Item label="类型">{getTypeText(selectedRecord.type)}</Descriptions.Item>
            <Descriptions.Item label="补充人数">{selectedRecord.supplementCount}</Descriptions.Item>
            <Descriptions.Item label="建议级别">{getLevelText(selectedRecord.proposedLevel)}</Descriptions.Item>
            <Descriptions.Item label="创建时间">
              {selectedRecord.createTime ? new Date(selectedRecord.createTime).toLocaleString('zh-CN') : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="岗位职责" span={3}>
              {selectedRecord.positionResponsibility}
            </Descriptions.Item>
          </Descriptions>
        )}
      </Drawer>
    </div>
  );
};

export default ApprovalManagement;