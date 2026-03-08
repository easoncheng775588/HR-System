import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import type { ApiLikeError } from '../utils/errorHandler';

const API_BASE_URL = 'http://localhost:8080/api';
const MessageContext = createContext(null);
const toApiError = (error: unknown): ApiLikeError => (error as ApiLikeError);

export const useMessage = () => {
  const ctx = useContext(MessageContext);
  if (!ctx) throw new Error('useMessage must be used within a MessageProvider');
  return ctx;
};

const formatTime = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  const now = new Date();
  const diff = now - date;

  if (diff < 60_000) return '刚刚';
  if (diff < 3_600_000) return `${Math.floor(diff / 60_000)} 分钟前`;
  if (diff < 86_400_000) return `${Math.floor(diff / 3_600_000)} 小时前`;
  if (diff < 172_800_000) return '昨天';
  return date.toLocaleString('zh-CN');
};

export const MessageProvider = ({ children, userId }) => {
  const [messages, setMessages] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(false);

  const normalizeMessages = useCallback((list = []) =>
    list.map((msg) => ({
      id: msg.messageId,
      title: msg.title,
      content: msg.content,
      type: String(msg.type || 'SYSTEM').toLowerCase(),
      status: String(msg.status || 'UNREAD').toLowerCase(),
      priority: String(msg.priority || 'NORMAL').toLowerCase(),
      time: formatTime(msg.createTime),
      targetUserId: msg.targetUserId,
      targetUserRole: msg.targetUserRole,
    })), []);

  const fetchMessages = useCallback(async () => {
    if (!userId) return;
    setLoading(true);
    try {
      const response = await fetch(`${API_BASE_URL}/message/user/${userId}`);
      const data = await response.json();
      if (data.returnCode === 'SUC0000') {
        setMessages(normalizeMessages(data.body));
      }
    } catch (error) {
      console.error('Failed to fetch messages:', toApiError(error));
    } finally {
      setLoading(false);
    }
  }, [normalizeMessages, userId]);

  const fetchUnreadCount = useCallback(async () => {
    if (!userId) return;
    try {
      const response = await fetch(`${API_BASE_URL}/message/unread-count/${userId}`);
      const data = await response.json();
      if (data.returnCode === 'SUC0000') {
        setUnreadCount(data.body?.unreadCount || 0);
      }
    } catch (error) {
      console.error('Failed to fetch unread count:', toApiError(error));
    }
  }, [userId]);

  useEffect(() => {
    if (userId) {
      fetchMessages();
      fetchUnreadCount();
    }
  }, [userId, fetchMessages, fetchUnreadCount]);

  const markAsRead = useCallback(async (id) => {
    try {
      const response = await fetch(`${API_BASE_URL}/message/${id}/mark-read`, { method: 'POST' });
      const data = await response.json();
      if (data.returnCode === 'SUC0000') {
        setMessages((prev) => prev.map((m) => (m.id === id ? { ...m, status: 'read' } : m)));
        setUnreadCount((prev) => Math.max(0, prev - 1));
      }
    } catch (error) {
      console.error('Failed to mark message as read:', toApiError(error));
    }
  }, []);

  const deleteMessage = useCallback(async (id) => {
    try {
      const wasUnread = messages.find((m) => m.id === id)?.status === 'unread';
      const response = await fetch(`${API_BASE_URL}/message/${id}`, { method: 'DELETE' });
      const data = await response.json();
      if (data.returnCode === 'SUC0000') {
        setMessages((prev) => prev.filter((m) => m.id !== id));
        if (wasUnread) setUnreadCount((prev) => Math.max(0, prev - 1));
      }
    } catch (error) {
      console.error('Failed to delete message:', toApiError(error));
    }
  }, [messages]);

  const addMessage = useCallback(async (msg) => {
    try {
      const response = await fetch(`${API_BASE_URL}/message/create`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          title: msg.title,
          content: msg.content,
          type: msg.type || 'SYSTEM',
          status: 'UNREAD',
          priority: msg.priority || 'NORMAL',
          targetUserId: msg.targetUserId || null,
          targetUserRole: msg.targetUserRole || null,
          createUserId: String(userId || ''),
          createUserName: '系统用户',
        }),
      });
      const data = await response.json();
      if (data.returnCode === 'SUC0000' && data.body) {
        const newMessage = normalizeMessages([data.body])[0];
        setMessages((prev) => [newMessage, ...prev]);
        setUnreadCount((prev) => prev + 1);
        return true;
      }
      return false;
    } catch (error) {
      console.error('Failed to add message:', toApiError(error));
      return false;
    }
  }, [normalizeMessages, userId]);

  const batchMarkAsRead = useCallback(async () => {
    try {
      const unreadIds = messages.filter((m) => m.status === 'unread').map((m) => m.id);
      if (!unreadIds.length) return;
      const response = await fetch(`${API_BASE_URL}/message/batch-mark-read`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(unreadIds),
      });
      const data = await response.json();
      if (data.returnCode === 'SUC0000') {
        setMessages((prev) => prev.map((m) => ({ ...m, status: 'read' })));
        setUnreadCount(0);
      }
    } catch (error) {
      console.error('Failed to batch mark messages as read:', toApiError(error));
    }
  }, [messages]);

  const value = useMemo(
    () => ({
      messages,
      unreadCount,
      loading,
      markAsRead,
      deleteMessage,
      addMessage,
      batchMarkAsRead,
      fetchMessages,
      fetchUnreadCount,
      setMessages,
    }),
    [
      messages,
      unreadCount,
      loading,
      markAsRead,
      deleteMessage,
      addMessage,
      batchMarkAsRead,
      fetchMessages,
      fetchUnreadCount,
    ]
  );

  return <MessageContext.Provider value={value}>{children}</MessageContext.Provider>;
};
