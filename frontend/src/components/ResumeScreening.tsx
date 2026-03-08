import React, { useEffect, useState } from 'react';
import {
  Button,
  Card,
  Descriptions,
  Drawer,
  Space,
  Table,
  Tag,
  message,
} from 'antd';
import {
  CheckCircleOutlined,
  CloseCircleOutlined,
  DownloadOutlined,
  EyeOutlined,
} from '@ant-design/icons';
import api from '../utils/api';
import { handleApiError } from '../utils/errorHandler';

const ResumeScreening = () => {
  const [loading, setLoading] = useState(false);
  const [resumes, setResumes] = useState([]);
  const [selectedResume, setSelectedResume] = useState(null);
  const [viewDrawerVisible, setViewDrawerVisible] = useState(false);
  const [isMobile, setIsMobile] = useState(false);

  useEffect(() => {
    const onResize = () => setIsMobile(window.innerWidth < 768);
    onResize();
    window.addEventListener('resize', onResize);
    fetchResumes();
    return () => window.removeEventListener('resize', onResize);
  }, []);

  const fetchResumes = async () => {
    setLoading(true);
    try {
      const response = await api.get('/api/resume/list');
      if (response.data?.returnCode === 'SUC0000') {
        setResumes(response.data.body || []);
      } else {
        message.error(`获取简历列表失败：${response.data?.errorMsg || '未知错误'}`);
      }
    } catch (error) {
      handleApiError(error, '获取简历列表失败，请稍后重试');
    } finally {
      setLoading(false);
    }
  };

  const handleDownload = async (record) => {
    const resumeUrl = record.resumeFileUrl;
    if (!resumeUrl) {
      message.error('简历文件不存在');
      return;
    }

    try {
      let fullUrl;
      if (resumeUrl.startsWith('http')) fullUrl = resumeUrl;
      else if (resumeUrl.startsWith('/uploads/')) fullUrl = `http://localhost:8080/api${resumeUrl}`;
      else fullUrl = `http://localhost:8080/api/uploads/${resumeUrl}`;

      const response = await fetch(fullUrl);
      if (!response.ok) throw new Error('Download failed');

      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = record.resumeFileName || 'resume';
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
      message.success('开始下载简历');
    } catch (error) {
      console.error('Download resume failed:', error);
      message.error('下载失败，请稍后重试');
    }
  };

  const handleUpdateStatus = async (record, newStatus) => {
    try {
      const response = await api.put(`/api/resume/${record.resumeId}/status`, { status: newStatus });
      if (response.data?.returnCode === 'SUC0000') {
        message.success('状态更新成功');
        fetchResumes();
      } else {
        message.error(`状态更新失败：${response.data?.errorMsg || '未知错误'}`);
      }
    } catch (error) {
      handleApiError(error, '状态更新失败，请稍后重试');
    }
  };

  const getStatusTag = (status) => {
    if (status === 'PENDING_SCREENING') return <Tag color="processing">待筛选</Tag>;
    if (status === 'SCREENED') return <Tag color="success">已筛选</Tag>;
    if (status === 'INTERVIEW') return <Tag color="orange">面试中</Tag>;
    if (status === 'HIRED') return <Tag color="green">已录用</Tag>;
    if (status === 'REJECTED') return <Tag color="error">已拒绝</Tag>;
    return <Tag>{status || '未知'}</Tag>;
  };

  const columns = [
    { title: '岗位标题', dataIndex: 'jobTitle', key: 'jobTitle', width: 180, ellipsis: true },
    { title: '申请人', dataIndex: 'applicantName', key: 'applicantName', width: 120 },
    { title: '联系电话', dataIndex: 'contactPhone', key: 'contactPhone', width: 140, responsive: ['lg', 'xl', 'xxl'] },
    { title: '邮箱', dataIndex: 'email', key: 'email', width: 180, responsive: ['lg', 'xl', 'xxl'] },
    {
      title: '简历文件',
      dataIndex: 'resumeFileName',
      key: 'resumeFileName',
      width: 180,
      render: (text, record) => (
        <Button type="link" icon={<DownloadOutlined />} onClick={() => handleDownload(record)}>
          {text || '下载'}
        </Button>
      ),
    },
    { title: '状态', dataIndex: 'status', key: 'status', width: 120, render: getStatusTag },
    {
      title: '操作',
      key: 'action',
      width: 260,
      fixed: 'right',
      render: (_, record) => (
        <Space size="small">
          <Button type="link" icon={<EyeOutlined />} onClick={() => { setSelectedResume(record); setViewDrawerVisible(true); }}>
            查看
          </Button>
          <Button type="link" icon={<DownloadOutlined />} onClick={() => handleDownload(record)}>
            下载
          </Button>
          {record.status === 'PENDING_SCREENING' && (
            <>
              <Button type="link" icon={<CheckCircleOutlined />} onClick={() => handleUpdateStatus(record, 'SCREENED')}>
                通过
              </Button>
              <Button type="link" danger icon={<CloseCircleOutlined />} onClick={() => handleUpdateStatus(record, 'REJECTED')}>
                拒绝
              </Button>
            </>
          )}
        </Space>
      ),
    },
  ];
  const hasData = resumes.length > 0;
  const tableColumns = hasData ? columns : columns.map(({ width, fixed, ...rest }) => rest);

  return (
    <div className="app-page">
      <Card
        title="简历筛选"
        extra={<Button onClick={fetchResumes} loading={loading}>刷新</Button>}
      >
        <div className="app-table-wrap">
          <Table
            className={hasData ? '' : 'table-empty-state'}
            rowKey="resumeId"
            loading={loading}
            dataSource={resumes}
            columns={tableColumns}
            scroll={hasData ? { x: isMobile ? 900 : 1300 } : undefined}
            pagination={{
              defaultPageSize: 10,
              showSizeChanger: true,
              pageSizeOptions: [10, 20, 30, 50, 100],
              hideOnSinglePage: false,
              showTotal: (total) => `共 ${total} 条记录`,
            }}
          />
        </div>
      </Card>

      <Drawer
        title="简历详情"
        placement="right"
        open={viewDrawerVisible}
        onClose={() => {
          setViewDrawerVisible(false);
          setSelectedResume(null);
        }}
        width={isMobile ? '100%' : 640}
      >
        {selectedResume && (
          <Descriptions column={1}>
            <Descriptions.Item label="岗位标题">{selectedResume.jobTitle}</Descriptions.Item>
            <Descriptions.Item label="申请人">{selectedResume.applicantName}</Descriptions.Item>
            <Descriptions.Item label="联系电话">{selectedResume.contactPhone}</Descriptions.Item>
            <Descriptions.Item label="邮箱">{selectedResume.email}</Descriptions.Item>
            <Descriptions.Item label="学历">{selectedResume.education || '-'}</Descriptions.Item>
            <Descriptions.Item label="工作经验">{selectedResume.workExperience || '-'}</Descriptions.Item>
            <Descriptions.Item label="状态">{getStatusTag(selectedResume.status)}</Descriptions.Item>
            <Descriptions.Item label="简历文件">{selectedResume.resumeFileName || '-'}</Descriptions.Item>
          </Descriptions>
        )}
      </Drawer>
    </div>
  );
};

export default ResumeScreening;
