import React, { useCallback, useEffect, useState } from 'react';
import { Button, Card, Descriptions, Drawer, Space, Table, Tag, message } from 'antd';
import { CheckCircleOutlined, CloseOutlined, EyeOutlined } from '@ant-design/icons';
import api from '../utils/api';
import { useAuth } from '../contexts/AuthContext';
import { useParam } from '../contexts/ParamContext';

const ApprovalManagement = () => {
  const { user } = useAuth();
  const { getLevelText, getPlatformText } = useParam();

  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [isMobile, setIsMobile] = useState(false);

  const [selectedRecord, setSelectedRecord] = useState(null);
  const [viewDrawerVisible, setViewDrawerVisible] = useState(false);

  const userRole = user?.position || user?.positionName || user?.role || '';

  useEffect(() => {
    const onResize = () => setIsMobile(window.innerWidth < 768);
    onResize();
    window.addEventListener('resize', onResize);
    return () => window.removeEventListener('resize', onResize);
  }, []);

  const fetchPendingApprovals = useCallback(async () => {
    setLoading(true);
    try {
      const encodedUserRole = encodeURIComponent(userRole);
      const response = await api.get(`/api/recruitment-request/approval/pending/${encodedUserRole}`);
      if (response.data?.returnCode === 'SUC0000') {
        setData(response.data.body || []);
      } else {
        message.error(`获取待审批列表失败：${response.data?.errorMsg || '未知错误'}`);
      }
    } catch (error) {
      console.error('Fetch pending approvals failed:', error);
      message.error('获取待审批列表失败，请稍后重试');
    } finally {
      setLoading(false);
    }
  }, [userRole]);

  useEffect(() => {
    if (userRole) {
      fetchPendingApprovals();
    }
  }, [userRole, fetchPendingApprovals]);

  const handleApprove = async (record) => {
    try {
      const response = await api.post(
        `/api/recruitment-request/${record.recruitmentRequestId}/three-level/approve`,
        {
          approvalUserId: String(user?.userId || '1001'),
          approvalUserName: user?.realName || '系统用户',
          approvalComment: '同意',
        }
      );

      if (response.data?.returnCode === 'SUC0000') {
        message.success('审批通过');
        localStorage.setItem('approvalCompleted', 'true');
        fetchPendingApprovals();
      } else {
        message.error(`审批失败：${response.data?.errorMsg || '未知错误'}`);
      }
    } catch (error) {
      console.error('Approve failed:', error);
      message.error('审批失败，请稍后重试');
    }
  };

  const handleReject = async (record) => {
    try {
      const response = await api.post(
        `/api/recruitment-request/${record.recruitmentRequestId}/three-level/reject`,
        {
          approvalUserId: String(user?.userId || '1001'),
          approvalUserName: user?.realName || '系统用户',
          approvalComment: '拒绝',
        }
      );

      if (response.data?.returnCode === 'SUC0000') {
        message.success('审批拒绝');
        localStorage.setItem('approvalCompleted', 'true');
        fetchPendingApprovals();
      } else {
        message.error(`审批失败：${response.data?.errorMsg || '未知错误'}`);
      }
    } catch (error) {
      console.error('Reject failed:', error);
      message.error('审批失败，请稍后重试');
    }
  };

  const statusTag = (status) => {
    if (status === 'APPROVED' || status === '3RDAPPROVED') return <Tag color="success">已通过</Tag>;
    if (status === 'REJECTED') return <Tag color="error">已拒绝</Tag>;
    if (status === '1STAPPROVED') return <Tag color="processing">一级通过</Tag>;
    if (status === '2NDAPPROVED') return <Tag color="processing">二级通过</Tag>;
    return <Tag color="processing">待审批</Tag>;
  };

  const columns = [
    {
      title: '岗位标题',
      dataIndex: 'requestTitle',
      key: 'requestTitle',
      width: isMobile ? 140 : 220,
      ellipsis: true,
    },
    {
      title: '所属团队',
      dataIndex: 'team',
      key: 'team',
      width: 140,
      ellipsis: true,
      responsive: ['md', 'lg', 'xl', 'xxl'],
    },
    {
      title: '技术平台',
      dataIndex: 'technicalPlatform',
      key: 'technicalPlatform',
      width: 120,
      responsive: ['lg', 'xl', 'xxl'],
      render: (text) => (text ? getPlatformText(text) : '-'),
    },
    {
      title: '建议级别',
      dataIndex: 'proposedLevel',
      key: 'proposedLevel',
      width: 120,
      responsive: ['lg', 'xl', 'xxl'],
      render: (text) => (text ? getLevelText(text) : '-'),
    },
    {
      title: '审批状态',
      dataIndex: 'approvalStatus',
      key: 'approvalStatus',
      width: 120,
      render: statusTag,
    },
    {
      title: '操作',
      key: 'action',
      width: 220,
      fixed: 'right',
      render: (_, record) => (
        <Space size="small">
          <Button type="link" icon={<EyeOutlined />} onClick={() => { setSelectedRecord(record); setViewDrawerVisible(true); }}>
            查看
          </Button>
          <Button type="link" icon={<CheckCircleOutlined />} onClick={() => handleApprove(record)}>
            同意
          </Button>
          <Button type="link" danger icon={<CloseOutlined />} onClick={() => handleReject(record)}>
            拒绝
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div className="app-page">
      <Card title="待审批列表" extra={<Tag color="blue">当前角色：{userRole || '-'}</Tag>}>
        <div className="app-table-wrap">
          <Table
            rowKey="recruitmentRequestId"
            dataSource={data}
            columns={columns}
            loading={loading}
            scroll={{ x: isMobile ? 900 : 1300 }}
            pagination={{
              defaultPageSize: 10,
              showSizeChanger: true,
              pageSizeOptions: [10, 20, 30, 50, 100],
              showTotal: (total) => `共 ${total} 条记录`,
              simple: isMobile,
            }}
          />
        </div>
      </Card>

      <Drawer
        title="申请详情"
        placement="right"
        open={viewDrawerVisible}
        onClose={() => {
          setViewDrawerVisible(false);
          setSelectedRecord(null);
        }}
        width={isMobile ? '100%' : 640}
      >
        {selectedRecord && (
          <Descriptions column={1}>
            <Descriptions.Item label="岗位标题">{selectedRecord.requestTitle}</Descriptions.Item>
            <Descriptions.Item label="总编制人数">{selectedRecord.totalRecruitmentCount}</Descriptions.Item>
            <Descriptions.Item label="空缺编制">{selectedRecord.vacancyCount}</Descriptions.Item>
            <Descriptions.Item label="所属团队">{selectedRecord.team || '-'}</Descriptions.Item>
            <Descriptions.Item label="技术平台">{getPlatformText(selectedRecord.technicalPlatform)}</Descriptions.Item>
            <Descriptions.Item label="建议级别">{getLevelText(selectedRecord.proposedLevel)}</Descriptions.Item>
            <Descriptions.Item label="经验要求">{selectedRecord.experienceYears || '-'}</Descriptions.Item>
            <Descriptions.Item label="技能要求">{selectedRecord.skillRequirement || '-'}</Descriptions.Item>
            <Descriptions.Item label="岗位职责">{selectedRecord.positionResponsibility || '-'}</Descriptions.Item>
            <Descriptions.Item label="审批状态">{statusTag(selectedRecord.approvalStatus)}</Descriptions.Item>
          </Descriptions>
        )}
      </Drawer>
    </div>
  );
};

export default ApprovalManagement;
