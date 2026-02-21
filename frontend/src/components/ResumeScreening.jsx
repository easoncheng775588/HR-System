import React, { useState, useEffect } from 'react';
import { Table, Button, Card, Modal, Tag, Space, Descriptions, message, Drawer } from 'antd';
import { EyeOutlined, DownloadOutlined, CheckCircleOutlined, CloseCircleOutlined, UserOutlined, FileTextOutlined } from '@ant-design/icons';
import api from '../utils/api';

const ResumeScreening = () => {
  const [loading, setLoading] = useState(false);
  const [resumes, setResumes] = useState([]);
  const [selectedResume, setSelectedResume] = useState(null);
  const [viewDrawerVisible, setViewDrawerVisible] = useState(false);
  const [isMobile, setIsMobile] = useState(window.innerWidth < 768);

  useEffect(() => {
    const handleResize = () => {
      setIsMobile(window.innerWidth < 768);
    };

    window.addEventListener('resize', handleResize);
    fetchResumes();

    return () => {
      window.removeEventListener('resize', handleResize);
    };
  }, []);

  const fetchResumes = async () => {
    setLoading(true);
    try {
      const response = await api.get('/api/resume/list');
      if (response.data && response.data.returnCode === 'SUC0000') {
        setResumes(response.data.body || []);
      } else {
        message.error('获取简历列表失败：' + (response.data ? response.data.errorMsg : '未知错误'));
      }
    } catch (error) {
      console.error('Fetch resumes error:', error);
      message.error('获取简历列表失败，请稍后重试');
    } finally {
      setLoading(false);
    }
  };

  const handleView = (record) => {
    setSelectedResume(record);
    setViewDrawerVisible(true);
  };

  const handleDownload = async (record) => {
    // 实现简历下载功能
    const resumeUrl = record.resumeFileUrl;
    if (resumeUrl) {
      // 构建完整的下载链接
      let fullUrl;
      if (resumeUrl.startsWith('http')) {
        fullUrl = resumeUrl;
      } else if (resumeUrl.startsWith('/uploads/')) {
        // 直接使用后端的静态资源处理器
        fullUrl = `http://localhost:8080/api${resumeUrl}`;
      } else {
        fullUrl = `http://localhost:8080/api/uploads/${resumeUrl}`;
      }
      console.log('Download URL:', fullUrl);
      
      try {
        // 使用fetch API获取文件
        const response = await fetch(fullUrl);
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        
        // 创建Blob对象
        const blob = await response.blob();
        
        // 创建临时URL
        const url = window.URL.createObjectURL(blob);
        
        // 创建a标签并触发下载
        const a = document.createElement('a');
        a.href = url;
        a.download = record.resumeFileName;
        document.body.appendChild(a);
        a.click();
        
        // 清理
        setTimeout(() => {
          window.URL.revokeObjectURL(url);
          document.body.removeChild(a);
        }, 100);
        
        message.success('开始下载简历');
      } catch (error) {
        console.error('Download error:', error);
        message.error('下载失败，请稍后重试');
      }
    } else {
      message.error('简历文件不存在');
    }
  };

  const handleUpdateStatus = async (record, newStatus) => {
    try {
      const response = await api.put(`/api/resume/${record.resumeId}/status`, {
        status: newStatus
      });
      if (response.data && response.data.returnCode === 'SUC0000') {
        message.success('状态更新成功');
        fetchResumes();
      } else {
        message.error('状态更新失败：' + (response.data ? response.data.errorMsg : '未知错误'));
      }
    } catch (error) {
      console.error('Update status error:', error);
      message.error('状态更新失败，请稍后重试');
    }
  };

  const getStatusTag = (status) => {
    switch (status) {
      case 'PENDING_SCREENING':
        return <Tag color="blue">待筛选</Tag>;
      case 'SCREENED':
        return <Tag color="green">已筛选</Tag>;
      case 'INTERVIEW':
        return <Tag color="orange">面试中</Tag>;
      case 'HIRED':
        return <Tag color="success">已录用</Tag>;
      case 'REJECTED':
        return <Tag color="error">已拒绝</Tag>;
      default:
        return <Tag color="default">未知状态</Tag>;
    }
  };

  const columns = [
    {
      title: '简历ID',
      dataIndex: 'resumeId',
      key: 'resumeId',
      width: 100,
      responsive: ['lg']
    },
    {
      title: '岗位标题',
      dataIndex: 'jobTitle',
      key: 'jobTitle',
      ellipsis: true,
      width: 150
    },
    {
      title: '申请人',
      dataIndex: 'applicantName',
      key: 'applicantName',
      width: 100
    },
    {
      title: '联系电话',
      dataIndex: 'contactPhone',
      key: 'contactPhone',
      width: 120,
      responsive: ['lg']
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      key: 'email',
      width: 150,
      responsive: ['lg']
    },
    {
      title: '学历',
      dataIndex: 'education',
      key: 'education',
      width: 80,
      responsive: ['lg']
    },
    {
      title: '工作经验',
      dataIndex: 'workExperience',
      key: 'workExperience',
      width: 100,
      responsive: ['lg']
    },
    {
      title: '简历文件',
      dataIndex: 'resumeFileName',
      key: 'resumeFileName',
      ellipsis: true,
      width: 150,
      render: (text, record) => (
        <Button
          type="link"
          icon={<FileTextOutlined />}
          onClick={() => handleDownload(record)}
          size="small"
        >
          {text}
        </Button>
      )
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 80,
      render: (status) => getStatusTag(status)
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_, record) => (
        <Space size={isMobile ? 'small' : 'middle'}>
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
            icon={<DownloadOutlined />}
            onClick={() => handleDownload(record)}
            size={isMobile ? 'small' : 'middle'}
          >
            下载
          </Button>
          {record.status === 'PENDING_SCREENING' && (
            <>
              <Button
                type="primary"
                icon={<CheckCircleOutlined />}
                onClick={() => handleUpdateStatus(record, 'SCREENED')}
                size={isMobile ? 'small' : 'middle'}
              >
                通过
              </Button>
              <Button
                danger
                icon={<CloseCircleOutlined />}
                onClick={() => handleUpdateStatus(record, 'REJECTED')}
                size={isMobile ? 'small' : 'middle'}
              >
                拒绝
              </Button>
            </>
          )}
        </Space>
      )
    }
  ];

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto', padding: isMobile ? '10px' : '20px', overflow: 'hidden' }}>
      <Card
        title="简历筛选管理"
        extra={
          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <Button
              type="primary"
              size={isMobile ? 'small' : 'middle'}
              onClick={fetchResumes}
              loading={loading}
            >
              刷新简历
            </Button>
            <Tag color="blue">共 {resumes.length} 份简历</Tag>
          </div>
        }
      >
        <div style={{ overflow: 'auto' }}>
          <Table
            dataSource={resumes}
            loading={loading}
            rowKey="resumeId"
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
              emptyText: '暂无已提交的简历',
              loadingText: '加载中...',
              pagination: {
                items_per_page: '条/页',
                jump_to: '跳至',
                page: '页',
                prev_page: '上一页',
                next_page: '下一页',
                total: '共 {total} 条记录'
              }
            }}
          />
        </div>
      </Card>

      <Drawer
        title="简历详情"
        open={viewDrawerVisible}
        onClose={() => setViewDrawerVisible(false)}
        width={isMobile ? '90%' : 700}
        placement="right"
        destroyOnClose
      >
        {selectedResume && (
          <div>
            <Card
              title="个人信息"
              style={{ marginBottom: 20 }}
              size={isMobile ? 'small' : 'default'}
            >
              <Descriptions column={2} size={isMobile ? 'small' : 'default'}>
                <Descriptions.Item label="姓名">{selectedResume.applicantName}</Descriptions.Item>
                <Descriptions.Item label="联系电话">{selectedResume.contactPhone}</Descriptions.Item>
                <Descriptions.Item label="邮箱">{selectedResume.email}</Descriptions.Item>
                <Descriptions.Item label="学历">{selectedResume.education}</Descriptions.Item>
                <Descriptions.Item label="工作经验">{selectedResume.workExperience}</Descriptions.Item>
                <Descriptions.Item label="状态">{getStatusTag(selectedResume.status)}</Descriptions.Item>
              </Descriptions>
            </Card>

            <Card
              title="岗位信息"
              style={{ marginBottom: 20 }}
              size={isMobile ? 'small' : 'default'}
            >
              <Descriptions column={1} size={isMobile ? 'small' : 'default'}>
                <Descriptions.Item label="岗位标题">{selectedResume.jobTitle}</Descriptions.Item>
                <Descriptions.Item label="招聘申请ID">{selectedResume.recruitmentRequestId}</Descriptions.Item>
              </Descriptions>
            </Card>

            <Card
              title="简历文件"
              size={isMobile ? 'small' : 'default'}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <FileTextOutlined style={{ fontSize: 24, color: '#1890ff' }} />
                <div>
                  <p style={{ margin: 0, fontWeight: 'bold' }}>{selectedResume.resumeFileName}</p>
                  <Button
                    type="primary"
                    icon={<DownloadOutlined />}
                    onClick={() => handleDownload(selectedResume)}
                    size={isMobile ? 'small' : 'middle'}
                    style={{ marginTop: 10 }}
                  >
                    下载简历
                  </Button>
                </div>
              </div>
            </Card>
          </div>
        )}
      </Drawer>
    </div>
  );
};

export default ResumeScreening;
