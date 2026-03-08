import React, { useEffect, useState } from 'react';
import { Badge, Button, Card, Form, Input, List, Modal, Space, Typography, message } from 'antd';
import { BellOutlined, CheckOutlined, DeleteOutlined, PlusOutlined } from '@ant-design/icons';
import { useAuth } from '../contexts/AuthContext';
import { useMessage } from '../contexts/MessageContext';

const { Text } = Typography;

const MessageManagement = () => {
  const { user } = useAuth();
  const { messages, unreadCount, loading, markAsRead, deleteMessage, addMessage, batchMarkAsRead } = useMessage();

  const [modalVisible, setModalVisible] = useState(false);
  const [isMobile, setIsMobile] = useState(false);
  const [form] = Form.useForm();

  useEffect(() => {
    const onResize = () => setIsMobile(window.innerWidth < 768);
    onResize();
    window.addEventListener('resize', onResize);
    return () => window.removeEventListener('resize', onResize);
  }, []);

  const isAdmin = user && (String(user.userId) === '1001');

  const handlePublish = async (values) => {
    const ok = await addMessage({
      title: values.title,
      content: values.content,
      type: 'SYSTEM',
      priority: 'NORMAL',
    });

    if (ok) {
      message.success('消息发布成功');
      form.resetFields();
      setModalVisible(false);
    } else {
      message.error('消息发布失败');
    }
  };

  return (
    <div className="app-page">
      <div className="app-page-actions">
        <Space>
          <Text type="secondary">未读消息：{unreadCount}</Text>
        </Space>
        <Space>
          {unreadCount > 0 && <Button onClick={batchMarkAsRead}>全部已读</Button>}
          {isAdmin && (
            <Button type="primary" icon={<PlusOutlined />} onClick={() => setModalVisible(true)}>
              发布消息
            </Button>
          )}
        </Space>
      </div>

      <Card title="消息列表">
        <List
          loading={loading}
          dataSource={messages}
          locale={{ emptyText: '暂无消息' }}
          renderItem={(item) => (
            <List.Item
              actions={[
                item.status === 'unread' ? (
                  <Button type="link" icon={<CheckOutlined />} onClick={() => markAsRead(item.id)}>
                    标记已读
                  </Button>
                ) : null,
                isAdmin ? (
                  <Button type="link" danger icon={<DeleteOutlined />} onClick={() => deleteMessage(item.id)}>
                    删除
                  </Button>
                ) : null,
              ].filter(Boolean)}
            >
              <List.Item.Meta
                avatar={
                  <Badge dot={item.status === 'unread'}>
                    <BellOutlined style={{ fontSize: 18 }} />
                  </Badge>
                }
                title={<Text strong={item.status === 'unread'}>{item.title}</Text>}
                description={
                  <Space direction="vertical" size={2}>
                    <Text>{item.content}</Text>
                    <Text type="secondary" style={{ fontSize: 12 }}>{item.time}</Text>
                  </Space>
                }
              />
            </List.Item>
          )}
        />
      </Card>

      <Modal
        title="发布消息"
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={() => form.submit()}
        width={isMobile ? '94%' : 560}
        okText="发布"
        cancelText="取消"
      >
        <Form form={form} layout="vertical" onFinish={handlePublish}>
          <Form.Item name="title" label="消息标题" rules={[{ required: true, message: '请输入消息标题' }]}>
            <Input placeholder="请输入消息标题" />
          </Form.Item>
          <Form.Item name="content" label="消息内容" rules={[{ required: true, message: '请输入消息内容' }]}>
            <Input.TextArea rows={4} placeholder="请输入消息内容" />
          </Form.Item>
        </Form>
      </Modal>

      <div className="app-footer-note">© 2026 外包招聘管理系统</div>
    </div>
  );
};

export default MessageManagement;
