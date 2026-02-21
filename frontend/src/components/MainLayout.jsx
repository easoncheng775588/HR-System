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
  CalendarOutlined
} from '@ant-design/icons';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { useMessage } from '../contexts/MessageContext';
import api from '../utils/api';

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
        // 获取待审核用人申请数量
        const approvalResponse = await api.get('/api/recruitment-request/approval/pending');
        const pendingApprovalCount = approvalResponse.data.returnCode === 'SUC0000' 
          ? (approvalResponse.data.body ? approvalResponse.data.body.length : 0) 
          : 0;

        // 获取待面试数量
        const interviewResponse = await api.get('/api/interview/pending/count');
        const pendingInterviewCount = interviewResponse.data.returnCode === 'SUC0000' 
          ? interviewResponse.data.body 
          : 0;

        setTodoItems([
          { id: 1, title: '审核用人申请', time: '10分钟前', status: 'pending', count: pendingApprovalCount },
          { id: 2, title: '待面试', time: '10分钟前', status: 'pending', count: pendingInterviewCount },
        ]);
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
      icon: <CheckCircleOutlined />,
      label: '欢迎页面',
    },
    {
      key: 'recruitment-request',
      icon: <CheckCircleOutlined />,
      label: '用人申请',
    },
    {
      key: 'approval-management',
      icon: <BellOutlined />,
      label: '审批管理',
    },
    {
      key: 'position-publishing',
      icon: <CheckCircleOutlined />,
      label: '岗位发布',
    },
    {
      key: 'resume-submission',
      icon: <FileTextOutlined />,
      label: '简历提交',
    },
    {
      key: 'resume-screening',
      icon: <CheckCircleOutlined />,
      label: '简历筛选',
    },
    {
      key: 'interview-scheduling',
      icon: <CalendarOutlined />,
      label: '面试安排',
    },
    {
      key: 'user-management',
      icon: <UserOutlined />,
      label: '用户管理',
    },
    {
      key: 'message-management',
      icon: <BellOutlined />,
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
      <div style={{ 
        height: 64, 
        display: 'flex', 
        alignItems: 'center', 
        justifyContent: 'center',
        color: '#fff',
        fontSize: collapsed ? 16 : 20,
        fontWeight: 'bold',
        borderBottom: '1px solid rgba(255,255,255,0.1)'
      }}>
        {collapsed ? 'HR' : '人事系统'}
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
          <Header style={{ 
            padding: '0 16px', 
            background: '#fff',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
            height: 64
          }}>
            <Button 
              type="text" 
              icon={<MenuUnfoldOutlined />}
              onClick={toggleDrawer}
              style={{ fontSize: 20 }}
            />
            <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
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
                <div style={{ display: 'flex', alignItems: 'center', cursor: 'pointer' }}>
                  <Avatar icon={<UserOutlined />} style={{ marginRight: 6 }} />
                  <span style={{ fontSize: 14 }}>{user ? user.realName : '未登录'}</span>
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
            <Header style={{ 
              padding: '0 24px', 
              background: '#fff',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
            }}>
              <div style={{ display: 'flex', alignItems: 'center' }}>
                {React.createElement(collapsed ? MenuUnfoldOutlined : MenuFoldOutlined, {
                  style: { fontSize: 18, cursor: 'pointer' },
                  onClick: () => setCollapsed(!collapsed),
                })}
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
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
                  <div style={{ display: 'flex', alignItems: 'center', cursor: 'pointer' }}>
                    <Avatar icon={<UserOutlined />} style={{ marginRight: 8 }} />
                    <span>{user ? user.realName : '未登录'}</span>
                  </div>
                </Dropdown>
              </div>
            </Header>
            <Content style={{ 
              margin: isMobile ? 12 : 24, 
              padding: isMobile ? 12 : 24, 
              minHeight: 280,
              background: '#ffffff',
              overflow: 'hidden'
            }}>
              <div style={{ 
                display: 'flex', 
                gap: isMobile ? 0 : 24,
                flexDirection: isMobile ? 'column' : 'row',
                height: '100%'
              }}>
                <div style={{ 
                  flex: 1,
                  overflow: 'auto',
                  maxHeight: isMobile ? 'calc(100vh - 100px)' : 'auto'
                }}>
                  {children}
                </div>
                {!isMobile && (
                  <div style={{ 
                    width: 280, 
                    display: 'flex', 
                    flexDirection: 'column', 
                    gap: 16,
                    flexShrink: 0,
                    overflow: 'auto',
                    maxHeight: 'calc(100vh - 100px)'
                  }}>
                    <Card title="待办事项" size="small" style={{ marginBottom: 0 }}>
                      <List
                        size="small"
                        dataSource={todoItems}
                        renderItem={(item) => (
                          <List.Item 
                            style={{ padding: '8px 0', cursor: 'pointer' }}
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
                        <Avatar size={48} icon={<UserOutlined />} style={{ marginBottom: 12 }} />
                        <h3 style={{ fontSize: 14, margin: '0 0 8px 0' }}>{user ? user.realName : '未登录'}</h3>
                        <p style={{ color: '#666', margin: 0, fontSize: 12 }}>{user ? user.position : '-'}</p>
                        <div style={{ marginTop: 12, textAlign: 'left', fontSize: 12 }}>
                          <p style={{ margin: '4px 0' }}><strong>用户ID：</strong>{user ? user.userId : '-'}</p>
                          <p style={{ margin: '4px 0' }}><strong>部门：</strong>{user ? user.department : '-'}</p>
                          <p style={{ margin: '4px 0' }}><strong>职位：</strong>{user ? user.position : '-'}</p>
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