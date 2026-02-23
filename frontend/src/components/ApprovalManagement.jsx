import React, { useState, useEffect } from 'react';
import { Table, Button, Card, Modal, Form, Input, message, Space, Tag, Drawer, Descriptions, Select } from 'antd';
import { CheckCircleOutlined, CloseOutlined, EyeOutlined, EditOutlined } from '@ant-design/icons';
import api from '../utils/api';
import { useAuth } from '../contexts/AuthContext';

// 错误边界组件
class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    console.error('错误边界捕获到错误:', error);
    console.error('错误信息:', errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div style={{ padding: '20px', textAlign: 'center' }}>
          <h2>出错了</h2>
          <p>页面加载失败，请刷新重试</p>
          <p style={{ color: 'red' }}>{this.state.error?.message}</p>
          <Button type="primary" onClick={() => window.location.reload()}>刷新页面</Button>
        </div>
      );
    }

    return this.props.children;
  }
}

const ApprovalManagement = () => {
  const { user } = useAuth();
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [viewDrawerVisible, setViewDrawerVisible] = useState(false);
  const [editDrawerVisible, setEditDrawerVisible] = useState(false);
  const [selectedRecord, setSelectedRecord] = useState(null);
  const [userRole, setUserRole] = useState(''); // 从登录信息中获取用户角色
  const [availableQuota, setAvailableQuota] = useState(0); // 可用编制数量
  const [approvalHistory, setApprovalHistory] = useState([]); // 审批历史
  const [isMobile, setIsMobile] = useState(false);

  useEffect(() => {
    const handleResize = () => {
      setIsMobile(window.innerWidth < 768);
    };

    handleResize();
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  useEffect(() => {
    console.log('=== 用户信息变化 ===');
    console.log('用户对象:', user);
    try {
      if (user) {
        // 从用户信息中获取用户角色
        console.log('用户position:', user.position);
        console.log('用户positionName:', user.positionName);
        console.log('用户role:', user.role);
        const role = user.position || user.positionName || user.role || '';
        console.log('最终用户角色:', role);
        setUserRole(role);
      } else {
        console.log('用户对象为空');
        setUserRole('');
      }
    } catch (error) {
      console.error('获取用户角色失败:', error);
      console.error('错误堆栈:', error.stack);
      setUserRole('');
    }
  }, [user]);

  // 添加一个调试方法，以便在浏览器控制台中查看组件状态
  useEffect(() => {
    // 将组件状态暴露给全局，便于调试
    window.debugApprovalManagement = {
      userRole,
      data,
      loading,
      viewDrawerVisible,
      editDrawerVisible,
      selectedRecord,
      availableQuota,
      // 手动触发编辑操作的方法
      triggerEdit: (record) => handleEdit(record)
    };
    
    console.log('=== 组件状态已更新 ===');
    console.log('用户角色:', userRole);
    console.log('待审批列表长度:', data.length);
    console.log('编辑抽屉可见性:', editDrawerVisible);
    console.log('选中的记录:', selectedRecord);
  }, [userRole, data, loading, viewDrawerVisible, editDrawerVisible, selectedRecord, availableQuota]);

  const fetchAvailableQuota = async () => {
    try {
      const response = await api.get('/api/sys-param/quota');
      if (response.data.returnCode === 'SUC0000' && response.data.body) {
        setAvailableQuota(response.data.body);
      }
    } catch (error) {
      console.error('获取可用编制数量失败:', error);
    }
  };

  const fetchPendingApprovals = async () => {
    setLoading(true);
    try {
      const response = await api.get(`/api/recruitment-request/approval/pending/${userRole}`);
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

  const fetchApprovalHistory = async (recruitmentRequestId) => {
    try {
      const response = await api.get(`/api/recruitment-request/approval-history/${recruitmentRequestId}`);
      if (response.data.returnCode === 'SUC0000') {
        setApprovalHistory(response.data.body || []);
      } else {
        message.error('获取审批历史失败：' + response.data.errorMsg);
        setApprovalHistory([]);
      }
    } catch (error) {
      message.error('获取审批历史失败，请稍后重试');
      console.error('Error fetching approval history:', error);
      setApprovalHistory([]);
    }
  };

  useEffect(() => {
    if (userRole) {
      // 具有审批权限或查看权限的角色才能获取待审批列表
      const hasPermission = [
        '编制管理岗',
        '外包招聘岗',
        '团队经理',
        '分管总'
      ].includes(userRole);
      
      if (hasPermission) {
        fetchPendingApprovals();
        if (userRole === '编制管理岗') {
          fetchAvailableQuota();
        }
      } else {
        // 没有权限的角色，清空待审批数据
        setData([]);
      }
    }
  }, [userRole]);

  const handleApprove = async (record) => {
    try {
      if (!record || !record.recruitmentRequestId) {
        message.error('记录不存在');
        return;
      }
      
      const url = `/api/recruitment-request/${record.recruitmentRequestId}/three-level/approve`;
      
      const params = {
        approvalUserId: '1001',
        approvalUserName: '系统用户',
        approvalComment: '同意'
      };

      const response = await api.post(url, params);
      
      if (response.data.returnCode === 'SUC0000') {
        message.success('审批同意');
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

  const handleReject = async (record) => {
    try {
      if (!record || !record.recruitmentRequestId) {
        message.error('记录不存在');
        return;
      }
      
      const url = `/api/recruitment-request/${record.recruitmentRequestId}/three-level/reject`;
      
      const params = {
        approvalUserId: '1001',
        approvalUserName: '系统用户',
        approvalComment: '拒绝'
      };

      const response = await api.post(url, params);
      
      if (response.data.returnCode === 'SUC0000') {
        message.success('审批拒绝');
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

  const handleView = (record) => {
    setSelectedRecord(record);
    setViewDrawerVisible(true);
    // 获取审批历史
    if (record.recruitmentRequestId) {
      fetchApprovalHistory(record.recruitmentRequestId);
    }
  };

  const handleEdit = (record) => {
    console.log('=== 开始编辑操作 ===');
    console.log('编辑记录:', record);
    console.log('记录类型:', typeof record);
    console.log('记录是否存在:', !!record);
    
    if (!record) {
      console.error('记录不存在');
      message.error('记录不存在');
      return;
    }
    
    console.log('记录属性:', Object.keys(record));
    console.log('requestTitle:', record.requestTitle);
    console.log('positionOrTeam:', record.positionOrTeam);
    console.log('supplementCount:', record.supplementCount);
    console.log('proposedLevel:', record.proposedLevel);
    console.log('positionResponsibility:', record.positionResponsibility);
    console.log('currentApprovalLevel:', record.currentApprovalLevel);
    console.log('approvalStatus:', record.approvalStatus);
    
    try {
      console.log('1. 设置selectedRecord');
      setSelectedRecord(record);
      console.log('2. 设置editDrawerVisible');
      setEditDrawerVisible(true);
      console.log('=== 编辑操作完成 ===');
    } catch (error) {
      console.error('编辑失败:', error);
      console.error('错误堆栈:', error.stack);
      message.error('编辑失败，请稍后重试');
    }
  };

  const handleApprovalModalOk = async (values) => {
    try {
      if (!selectedRecord || !selectedRecord.recruitmentRequestId) {
        message.error('记录不存在');
        return;
      }
      
      const url = `/api/recruitment-request/${selectedRecord.recruitmentRequestId}/three-level/${approvalType}`;
      
      const params = {
        approvalUserId: '1001',
        approvalUserName: '系统用户',
        approvalComment: values.approvalComment
      };

      const response = await api.post(url, params);
      
      if (response.data.returnCode === 'SUC0000') {
        message.success(approvalType === 'approve' ? '审批同意' : '审批拒绝');
        setApprovalModalVisible(false);
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

  const handleEditDrawerOk = async (values) => {
    try {
      if (!selectedRecord || !selectedRecord.recruitmentRequestId) {
        message.error('记录不存在');
        return;
      }
      const updatedRecord = {
        ...selectedRecord,
        ...values
      };

      const response = await api.put(`/api/recruitment-request/${selectedRecord.recruitmentRequestId}`, updatedRecord);
      
      if (response.data.returnCode === 'SUC0000') {
        message.success('编辑成功');
        setEditDrawerVisible(false);
        fetchPendingApprovals();
      } else {
        message.error('编辑失败：' + response.data.errorMsg);
      }
    } catch (error) {
      message.error('编辑失败，请稍后重试');
      console.error('Edit error:', error);
    }
  };

  const handleApprovalModalCancel = () => {
    setApprovalModalVisible(false);
  };

  const handleEditDrawerCancel = () => {
    setEditDrawerVisible(false);
  };

  const handleViewDrawerClose = () => {
    setViewDrawerVisible(false);
    setSelectedRecord(null);
  };

  const handleEditDrawerClose = () => {
    setEditDrawerVisible(false);
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
      title: '岗位标题',
      dataIndex: 'requestTitle',
      key: 'requestTitle',
      width: 200,
      ellipsis: true
    },
    {
      title: '岗位/班组',
      dataIndex: 'positionOrTeam',
      key: 'positionOrTeam',
      width: 150,
      ellipsis: true,
      responsive: ['md', 'lg', 'xl', 'xxl']
    },
    {
      title: '所属分类',
      dataIndex: 'category',
      key: 'category',
      width: 100,
      render: getCategoryText,
      responsive: ['md', 'lg', 'xl', 'xxl']
    },
    {
      title: '补充人数',
      dataIndex: 'supplementCount',
      key: 'supplementCount',
      width: 100,
      align: 'center',
      responsive: ['md', 'lg', 'xl', 'xxl']
    },
    {
      title: '建议级别',
      dataIndex: 'proposedLevel',
      key: 'proposedLevel',
      width: 100,
      render: getLevelText,
      responsive: ['lg', 'xl', 'xxl']
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 180,
      render: (text) => text ? new Date(text).toLocaleString('zh-CN') : '-',
      responsive: ['md', 'lg', 'xl', 'xxl']
    },
    {
        title: '操作',
        key: 'action',
        width: isMobile ? 180 : 240,
        fixed: 'right',
        render: (text, record) => (
        <Space size={isMobile ? 'small' : 'middle'}>
          <Button 
            type="link" 
            icon={<EyeOutlined />}
            onClick={() => handleView(record)}
            size={isMobile ? 'small' : 'middle'}
            key="view"
          >
            查看
          </Button>
          {userRole === '外包招聘岗' && record.currentApprovalLevel === 2 && (
            <Button 
              type="link" 
              icon={<EditOutlined />}
              onClick={() => handleEdit(record)}
              size={isMobile ? 'small' : 'middle'}
              key="edit"
            >
              编辑
            </Button>
          )}
          {((userRole === '编制管理岗' && record.currentApprovalLevel === 1) || 
            (userRole === '外包招聘岗' && record.currentApprovalLevel === 2) || 
            (userRole === '团队经理' && record.currentApprovalLevel === 3)) && 
            userRole !== '分管总' && (
            <Space key="approval" size={isMobile ? 'small' : 'middle'}>
              <Button 
                type="link" 
                icon={<CheckCircleOutlined />}
                onClick={() => handleApprove(record)}
                size={isMobile ? 'small' : 'middle'}
                key="approve"
              >
                同意
              </Button>
              <Button 
                type="link" 
                icon={<CloseOutlined />}
                onClick={() => handleReject(record)}
                danger
                size={isMobile ? 'small' : 'middle'}
                key="reject"
              >
                拒绝
              </Button>
            </Space>
          )}
        </Space>
      )
    }
  ];

  return (
    <div className="interview-scheduling">
      <Card 
        title={userRole === '编制管理岗' ? `待审批列表 (可用编制: ${availableQuota})` : '待审批列表'} 
        extra={
          <Space>
            <Tag color="green">当前角色：{userRole}</Tag>
            {userRole === '编制管理岗' && (
              <Tag color="orange">可用编制：{availableQuota}</Tag>
            )}
            {/* 具有审批权限或查看权限的角色才能看到待审批数量 */}
            {[
              '编制管理岗',
              '外包招聘岗',
              '团队经理',
              '分管总'
            ].includes(userRole) && (
              <Tag color="blue">共 {data.length} 条待审批</Tag>
            )}
          </Space>
        }
      >
        <Table
          dataSource={data}
          loading={loading}
          rowKey="recruitmentRequestId"
          columns={columns}
          pagination={{
            pageSize: isMobile ? 5 : 10,
            showSizeChanger: !isMobile,
            showTotal: (total) => `共 ${total} 条记录`,
            simple: isMobile
          }}
          size={isMobile ? 'small' : 'middle'}
        />
      </Card>



      <Drawer
        title="申请详情"
        placement="right"
        onClose={handleViewDrawerClose}
        open={viewDrawerVisible}
        width={600}
      >
        {selectedRecord && (
          <div>
            <Descriptions column={1} bordered>
              <Descriptions.Item label="岗位标题">{selectedRecord.requestTitle}</Descriptions.Item>
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
            
            <div style={{ marginTop: 24 }}>
              <h3 style={{ marginBottom: 16, fontSize: 16, fontWeight: 600 }}>审批历史</h3>
              {approvalHistory.length > 0 ? (
                <Table
                  dataSource={approvalHistory}
                  columns={[
                    {
                      title: '审批级别',
                      dataIndex: 'approvalLevel',
                      key: 'approvalLevel',
                      render: (level) => {
                        if (level === 1) return '第一级（编制管理岗）';
                        if (level === 2) return '第二级（外包招聘岗）';
                        if (level === 3) return '第三级（团队经理）';
                        return `第${level}级`;
                      }
                    },
                    {
                      title: '审批人',
                      dataIndex: 'approverName',
                      key: 'approverName'
                    },
                    {
                      title: '审批状态',
                      dataIndex: 'approvalStatus',
                      key: 'approvalStatus',
                      render: (status) => {
                        if (status === 'APPROVED') return <Tag color="success">通过</Tag>;
                        if (status === 'REJECTED') return <Tag color="error">拒绝</Tag>;
                        return status;
                      }
                    },
                    {
                      title: '审批意见',
                      dataIndex: 'approvalComment',
                      key: 'approvalComment',
                      render: (comment) => comment || '-'
                    },
                    {
                      title: '审批时间',
                      dataIndex: 'approvalTime',
                      key: 'approvalTime',
                      render: (time) => time ? new Date(time).toLocaleString('zh-CN') : '-'
                    }
                  ]}
                  rowKey="approvalHistoryId"
                  pagination={false}
                />
              ) : (
                <div style={{ textAlign: 'center', padding: '40px 0', color: '#999' }}>
                  暂无审批历史
                </div>
              )}
            </div>
          </div>
        )}
      </Drawer>

      <Drawer
        title="编辑申请"
        placement="right"
        onClose={handleEditDrawerClose}
        open={editDrawerVisible}
        width={600}
      >
        {selectedRecord ? (
          <div style={{ padding: '20px' }}>
            <form onSubmit={(e) => {
              e.preventDefault();
              const formData = new FormData(e.target);
              const values = {
                requestTitle: formData.get('requestTitle'),
                totalRecruitmentCount: parseInt(formData.get('totalRecruitmentCount')),
                vacancyCount: parseInt(formData.get('vacancyCount')),
                team: formData.get('team'),
                technicalPlatform: formData.get('technicalPlatform'),
                supplementCount: parseInt(formData.get('supplementCount')),
                proposedLevel: formData.get('proposedLevel'),
                experienceYears: formData.get('experienceYears'),
                skillRequirement: formData.get('skillRequirement'),
                positionResponsibility: formData.get('positionResponsibility')
              };
              handleEditDrawerOk(values);
            }}>
              <div style={{ marginBottom: 16 }}>
                <label style={{ display: 'block', marginBottom: 8 }}>岗位标题</label>
                <input 
                  type="text" 
                  name="requestTitle" 
                  defaultValue={selectedRecord.requestTitle || ''} 
                  placeholder="请输入岗位标题" 
                  style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #d9d9d9' }}
                  required
                />
              </div>
              <div style={{ display: 'flex', gap: 16, marginBottom: 16 }}>
                <div style={{ flex: 1 }}>
                  <label style={{ display: 'block', marginBottom: 8 }}>总编制人数</label>
                  <input 
                    type="number" 
                    name="totalRecruitmentCount" 
                    defaultValue={selectedRecord.totalRecruitmentCount || 0} 
                    placeholder="请输入总编制人数" 
                    style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #d9d9d9' }}
                    required
                  />
                </div>
                <div style={{ flex: 1 }}>
                  <label style={{ display: 'block', marginBottom: 8 }}>空缺编制</label>
                  <input 
                    type="number" 
                    name="vacancyCount" 
                    defaultValue={selectedRecord.vacancyCount || 0} 
                    placeholder="请输入空缺编制" 
                    style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #d9d9d9' }}
                    required
                  />
                </div>
              </div>
              <div style={{ display: 'flex', gap: 16, marginBottom: 16 }}>
                <div style={{ flex: 1 }}>
                  <label style={{ display: 'block', marginBottom: 8 }}>所属团队</label>
                  <select 
                    name="team" 
                    defaultValue={selectedRecord.team || ''} 
                    style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #d9d9d9' }}
                    required
                  >
                    <option value="">请选择所属团队</option>
                    <option value="零售">零售</option>
                    <option value="批发">批发</option>
                    <option value="基础">基础</option>
                    <option value="数据">数据</option>
                  </select>
                </div>
                <div style={{ flex: 1 }}>
                  <label style={{ display: 'block', marginBottom: 8 }}>技术平台</label>
                  <select 
                    name="technicalPlatform" 
                    defaultValue={selectedRecord.technicalPlatform || ''} 
                    style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #d9d9d9' }}
                    required
                  >
                    <option value="">请选择技术平台</option>
                    <option value="frontend">前端</option>
                    <option value="backend">后端</option>
                    <option value="mobile">移动端</option>
                    <option value="ai">人工智能</option>
                  </select>
                </div>
              </div>
              <div style={{ marginBottom: 16 }}>
                <label style={{ display: 'block', marginBottom: 8 }}>补充人数</label>
                <input 
                  type="number" 
                  name="supplementCount" 
                  defaultValue={selectedRecord.supplementCount || 0} 
                  placeholder="请输入补充人数" 
                  style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #d9d9d9' }}
                  required
                />
              </div>
              <div style={{ display: 'flex', gap: 16, marginBottom: 16 }}>
                <div style={{ flex: 1 }}>
                  <label style={{ display: 'block', marginBottom: 8 }}>建议级别</label>
                  <select 
                    name="proposedLevel" 
                    defaultValue={selectedRecord.proposedLevel || ''} 
                    style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #d9d9d9' }}
                    required
                  >
                    <option value="">请选择建议级别</option>
                    <option value="初级">初级</option>
                    <option value="中级">中级</option>
                    <option value="高级">高级</option>
                    <option value="资深">资深</option>
                  </select>
                </div>
                <div style={{ flex: 1 }}>
                  <label style={{ display: 'block', marginBottom: 8 }}>相关经验年限要求</label>
                  <select 
                    name="experienceYears" 
                    defaultValue={selectedRecord.experienceYears || ''} 
                    style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #d9d9d9' }}
                    required
                  >
                    <option value="">请选择经验年限</option>
                    <option value="0-1">0-1年</option>
                    <option value="1-3">1-3年</option>
                    <option value="3-5">3-5年</option>
                    <option value="5+">5年以上</option>
                  </select>
                </div>
              </div>
              <div style={{ marginBottom: 16 }}>
                <label style={{ display: 'block', marginBottom: 8 }}>技能要求描述</label>
                <textarea 
                  name="skillRequirement" 
                  defaultValue={selectedRecord.skillRequirement || ''} 
                  placeholder="请详细描述技能要求" 
                  rows={3} 
                  style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #d9d9d9' }}
                  required
                />
              </div>
              <div style={{ marginBottom: 16 }}>
                <label style={{ display: 'block', marginBottom: 8 }}>岗位职责</label>
                <textarea 
                  name="positionResponsibility" 
                  defaultValue={selectedRecord.positionResponsibility || ''} 
                  placeholder="请详细描述岗位职责和工作内容" 
                  rows={4} 
                  style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #d9d9d9' }}
                  required
                />
              </div>
              <div style={{ marginTop: 16 }}>
                <Button onClick={handleEditDrawerCancel}>取消</Button>
                <Button type="primary" htmlType="submit" style={{ marginLeft: 8 }}>
                  保存
                </Button>
              </div>
            </form>
          </div>
        ) : (
          <div style={{ padding: '20px', textAlign: 'center' }}>加载中...</div>
        )}
      </Drawer>
    </div>
  );
};

// 包装错误边界的组件
const ApprovalManagementWithErrorBoundary = () => (
  <ErrorBoundary>
    <ApprovalManagement />
  </ErrorBoundary>
);

export default ApprovalManagementWithErrorBoundary;