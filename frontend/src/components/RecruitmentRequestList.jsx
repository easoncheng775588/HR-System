import React, { useState, useEffect } from 'react';
import { Table, Button, Card, Tag, Space, message, Drawer, Descriptions } from 'antd';
import { PlusOutlined, CloseOutlined } from '@ant-design/icons';
import api from '../utils/api';
import { useNavigate } from 'react-router-dom';

const { Column } = Table;

const RecruitmentRequestList = () => {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [selectedRecord, setSelectedRecord] = useState(null);
  const [isMobile, setIsMobile] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    fetchRequests();
    handleResize();
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  const handleResize = () => {
    setIsMobile(window.innerWidth < 768);
  };

  const fetchRequests = async () => {
    setLoading(true);
    try {
      const response = await api.get('/api/recruitment-request/list');
      if (response.data.returnCode === 'SUC0000') {
        setData(response.data.body || []);
      } else {
        message.error('获取数据失败：' + response.data.errorMsg);
      }
    } catch (error) {
      message.error('获取数据失败，请稍后重试');
      console.error('Fetch error:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleAddNew = () => {
    navigate('/recruitment-request/new');
  };

  const handleView = (record) => {
    setSelectedRecord(record);
    setDrawerVisible(true);
  };

  const handleEdit = (record) => {
    navigate(`/recruitment-request/edit/${record.recruitmentRequestId}`);
  };

  const handleDrawerClose = () => {
    setDrawerVisible(false);
    setSelectedRecord(null);
  };

  const getStatusTag = (status) => {
    if (status === 'DRAFT') {
      return <Tag color="default">草稿</Tag>;
    } else if (status === 'SUBMITTED') {
      return <Tag color="processing">已提交</Tag>;
    }
    return <Tag>{status}</Tag>;
  };

  const getApprovalStatusTag = (approvalStatus) => {
    if (approvalStatus === 'PENDING') {
      return <Tag color="processing">待审批</Tag>;
    } else if (approvalStatus === 'APPROVED') {
      return <Tag color="success">已通过</Tag>;
    } else if (approvalStatus === 'REJECTED') {
      return <Tag color="error">已拒绝</Tag>;
    }
    return <Tag>{approvalStatus}</Tag>;
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
      title: '技术平台',
      dataIndex: 'technicalPlatform',
      key: 'technicalPlatform',
      width: isMobile ? 80 : 100,
      render: getPlatformText,
      responsive: ['lg', 'xl', 'xxl']
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      width: isMobile ? 80 : 100,
      render: getTypeText,
      responsive: ['lg', 'xl', 'xxl']
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
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: isMobile ? 80 : 100,
      render: getStatusTag
    },
    {
      title: '审批状态',
      dataIndex: 'approvalStatus',
      key: 'approvalStatus',
      width: isMobile ? 80 : 100,
      render: getApprovalStatusTag
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
      width: isMobile ? 60 : 150,
      render: (text, record) => (
        <Space size="middle">
          <Button 
            type="link" 
            onClick={() => handleView(record)}
            size={isMobile ? 'small' : 'middle'}
          >
            查看
          </Button>
          {record.approvalStatus === 'PENDING' && record.status === 'SUBMITTED' && (
            <Button 
              type="link" 
              onClick={() => handleEdit(record)}
              size={isMobile ? 'small' : 'middle'}
            >
              编辑
            </Button>
          )}
        </Space>
      )
    }
  ];

  return (
    <div className="page-container">
      <div className="page-title">
        用人申请查询
      </div>
      
      <div style={{ marginBottom: isMobile ? '16px' : '20px', textAlign: 'right' }}>
        <Button 
          type="primary" 
          icon={<PlusOutlined />} 
          onClick={handleAddNew}
          size={isMobile ? 'small' : 'middle'}
        >
          新增用人申请
        </Button>
      </div>
      
      <div className="form-card">
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
      </div>
      
      <Drawer
        title="申请详情"
        placement="right"
        onClose={handleDrawerClose}
        open={drawerVisible}
        width={isMobile ? '100%' : 600}
        closeIcon={<CloseOutlined />}
      >
        {selectedRecord && (
          <Descriptions column={1} bordered={false} size={isMobile ? 'small' : 'default'}>
            <Descriptions.Item label="申请标题">{selectedRecord.requestTitle}</Descriptions.Item>
            <Descriptions.Item label="岗位/班组">{selectedRecord.positionOrTeam}</Descriptions.Item>
            <Descriptions.Item label="所属分类">{getCategoryText(selectedRecord.category)}</Descriptions.Item>
            <Descriptions.Item label="技术平台">{getPlatformText(selectedRecord.technicalPlatform)}</Descriptions.Item>
            <Descriptions.Item label="类型">{getTypeText(selectedRecord.type)}</Descriptions.Item>
            <Descriptions.Item label="补充人数">{selectedRecord.supplementCount}</Descriptions.Item>
            <Descriptions.Item label="建议级别">{getLevelText(selectedRecord.proposedLevel)}</Descriptions.Item>
            <Descriptions.Item label="状态">{getStatusTag(selectedRecord.status)}</Descriptions.Item>
            <Descriptions.Item label="创建时间">
              {selectedRecord.createTime ? new Date(selectedRecord.createTime).toLocaleString('zh-CN') : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="岗位职责" span={3}>
              {selectedRecord.positionResponsibility}
            </Descriptions.Item>
          </Descriptions>
        )}
      </Drawer>
      
      <div style={{ textAlign: 'center', marginTop: isMobile ? '15px' : '30px', color: '#999', fontSize: isMobile ? '10px' : '12px' }}>
        © 2024人力资源管理系统 - 用人申请查询模块
      </div>
    </div>
  );
};

export default RecruitmentRequestList;