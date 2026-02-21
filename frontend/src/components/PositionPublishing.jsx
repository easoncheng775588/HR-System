import React, { useState, useEffect } from 'react';
import { Table, Button, Card, Modal, Form, Input, message, Space, Tag, Drawer, Descriptions } from 'antd';
import { EyeOutlined, CheckCircleOutlined, CloseOutlined, EditOutlined } from '@ant-design/icons';
import api from '../utils/api';

const PositionPublishing = () => {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [viewDrawerVisible, setViewDrawerVisible] = useState(false);
  const [publishModalVisible, setPublishModalVisible] = useState(false);
  const [selectedRecord, setSelectedRecord] = useState(null);
  const [positionForm] = Form.useForm();
  const [isMobile, setIsMobile] = useState(false);

  useEffect(() => {
    fetchApprovedRequests();
    handleResize();
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  const handleResize = () => {
    setIsMobile(window.innerWidth < 768);
  };

  const fetchApprovedRequests = async () => {
    setLoading(true);
    try {
      // 调用后端API获取已批准的用人申请
      console.log('开始获取已批准的用人申请...');
      console.log('API基础URL:', api.defaults.baseURL);
      console.log('API请求路径:', '/api/recruitment-request/approval/status/APPROVED');
      const response = await api.get('/api/recruitment-request/approval/status/APPROVED');
      console.log('API响应:', response);
      console.log('API响应数据:', response.data);
      if (response.data && response.data.returnCode === 'SUC0000') {
        console.log('已批准的用人申请数据:', response.data.body);
        console.log('已批准的用人申请数量:', response.data.body ? response.data.body.length : 0);
        // 为每个已批准的申请添加发布状态字段
        const approvedRequests = (response.data.body || []).map(item => ({
          ...item,
          positionPublishStatus: item.positionPublishStatus || 'UNPUBLISHED'
        }));
        console.log('处理后的申请数据:', approvedRequests);
        console.log('处理后的申请数量:', approvedRequests.length);
        setData(approvedRequests);
        console.log('数据设置完成:', approvedRequests.length);
        // 移除成功提示，避免显示绿色提示框
      } else {
        console.error('获取已批准的用人申请失败:', response.data ? response.data.errorMsg : '未知错误');
        message.error('获取已批准的用人申请失败：' + (response.data ? response.data.errorMsg : '未知错误'));
      }
    } catch (error) {
      console.error('Fetch error:', error);
      console.error('Fetch error message:', error.message);
      console.error('Fetch error stack:', error.stack);
      message.error('获取已批准的用人申请失败，请稍后重试');
    } finally {
      setLoading(false);
      console.log('加载状态设置为false');
    }
  };

  const handleView = (record) => {
    setSelectedRecord(record);
    setViewDrawerVisible(true);
  };

  const handlePublish = (record) => {
    setSelectedRecord(record);
    // 初始化表单数据，整合用人申请中的岗位相关描述信息
    positionForm.setFieldsValue({
      positionTitle: record.requestTitle,
      positionName: record.positionOrTeam,
      recruitCount: record.supplementCount,
      positionLevel: getLevelText(record.proposedLevel),
      experienceYears: record.experienceYears,
      jobDescription: record.positionResponsibility,
      technicalRequirements: getTechnicalRequirements(record.technicalPlatform)
    });
    setPublishModalVisible(true);
  };

  const handleCancelPublish = () => {
    setPublishModalVisible(false);
    positionForm.resetFields();
  };

  const handleConfirmPublish = async () => {
    try {
      const values = await positionForm.validateFields();
      // 调用后端API发布岗位
      console.log('发布岗位:', values);
      const response = await api.put(`/api/recruitment-request/${selectedRecord.recruitmentRequestId}/publish-status`, {
        publishStatus: 'PUBLISHED'
      });
      if (response.data && response.data.returnCode === 'SUC0000') {
        // 移除成功提示，避免显示绿色提示框
        setPublishModalVisible(false);
        positionForm.resetFields();
        // 刷新数据
        fetchApprovedRequests();
      } else {
        message.error('发布失败：' + (response.data ? response.data.errorMsg : '未知错误'));
      }
    } catch (error) {
      message.error('发布失败，请稍后重试');
      console.error('Publish error:', error);
    }
  };

  const handleRevoke = async (record) => {
    try {
      // 调用后端API撤销岗位发布
      console.log('撤销岗位发布:', record);
      const response = await api.put(`/api/recruitment-request/${record.recruitmentRequestId}/publish-status`, {
        publishStatus: 'UNPUBLISHED'
      });
      if (response.data && response.data.returnCode === 'SUC0000') {
        // 移除成功提示，避免显示绿色提示框
        // 刷新数据
        fetchApprovedRequests();
      } else {
        message.error('撤销失败：' + (response.data ? response.data.errorMsg : '未知错误'));
      }
    } catch (error) {
      message.error('撤销失败，请稍后重试');
      console.error('Revoke error:', error);
    }
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

  const getTechnicalRequirements = (platform) => {
    const requirementsMap = {
      'frontend': '熟悉HTML、CSS、JavaScript，掌握React或Vue等前端框架，了解前端工程化和性能优化。',
      'backend': '熟悉Java或Python等后端语言，掌握Spring Boot或Django等框架，了解数据库设计和优化。',
      'mobile': '熟悉iOS或Android开发，掌握Swift或Kotlin等语言，了解移动应用性能优化。',
      'ai': '熟悉机器学习和深度学习算法，掌握Python和相关库，了解大数据处理技术。'
    };
    return requirementsMap[platform] || '';
  };

  const getPublishStatusTag = (status) => {
    if (status === 'PUBLISHED') {
      return <Tag color="success">已发布</Tag>;
    } else if (status === 'UNPUBLISHED') {
      return <Tag color="default">未发布</Tag>;
    }
    return <Tag>{status}</Tag>;
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
      title: '发布状态',
      dataIndex: 'positionPublishStatus',
      key: 'positionPublishStatus',
      width: isMobile ? 80 : 100,
      render: getPublishStatusTag
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
          {record.positionPublishStatus === 'UNPUBLISHED' && (
            <Button 
              type="link" 
              icon={<CheckCircleOutlined />}
              onClick={() => handlePublish(record)}
              style={{ color: '#52c41a' }}
              size={isMobile ? 'small' : 'middle'}
            >
              发布
            </Button>
          )}
          {record.positionPublishStatus === 'PUBLISHED' && (
            <Button 
              type="link" 
              icon={<CloseOutlined />}
              onClick={() => handleRevoke(record)}
              danger
              size={isMobile ? 'small' : 'middle'}
            >
              撤销
            </Button>
          )}
        </Space>
      )
    }
  ];

  return (
    <div className="page-container">
      <div className="page-title">
        岗位发布管理
      </div>
      
      <div style={{ marginBottom: isMobile ? '16px' : '20px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: 10 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          <Button 
            type="primary" 
            size={isMobile ? 'small' : 'middle'}
            onClick={fetchApprovedRequests}
            loading={loading}
          >
            刷新数据
          </Button>
          <Tag color="blue">共 {data.length} 条已批准申请</Tag>
        </div>
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
            locale={{
              emptyText: '暂无已批准的用人申请，请先审批用人申请',
              loadingText: '加载中...',
              pagination: {
                items_per_page: '条/页',
                jump_to: '跳至',
                page: '页',
                prev_page: '上一页',
                next_page: '下一页',
                total: '共 {total} 条记录',
                show_total: true
              }
            }}
          />
        </div>
      </div>

      <Drawer
        title="申请详情"
        placement="right"
        onClose={() => setViewDrawerVisible(false)}
        open={viewDrawerVisible}
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
            <Descriptions.Item label="相关经验年限">{selectedRecord.experienceYears}</Descriptions.Item>
            <Descriptions.Item label="创建时间">
              {selectedRecord.createTime ? new Date(selectedRecord.createTime).toLocaleString('zh-CN') : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="岗位职责" span={3}>
              {selectedRecord.positionResponsibility}
            </Descriptions.Item>
            <Descriptions.Item label="发布状态">{getPublishStatusTag(selectedRecord.positionPublishStatus)}</Descriptions.Item>
          </Descriptions>
        )}
      </Drawer>

      <Modal
        title="发布岗位"
        open={publishModalVisible}
        onOk={handleConfirmPublish}
        onCancel={handleCancelPublish}
        okText="发布"
        cancelText="取消"
        okButtonProps={{ style: { background: '#52c41a', borderColor: '#52c41a' } }}
        width={isMobile ? '90%' : 700}
      >
        <Form
          form={positionForm}
          layout="vertical"
        >
          <Form.Item
            name="positionTitle"
            label="岗位标题"
            rules={[{ required: true, message: '请输入岗位标题' }]}
          >
            <Input placeholder="请输入岗位标题" size={isMobile ? 'small' : 'middle'} />
          </Form.Item>

          <Form.Item
            name="positionName"
            label="岗位名称"
            rules={[{ required: true, message: '请输入岗位名称' }]}
          >
            <Input placeholder="请输入岗位名称" size={isMobile ? 'small' : 'middle'} />
          </Form.Item>

          <Form.Item
            name="recruitCount"
            label="招聘人数"
            rules={[{ required: true, message: '请输入招聘人数' }]}
          >
            <Input type="number" placeholder="请输入招聘人数" size={isMobile ? 'small' : 'middle'} />
          </Form.Item>

          <Form.Item
            name="positionLevel"
            label="岗位级别"
            rules={[{ required: true, message: '请输入岗位级别' }]}
          >
            <Input placeholder="请输入岗位级别" size={isMobile ? 'small' : 'middle'} />
          </Form.Item>

          <Form.Item
            name="experienceYears"
            label="工作经验"
            rules={[{ required: true, message: '请输入工作经验要求' }]}
          >
            <Input placeholder="请输入工作经验要求" size={isMobile ? 'small' : 'middle'} />
          </Form.Item>

          <Form.Item
            name="jobDescription"
            label="岗位职责"
            rules={[{ required: true, message: '请输入岗位职责' }]}
          >
            <Input.TextArea 
              rows={4} 
              placeholder="请输入岗位职责" 
              size={isMobile ? 'small' : 'middle'}
            />
          </Form.Item>

          <Form.Item
            name="technicalRequirements"
            label="技术要求"
            rules={[{ required: true, message: '请输入技术要求' }]}
          >
            <Input.TextArea 
              rows={4} 
              placeholder="请输入技术要求" 
              size={isMobile ? 'small' : 'middle'}
            />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default PositionPublishing;