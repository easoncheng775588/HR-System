import React, { createContext, useContext, useState, useEffect } from 'react';

const MessageContext = createContext();

export const useMessage = () => {
  const context = useContext(MessageContext);
  if (!context) {
    throw new Error('useMessage must be used within a MessageProvider');
  }
  return context;
};

export const MessageProvider = ({ children }) => {
  const [messages, setMessages] = useState(() => {
    // 从localStorage读取最新消息状态
    const latestMessages = localStorage.getItem('latestMessages');
    if (latestMessages) {
      try {
        return JSON.parse(latestMessages);
      } catch (error) {
        console.error('Failed to parse latest messages:', error);
      }
    }
    // 默认消息
    return [
      { id: 1, title: '系统通知：系统维护公告', time: '今天 09:00', status: 'unread' },
      { id: 2, title: '人事通知：新员工入职', time: '昨天 15:30', status: 'unread' },
    ];
  });

  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    // 计算未读消息数量
    const count = messages.filter(msg => msg.status === 'unread').length;
    setUnreadCount(count);
    // 同步到localStorage
    localStorage.setItem('latestMessages', JSON.stringify(messages));
  }, [messages]);

  const markAsRead = (id) => {
    setMessages(prevMessages => 
      prevMessages.map(msg => 
        msg.id === id ? { ...msg, status: 'read' } : msg
      )
    );
  };

  const deleteMessage = (id) => {
    setMessages(prevMessages => 
      prevMessages.filter(msg => msg.id !== id)
    );
  };

  const addMessage = (message) => {
    setMessages(prevMessages => 
      [message, ...prevMessages]
    );
  };

  const value = {
    messages,
    unreadCount,
    markAsRead,
    deleteMessage,
    addMessage,
    setMessages
  };

  return (
    <MessageContext.Provider value={value}>
      {children}
    </MessageContext.Provider>
  );
};