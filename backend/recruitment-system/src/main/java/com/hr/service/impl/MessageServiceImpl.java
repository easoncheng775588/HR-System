package com.hr.service.impl;

import com.hr.entity.Message;
import com.hr.mapper.MessageMapper;
import com.hr.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Override
    @Transactional
    public Message createMessage(Message message) {
        if (message.getCreateTime() == null) {
            message.setCreateTime(new Date());
        }
        if (message.getStatus() == null) {
            message.setStatus("UNREAD");
        }
        if (message.getPriority() == null) {
            message.setPriority("NORMAL");
        }
        if (message.getIsDeleted() == null) {
            message.setIsDeleted(0);
        }
        messageMapper.insert(message);
        return message;
    }

    @Override
    @Transactional
    public Message updateMessage(Message message) {
        if (message.getMessageId() == null) {
            throw new RuntimeException("消息ID不能为空");
        }
        messageMapper.updateByPrimaryKey(message);
        return messageMapper.selectByPrimaryKey(message.getMessageId());
    }

    @Override
    @Transactional
    public void deleteMessage(Long messageId) {
        if (messageId == null) {
            throw new RuntimeException("消息ID不能为空");
        }
        messageMapper.softDelete(messageId);
    }

    @Override
    public Message getMessageById(Long messageId) {
        if (messageId == null) {
            throw new RuntimeException("消息ID不能为空");
        }
        return messageMapper.selectByPrimaryKey(messageId);
    }

    @Override
    public List<Message> getAllMessages() {
        return messageMapper.selectAll();
    }

    @Override
    public List<Message> getMessagesByTargetUserId(String targetUserId) {
        return messageMapper.selectByTargetUserId(targetUserId);
    }

    @Override
    public List<Message> getMessagesByTargetUserRole(String targetUserRole) {
        return messageMapper.selectByTargetUserRole(targetUserRole);
    }

    @Override
    public List<Message> getMessagesByStatus(String status) {
        return messageMapper.selectByStatus(status);
    }

    @Override
    public List<Message> getMessagesByType(String type) {
        return messageMapper.selectByType(type);
    }

    @Override
    public int getUnreadCount(String targetUserId) {
        return messageMapper.countByTargetUserIdAndStatus(targetUserId, "UNREAD");
    }

    @Override
    @Transactional
    public void markAsRead(Long messageId) {
        if (messageId == null) {
            throw new RuntimeException("消息ID不能为空");
        }
        messageMapper.updateReadTime(messageId);
    }

    @Override
    @Transactional
    public void batchMarkAsRead(List<Long> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            throw new RuntimeException("消息ID列表不能为空");
        }
        messageMapper.batchUpdateReadTime(messageIds);
    }
}