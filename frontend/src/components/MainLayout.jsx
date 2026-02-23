import React, { useState, useEffect } from 'react';
import { Layout, Menu, Avatar, Dropdown, Badge, Card, List, Drawer, Button } from 'antd';
import { 
  UserOutlined, 
  BellOutlined, 
  CheckCircleOutlined, 
  MenuFoldOutlined, 
  MenuUnfoldOutlined,
  LogoutOutlined,
  SettingOutlined,
  CloseOutlined,
  FileTextOutlined,
  CalendarOutlined,
  HomeOutlined,
  SolutionOutlined,
  FormOutlined,
  TableOutlined,
  StarOutlined,
  MessageOutlined
} from '@ant-design/icons';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { useMessage } from '../contexts/MessageContext';
import api from '../utils/api';
import './MainLayout.css';

const { Header, Sider, Content } = Layout;

const MainLayout = ({ children }) => {
  const { user, hasMenuAccess, logout } = useAuth();
  const { messages, unreadCount, markAsRead } = useMessage();
  const [collapsed, setCollapsed] = useState(false);
  const [isMobile, setIsMobile] = useState(false);
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [todoItems, setTodoItems] = useState([]);

  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    // 检查是否为移动设备
    const handleResize = () => {
      setIsMobile(window.innerWidth < 768);
    };
    
    handleResize();
    window.addEventListener('resize', handleResize);
    
    // 获取待办任务数量
    const fetchPendingTasks = async () => {
      try {
        // 只有具有审批权限的角色才能获取待审核用人申请数量
        const hasApprovalPermission = user && [
          '编制管理岗',
          '外包招聘岗',
          '团队经理'
        ].includes(user.position);

        let pendingApprovalCount = 0;
        if (hasApprovalPermission) {
          // 获取待审核用人申请数量
          const approvalResponse = await api.get('/api/recruitment-request/approval/pending');
          pendingApprovalCount = approvalResponse.data.returnCode === 'SUC0000' 
            ? (approvalResponse.data.body ? approvalResponse.data.body.length : 0) 
            : 0;
        }

        // 获取待面试数量
        const interviewResponse = await api.get('/api/interview/pending/count');
        const pendingInterviewCount = interviewResponse.data.returnCode === 'SUC0000' 
          ? interviewResponse.data.body 
          : 0;

        // 构建待办事项列表
        const newTodoItems = [];
        
        // 只有具有审批权限的角色才能看到审核用人申请待办事项
        if (hasApprovalPermission) {
          newTodoItems.push({
            id: 1, 
            title: '审核用人申请', 
            time: '10分钟前', 
            status: 'pending', 
            count: pendingApprovalCount 
          });
        }
        
        // 所有角色都能看到待面试待办事项
        newTodoItems.push({
          id: 2, 
          title: '待面试', 
          time: '10分钟前', 
          status: 'pending', 
          count: pendingInterviewCount 
        });

        setTodoItems(newTodoItems);
      } catch (error) {
        console.error('Failed to fetch pending tasks:', error);
      }
    };

    fetchPendingTasks();

    // 监听审批完成事件
    const handleStorageChange = (e) => {
      if (e.key === 'approvalCompleted' && e.newValue === 'true') {
        fetchPendingTasks();
        // 重置标志
        localStorage.removeItem('approvalCompleted');
      }
      if (e.key === 'recruitmentRequestCreated' && e.newValue === 'true') {
        fetchPendingTasks();
        // 重置标志
        localStorage.removeItem('recruitmentRequestCreated');
      }
      if (e.key === 'interviewResultUpdated' && e.newValue === 'true') {
        fetchPendingTasks();
        // 重置标志
        localStorage.removeItem('interviewResultUpdated');
      }
    };

    window.addEventListener('storage', handleStorageChange);

    // 每5秒自动刷新一次待审核数量
    const intervalId = setInterval(() => {
      fetchPendingTasks();
    }, 5000);

    return () => {
      window.removeEventListener('resize', handleResize);
      window.removeEventListener('storage', handleStorageChange);
      clearInterval(intervalId);
    };
  }, []);

  const allMenuItems = [
    {
      key: 'dashboard',
      icon: <HomeOutlined />,
      label: '欢迎页面',
    },
    {
      key: 'recruitment-request',
      icon: <FormOutlined />,
      label: '用人申请',
    },
    {
      key: 'approval-management',
      icon: <CheckCircleOutlined />,
      label: '用人审批',
    },
    {
      key: 'position-publishing',
      icon: <TableOutlined />,
      label: '岗位发布',
    },
    {
      key: 'resume-submission',
      icon: <FileTextOutlined />,
      label: '简历提交',
    },
    {
      key: 'resume-screening',
      icon: <StarOutlined />,
      label: '简历筛选',
    },
    {
          key: 'interview-scheduling',
          icon: <CalendarOutlined />,
          label: '面试安排',
        },
        {
          key: 'offer-management',
          icon: <CheckCircleOutlined />,
          label: '录用管理',
        },
    {
      key: 'user-management',
      icon: <UserOutlined />,
      label: '用户管理',
    },
    {
      key: 'message-management',
      icon: <MessageOutlined />,
      label: '消息管理',
    },
  ];

  const menuItems = allMenuItems.filter(item => {
    const hasAccess = hasMenuAccess('/' + item.key);
    return hasAccess;
  });

  const userMenuItems = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: '个人信息',
    },
    {
      key: 'settings',
      icon: <SettingOutlined />,
      label: '系统设置',
    },
    {
      type: 'divider',
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
      danger: true,
    },
  ];

  const handleMenuClick = ({ key }) => {
    // 使用正确的路径格式进行导航
    navigate('/' + key);
    if (isMobile) {
      setDrawerVisible(false);
    }
  };

  const handleUserMenuClick = ({ key }) => {
    if (key === 'logout') {
      logout();
    } else if (key === 'profile') {
      // 个人信息页面导航
    } else if (key === 'settings') {
      // 系统设置页面导航
    }
  };

  const toggleDrawer = () => {
    setDrawerVisible(!drawerVisible);
  };

  const menuContent = (
    <div>
      <div className={`sidebar-header ${collapsed ? 'collapsed' : ''}`}>
        {collapsed ? 'HR' : '外包招聘管理系统'}
      </div>
      <Menu
        theme="dark"
        mode="inline"
        selectedKeys={[location.pathname.replace('/', '')]}
        items={menuItems}
        onClick={handleMenuClick}
      />
    </div>
  );

  return (
    <Layout style={{ minHeight: '100vh' }}>
      {isMobile ? (
        <>
          <Header className="mobile-header">
            <Button 
              type="text" 
              icon={<MenuUnfoldOutlined />}
              onClick={toggleDrawer}
              style={{ fontSize: 20 }}
            />
            <div className="header-actions">
                <Badge count={unreadCount} size="small">
                  <BellOutlined 
                    style={{ fontSize: 18, cursor: 'pointer' }}
                    onClick={() => navigate('/message-management')}
                  />
                </Badge>
              <Dropdown 
                menu={{ items: userMenuItems, onClick: handleUserMenuClick }}
                placement="bottomRight"
              >
                <div className="user-info">
                  <Avatar icon={<UserOutlined />} className="avatar" />
                  <span className="user-name">{user ? user.realName : '未登录'}</span>
                </div>
              </Dropdown>
            </div>
          </Header>
          <Drawer
            title="菜单"
            placement="left"
            onClose={toggleDrawer}
            open={drawerVisible}
            bodyStyle={{ padding: 0, background: '#001529' }}
            headerStyle={{ background: '#001529', color: '#fff', border: 'none' }}
            closeIcon={<CloseOutlined style={{ color: '#fff' }} />}
          >
            {menuContent}
          </Drawer>
        </>
      ) : (
        <>
          <Sider 
            trigger={null} 
            collapsible 
            collapsed={collapsed}
            style={{
              background: '#001529',
            }}
          >
            {menuContent}
          </Sider>
          <Layout>
            <Header className="desktop-header">
              <div style={{ display: 'flex', alignItems: 'center' }}>
                {React.createElement(collapsed ? MenuUnfoldOutlined : MenuFoldOutlined, {
                  style: { fontSize: 18, cursor: 'pointer' },
                  onClick: () => setCollapsed(!collapsed),
                })}
              </div>
              <div className="header-actions">
                <Badge count={unreadCount} size="small">
                  <BellOutlined 
                    style={{ fontSize: 18, cursor: 'pointer' }}
                    onClick={() => navigate('/message-management')}
                  />
                </Badge>
                <Dropdown 
                  menu={{ items: userMenuItems, onClick: handleUserMenuClick }}
                  placement="bottomRight"
                >
                  <div className="user-info">
                    <Avatar icon={<UserOutlined />} className="avatar" />
                    <span>{user ? user.realName : '未登录'}</span>
                  </div>
                </Dropdown>
              </div>
            </Header>
            <Content className={isMobile ? 'mobile-content-area' : 'content-area'}>
              <div className={isMobile ? 'mobile-main-content-container' : 'main-content-container'}>
                <div className={isMobile ? 'mobile-main-content' : 'main-content'}>
                  {children}
                </div>
                {!isMobile && (
                  <div className="sidebar">
                    <Card title="待办事项" size="small" style={{ marginBottom: 0 }}>
                      <List
                        size="small"
                        dataSource={todoItems}
                        renderItem={(item) => (
                          <List.Item 
                            style={{ padding: '2px 0', cursor: 'pointer' }}
                            onClick={() => {
                              if (item.id === 1) {
                                navigate('/approval-management');
                              } else if (item.id === 2) {
                                navigate('/interview-scheduling');
                              }
                            }}
                          >
                            <List.Item.Meta
                              title={
                                <div style={{ fontSize: 12, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                                  {item.title}
                                  {item.count > 0 && (
                                    <Badge 
                                      count={item.count} 
                                      size="small"
                                      style={{ 
                                        backgroundColor: '#6366f1',
                                        marginLeft: 8 
                                      }} 
                                    />
                                  )}
                                </div>
                              }
                              description={<div style={{ fontSize: 11 }}>{item.time}</div>}
                            />
                            {item.status === 'completed' && (
                              <CheckCircleOutlined style={{ color: '#52c41a', fontSize: 12 }} />
                            )}
                          </List.Item>
                        )}
                      />
                    </Card>
                    
                    <Card title="用户信息" size="small" style={{ marginBottom: 0 }}>
                      <div style={{ textAlign: 'center' }}>
                        <Avatar size={24} icon={<UserOutlined />} style={{ marginBottom: 2 }} />
                        <h3 style={{ fontSize: 11, margin: '0 0 1px 0' }}>{user ? user.realName : '未登录'}</h3>
                        <p style={{ color: '#666', margin: 0, fontSize: 10 }}>{user ? user.position : '-'}</p>
                        <div style={{ marginTop: 2, textAlign: 'left', fontSize: 10 }}>
                          <p style={{ margin: '2px 0', fontSize: '10px' }}><strong>用户ID：</strong>{user ? user.userId : '-'}</p>
                          <p style={{ margin: '2px 0', fontSize: '10px' }}><strong>部门：</strong>{user ? user.department : '-'}</p>
                          <p style={{ margin: '2px 0', fontSize: '10px' }}><strong>职位：</strong>{user ? user.position : '-'}</p>
                        </div>
                      </div>
                    </Card>
                    
                    <Card title="消息通知" size="small" style={{ marginBottom: 0 }}>
                      <List
                        size="small"
                        dataSource={messages}
                        renderItem={(item) => (
                          <List.Item 
                            style={{ 
                              padding: '8px 0',
                              cursor: 'pointer',
                              backgroundColor: item.status === 'unread' ? '#f0f9ff' : 'transparent'
                            }}
                            onClick={() => {
                              // 标记为已读
                              markAsRead(item.id);
                            }}
                          >
                            <List.Item.Meta
                              title={
                                <div style={{ 
                                  fontSize: 12,
                                  fontWeight: item.status === 'unread' ? 'bold' : 'normal'
                                }}>
                                  {item.title}
                                </div>
                              }
                              description={<div style={{ fontSize: 11 }}>{item.time}</div>}
                            />
                            {item.status === 'unread' && (
                              <Badge dot size="small" />
                            )}
                          </List.Item>
                        )}
                      />
                    </Card>
                  </div>
                )}
              </div>
            </Content>
          </Layout>
        </>
      )}
    </Layout>
  );
};

export default MainLayout;