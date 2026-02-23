import React, { useState, useEffect } from 'react';
import { Card, List, Button, Modal, Form, Input, message, Badge } from 'antd';
import { BellOutlined, CloseOutlined, CheckOutlined, DeleteOutlined } from '@ant-design/icons';
import api from '../utils/api';
import { useAuth } from '../contexts/AuthContext';
import { useMessage } from '../contexts/MessageContext';

const MessageManagement = () => {
  const { messages, markAsRead, deleteMessage, addMessage } = useMessage();
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [form] = Form.useForm();
  const { user } = useAuth();



  const handleMarkAsRead = (id) => {
    markAsRead(id);
    message.success('已标记为已读');
  };

  const handleDelete = (id) => {
    deleteMessage(id);
    message.success('消息已删除');
  };

  const handlePublish = async (values) => {
    try {
      const newMessage = {
        id: Date.now(), // 使用时间戳作为唯一ID
        title: values.title,
        content: values.content,
        time: new Date().toLocaleString('zh-CN'),
        status: 'unread',
        type: 'system',
      };
      addMessage(newMessage);
      setModalVisible(false);
      form.resetFields();
      message.success('消息发布成功');
    } catch (error) {
      message.error('发布失败');
      console.error('Failed to publish message:', error);
    }
  };

  const [isMobile, setIsMobile] = useState(false);

  useEffect(() => {
    const handleResize = () => {
      setIsMobile(window.innerWidth < 768);
    };

    handleResize();
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  return (
    <div className="interview-scheduling">
      <h2>消息管理</h2>
      
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
        {user && (user.userId === '1001' || user.userId === 1001) && (
          <Button 
            type="primary" 
            icon={<BellOutlined />}
            onClick={() => setModalVisible(true)}
            size={isMobile ? 'small' : 'middle'}
          >
            发布消息
          </Button>
        )}
      </div>

      <Card>
        <List
          dataSource={messages}
          loading={loading}
          renderItem={(item) => (
            <List.Item
              style={{
                padding: '8px 0',
                borderBottom: '1px solid #f0f0f0',
                cursor: 'pointer',
              }}
              actions={[
                item.status === 'unread' && (
                  <Button 
                    type="link" 
                    icon={<CheckOutlined />}
                    size={isMobile ? 'small' : 'middle'}
                    onClick={() => handleMarkAsRead(item.id)}
                  >
                    标记已读
                  </Button>
                ),
                (user && (user.userId === '1001' || user.userId === 1001)) && (
                  <Button 
                    type="link" 
                    danger 
                    icon={<DeleteOutlined />}
                    size={isMobile ? 'small' : 'middle'}
                    onClick={() => handleDelete(item.id)}
                  >
                    删除
                  </Button>
                ),
              ].filter(Boolean)}
            >
              <List.Item.Meta
                avatar={
                  <Badge dot={item.status === 'unread'}>
                    <div style={{ width: 24, height: 24, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                      <BellOutlined style={{ fontSize: 16 }} />
                    </div>
                  </Badge>
                }
                title={
                  <div style={{ 
                    fontSize: 14, 
                    fontWeight: item.status === 'unread' ? 'bold' : 'normal',
                    color: item.status === 'unread' ? '#001529' : '#666'
                  }}>
                    {item.title}
                  </div>
                }
                description={
                  <div>
                    <div style={{ fontSize: 12, color: '#666', marginBottom: 4 }}>
                      {item.content}
                    </div>
                    <div style={{ fontSize: 11, color: '#999' }}>
                      {item.time}
                    </div>
                  </div>
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
        okText="发布"
        cancelText="取消"
        width={isMobile ? '90%' : 600}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handlePublish}
        >
          <Form.Item
            name="title"
            label="消息标题"
            rules={[{ required: true, message: '请输入消息标题' }]}
          >
            <Input placeholder="请输入消息标题" size={isMobile ? 'small' : 'middle'} />
          </Form.Item>
          
          <Form.Item
            name="content"
            label="消息内容"
            rules={[{ required: true, message: '请输入消息内容' }]}
          >
            <Input.TextArea 
              rows={4} 
              placeholder="请输入消息内容"
              size={isMobile ? 'small' : 'middle'}
            />
          </Form.Item>
        </Form>
      </Modal>

      <div style={{ textAlign: 'center', marginTop: 8, color: '#999', fontSize: 10 }}>
        © 2024人力资源管理系统 - 消息管理模块
      </div>
    </div>
  );
};

export default MessageManagement;