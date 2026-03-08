package com.hr.controller;

import com.hr.common.Response;
import com.hr.common.BusinessException;
import com.hr.entity.Message;
import com.hr.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/message")
@CrossOrigin(origins = "*")
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private MessageService messageService;

    private void validateMessage(Message message) {
        if (message == null) {
            throw new BusinessException("ERR0001", "消息数据不能为空");
        }
        if (message.getTitle() == null || message.getTitle().trim().isEmpty()) {
            throw new BusinessException("ERR0002", "消息标题不能为空");
        }
        if (message.getContent() == null || message.getContent().trim().isEmpty()) {
            throw new BusinessException("ERR0003", "消息内容不能为空");
        }
        if (message.getType() == null || message.getType().trim().isEmpty()) {
            throw new BusinessException("ERR0004", "消息类型不能为空");
        }
        if (message.getCreateUserId() == null || message.getCreateUserId().trim().isEmpty()) {
            throw new BusinessException("ERR0005", "创建用户ID不能为空");
        }
        if (message.getCreateUserName() == null || message.getCreateUserName().trim().isEmpty()) {
            throw new BusinessException("ERR0006", "创建用户姓名不能为空");
        }
    }

    @PostMapping("/create")
    public Response<?> createMessage(@RequestBody Message message) {
        try {
            validateMessage(message);
            logger.info("开始创建消息: {}", message.getTitle());
            Message createdMessage = messageService.createMessage(message);
            logger.info("创建消息成功: {}", createdMessage.getMessageId());
            return Response.success(createdMessage);
        } catch (BusinessException e) {
            logger.error("创建消息失败: {}", e.getMessage());
            return Response.fail(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("创建消息失败: {}", message.getTitle(), e);
            return Response.fail("创建消息失败：" + e.getMessage());
        }
    }

    @PutMapping("/update")
    public Response<?> updateMessage(@RequestBody Message message) {
        try {
            if (message.getMessageId() == null) {
                throw new BusinessException("ERR0007", "消息ID不能为空");
            }
            logger.info("开始更新消息: {}", message.getMessageId());
            Message updatedMessage = messageService.updateMessage(message);
            logger.info("更新消息成功: {}", updatedMessage.getMessageId());
            return Response.success(updatedMessage);
        } catch (BusinessException e) {
            logger.error("更新消息失败: {}", e.getMessage());
            return Response.fail(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("更新消息失败: {}", message.getMessageId(), e);
            return Response.fail("更新消息失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Response<?> deleteMessage(@PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                throw new BusinessException("ERR0008", "消息ID必须大于0");
            }
            logger.info("开始删除消息: {}", id);
            messageService.deleteMessage(id);
            logger.info("删除消息成功: {}", id);
            return Response.success(null);
        } catch (BusinessException e) {
            logger.error("删除消息失败: {}", e.getMessage());
            return Response.fail(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("删除消息失败: {}", id, e);
            return Response.fail("删除消息失败：" + e.getMessage());
        }
    }

    @GetMapping("/list")
    public Response<?> listMessages() {
        try {
            logger.info("开始获取消息列表");
            List<Message> messages = messageService.getAllMessages();
            logger.info("获取消息列表成功，共{}条", messages.size());
            return Response.success(messages);
        } catch (Exception e) {
            logger.error("获取消息列表失败", e);
            return Response.fail("获取消息列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Response<?> getMessage(@PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                throw new BusinessException("ERR0008", "消息ID必须大于0");
            }
            logger.info("开始获取消息详情: {}", id);
            Message message = messageService.getMessageById(id);
            logger.info("获取消息详情成功: {}", id);
            return Response.success(message);
        } catch (BusinessException e) {
            logger.error("获取消息详情失败: {}", e.getMessage());
            return Response.fail(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("获取消息详情失败: {}", id, e);
            return Response.fail("获取消息详情失败：" + e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public Response<?> getMessagesByUser(@PathVariable String userId) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                throw new BusinessException("ERR0009", "用户ID不能为空");
            }
            logger.info("开始获取用户消息列表: {}", userId);
            List<Message> messages = messageService.getMessagesByTargetUserId(userId);
            logger.info("获取用户消息列表成功，共{}条", messages.size());
            return Response.success(messages);
        } catch (BusinessException e) {
            logger.error("获取用户消息列表失败: {}", e.getMessage());
            return Response.fail(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("获取用户消息列表失败: {}", userId, e);
            return Response.fail("获取用户消息列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/role/{role}")
    public Response<?> getMessagesByRole(@PathVariable String role) {
        try {
            if (role == null || role.trim().isEmpty()) {
                throw new BusinessException("ERR0010", "角色不能为空");
            }
            logger.info("开始获取角色消息列表: {}", role);
            List<Message> messages = messageService.getMessagesByTargetUserRole(role);
            logger.info("获取角色消息列表成功，共{}条", messages.size());
            return Response.success(messages);
        } catch (BusinessException e) {
            logger.error("获取角色消息列表失败: {}", e.getMessage());
            return Response.fail(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("获取角色消息列表失败: {}", role, e);
            return Response.fail("获取角色消息列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/status/{status}")
    public Response<?> getMessagesByStatus(@PathVariable String status) {
        try {
            if (status == null || status.trim().isEmpty()) {
                throw new BusinessException("ERR0011", "状态不能为空");
            }
            logger.info("开始获取{}状态的消息列表", status);
            List<Message> messages = messageService.getMessagesByStatus(status);
            logger.info("获取{}状态的消息列表成功，共{}条", status, messages.size());
            return Response.success(messages);
        } catch (BusinessException e) {
            logger.error("获取状态消息列表失败: {}", e.getMessage());
            return Response.fail(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("获取状态消息列表失败: {}", status, e);
            return Response.fail("获取状态消息列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/type/{type}")
    public Response<?> getMessagesByType(@PathVariable String type) {
        try {
            if (type == null || type.trim().isEmpty()) {
                throw new BusinessException("ERR0012", "类型不能为空");
            }
            logger.info("开始获取{}类型的消息列表", type);
            List<Message> messages = messageService.getMessagesByType(type);
            logger.info("获取{}类型的消息列表成功，共{}条", type, messages.size());
            return Response.success(messages);
        } catch (BusinessException e) {
            logger.error("获取类型消息列表失败: {}", e.getMessage());
            return Response.fail(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("获取类型消息列表失败: {}", type, e);
            return Response.fail("获取类型消息列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/unread-count/{userId}")
    public Response<?> getUnreadCount(@PathVariable String userId) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                throw new BusinessException("ERR0009", "用户ID不能为空");
            }
            logger.info("开始获取用户未读消息数量: {}", userId);
            int count = messageService.getUnreadCount(userId);
            logger.info("获取用户未读消息数量成功: {}", count);
            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId);
            result.put("unreadCount", count);
            return Response.success(result);
        } catch (BusinessException e) {
            logger.error("获取未读消息数量失败: {}", e.getMessage());
            return Response.fail(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("获取未读消息数量失败: {}", userId, e);
            return Response.fail("获取未读消息数量失败：" + e.getMessage());
        }
    }

    @PostMapping("/{id}/mark-read")
    public Response<?> markAsRead(@PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                throw new BusinessException("ERR0008", "消息ID必须大于0");
            }
            logger.info("开始标记消息为已读: {}", id);
            messageService.markAsRead(id);
            logger.info("标记消息为已读成功: {}", id);
            return Response.success(null);
        } catch (BusinessException e) {
            logger.error("标记消息为已读失败: {}", e.getMessage());
            return Response.fail(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("标记消息为已读失败: {}", id, e);
            return Response.fail("标记消息为已读失败：" + e.getMessage());
        }
    }

    @PostMapping("/batch-mark-read")
    public Response<?> batchMarkAsRead(@RequestBody List<Long> messageIds) {
        try {
            if (messageIds == null || messageIds.isEmpty()) {
                throw new BusinessException("ERR0013", "消息ID列表不能为空");
            }
            logger.info("开始批量标记消息为已读，共{}条", messageIds.size());
            messageService.batchMarkAsRead(messageIds);
            logger.info("批量标记消息为已读成功");
            return Response.success(null);
        } catch (BusinessException e) {
            logger.error("批量标记消息为已读失败: {}", e.getMessage());
            return Response.fail(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("批量标记消息为已读失败", e);
            return Response.fail("批量标记消息为已读失败：" + e.getMessage());
        }
    }
}