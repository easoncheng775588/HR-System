package com.hr.service;

import com.hr.entity.Message;
import java.util.List;

public interface MessageService {
    Message createMessage(Message message);
    Message updateMessage(Message message);
    void deleteMessage(Long messageId);
    Message getMessageById(Long messageId);
    List<Message> getAllMessages();
    List<Message> getMessagesByTargetUserId(String targetUserId);
    List<Message> getMessagesByTargetUserRole(String targetUserRole);
    List<Message> getMessagesByStatus(String status);
    List<Message> getMessagesByType(String type);
    int getUnreadCount(String targetUserId);
    void markAsRead(Long messageId);
    void batchMarkAsRead(List<Long> messageIds);
}