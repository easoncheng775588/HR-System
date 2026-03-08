import React, { useEffect, useState } from 'react';
import { Button, Descriptions, Drawer, Space, Table, Tag, message } from 'antd';
import { EditOutlined, EyeOutlined, PlusOutlined, CloseOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import api from '../utils/api';
import { useParam } from '../contexts/ParamContext';

const RecruitmentRequestList = () => {
  const { getLevelText, getPlatformText } = useParam();
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [selectedRecord, setSelectedRecord] = useState(null);
  const [isMobile, setIsMobile] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    fetchRequests();
    const onResize = () => setIsMobile(window.innerWidth < 768);
    onResize();
    window.addEventListener('resize', onResize);
    return () => window.removeEventListener('resize', onResize);
  }, []);

  const fetchRequests = async () => {
    setLoading(true);
    try {
      const response = await api.get('/api/recruitment-request/list');
      if (response.data?.returnCode === 'SUC0000') {
        setData(response.data.body || []);
      } else {
        message.error(`获取数据失败：${response.data?.errorMsg || '未知错误'}`);
      }
    } catch (error) {
      message.error('获取数据失败，请稍后重试');
      console.error('Fetch recruitment request list failed:', error);
    } finally {
      setLoading(false);
    }
  };

  const getApprovalStatusTag = (approvalStatus) => {
    if (approvalStatus === 'DRAFT') return <Tag>未提交</Tag>;
    if (approvalStatus === '1STAPPROVED') return <Tag color="processing">一级通过</Tag>;
    if (approvalStatus === '2NDAPPROVED') return <Tag color="processing">二级通过</Tag>;
    if (approvalStatus === '3RDAPPROVED') return <Tag color="success">三级通过</Tag>;
    if (approvalStatus === 'APPROVED') return <Tag color="success">已通过</Tag>;
    if (approvalStatus === 'REJECTED') return <Tag color="error">已拒绝</Tag>;
    if (approvalStatus === 'PENDING') return <Tag color="processing">待审批</Tag>;
    return <Tag>{approvalStatus || '-'}</Tag>;
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
      title: '申请部门',
      dataIndex: 'team',
      key: 'team',
      width: 140,
      ellipsis: true,
      responsive: ['md', 'lg', 'xl', 'xxl'],
      render: (text) => text || '-',
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
      title: '补充人数',
      dataIndex: 'supplementCount',
      key: 'supplementCount',
      width: 110,
      align: 'center',
      responsive: ['md', 'lg', 'xl', 'xxl'],
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
      render: getApprovalStatusTag,
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 180,
      responsive: ['md', 'lg', 'xl', 'xxl'],
      render: (text) => (text ? new Date(text).toLocaleString('zh-CN') : '-'),
    },
    {
      title: '操作',
      key: 'action',
      fixed: 'right',
      width: 160,
      render: (_, record) => (
        <Space size="small">
          <Button type="link" icon={<EyeOutlined />} onClick={() => { setSelectedRecord(record); setDrawerVisible(true); }}>
            查看
          </Button>
          {(record.approvalStatus === 'PENDING' || record.approvalStatus === 'DRAFT') && (
            <Button type="link" icon={<EditOutlined />} onClick={() => navigate(`/recruitment-request/edit/${record.recruitmentRequestId}`)}>
              编辑
            </Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div className="app-page">
      <div className="app-page-actions">
        <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/recruitment-request/new')}>
          新增用人申请
        </Button>
      </div>

      <div className="app-table-wrap">
        <Table
          rowKey="recruitmentRequestId"
          loading={loading}
          dataSource={data}
          columns={columns}
          scroll={{ x: isMobile ? 900 : 1300 }}
          pagination={{
            defaultPageSize: 10,
            showSizeChanger: true,
            pageSizeOptions: [10, 20, 30, 50, 100],
            showTotal: (total) => `共 ${total} 条记录`,
            simple: isMobile,
          }}
          size={isMobile ? 'small' : 'middle'}
        />
      </div>

      <Drawer
        title="申请详情"
        placement="right"
        open={drawerVisible}
        onClose={() => {
          setDrawerVisible(false);
          setSelectedRecord(null);
        }}
        width={isMobile ? '100%' : 640}
        closeIcon={<CloseOutlined />}
      >
        {selectedRecord && (
          <Descriptions column={1} size={isMobile ? 'small' : 'default'}>
            <Descriptions.Item label="岗位标题">{selectedRecord.requestTitle}</Descriptions.Item>
            <Descriptions.Item label="申请部门">{selectedRecord.team || '-'}</Descriptions.Item>
            <Descriptions.Item label="技术平台">{getPlatformText(selectedRecord.technicalPlatform)}</Descriptions.Item>
            <Descriptions.Item label="补充人数">{selectedRecord.supplementCount}</Descriptions.Item>
            <Descriptions.Item label="建议级别">{getLevelText(selectedRecord.proposedLevel)}</Descriptions.Item>
            <Descriptions.Item label="创建时间">
              {selectedRecord.createTime ? new Date(selectedRecord.createTime).toLocaleString('zh-CN') : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="岗位职责">{selectedRecord.positionResponsibility || '-'}</Descriptions.Item>
          </Descriptions>
        )}
      </Drawer>

      <div className="app-footer-note">© 2026 外包招聘管理系统</div>
    </div>
  );
};

export default RecruitmentRequestList;
