package com.hr.service.impl;

import com.hr.entity.Message;
import com.hr.entity.User;
import com.hr.mapper.UserMapper;
import com.hr.service.InterviewMessageService;
import com.hr.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class InterviewMessageServiceImpl implements InterviewMessageService {

    private static final String SYSTEM_USER_ID = "1001";
    private static final String SYSTEM_USER_NAME = "系统用户";

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserMapper userMapper;

    @Override
    public void sendInterviewerConfirmedMessage(String candidateName, String interviewerName, String supplierHrUserId) {
        if (supplierHrUserId == null || supplierHrUserId.trim().isEmpty()) {
            return;
        }
        String content = defaultValue(candidateName, "候选人") + "的简历已被"
            + defaultValue(interviewerName, "面试官") + "确认选择，请及时确认面试时间。";
        createMessage("面试安排提醒", content, supplierHrUserId, "供应商HR");
    }

    @Override
    public void sendInterviewerConfirmedMessageToOutsourcingManagers(String candidateName, String interviewerName) {
        List<User> users = userMapper.getActiveUsersByRoleKeyword("外包招聘管理");
        if (users == null || users.isEmpty()) {
            users = userMapper.getActiveUsersByRoleName("外包招聘管理岗");
        }
        if (users == null || users.isEmpty()) {
            users = userMapper.getActiveUsersByRoleName("外包招聘管理");
        }
        if (users == null || users.isEmpty()) {
            return;
        }
        String content = defaultValue(candidateName, "候选人") + "的简历已被"
            + defaultValue(interviewerName, "面试官") + "确认选择，请及时确认面试时间。";
        for (User user : users) {
            if (user == null || user.getUserId() == null || user.getUserId().trim().isEmpty()) {
                continue;
            }
            createMessage("面试安排提醒", content, user.getUserId(), "外包招聘管理岗");
        }
    }

    @Override
    public void sendInterviewTimeConfirmedMessage(String candidateName, String interviewerUserId, String supplierHrUserId) {
        String content = defaultValue(candidateName, "候选人") + "的面试时间已确认，请合理安排面试时间。";
        if (interviewerUserId != null && !interviewerUserId.trim().isEmpty()) {
            createMessage("面试时间确认提醒", content, interviewerUserId, "面试官");
        }
        if (supplierHrUserId != null && !supplierHrUserId.trim().isEmpty()) {
            createMessage("面试时间确认提醒", content, supplierHrUserId, "供应商HR");
        }
    }

    @Override
    public void sendEvaluationRejectedMessage(String interviewerUserId) {
        if (interviewerUserId == null || interviewerUserId.trim().isEmpty()) {
            return;
        }
        createMessage("面试评价审批提醒", "您有一个面试评价流程被驳回，请修改内容重新提交。", interviewerUserId, "面试官");
    }

    @Override
    public void sendEvaluationCompletedMessage(String interviewerUserId,
                                               String roomManagerUserId,
                                               String interviewerName,
                                               String interviewerDepartment,
                                               String candidateName,
                                               String entryLevelSuggestion,
                                               String interviewDateText) {
        String content = "面试评价完成提醒。您有一个面试评价流程已经完成，请注意查看！"
            + "面试官：" + defaultValue(interviewerName, "-")
            + "，面试室组：" + defaultValue(interviewerDepartment, "-")
            + "，候选人：" + defaultValue(candidateName, "-")
            + "，面试职级：" + defaultValue(entryLevelSuggestion, "-")
            + "，面试时间：" + defaultValue(interviewDateText, "-") + "。";

        Set<String> recipientIds = new LinkedHashSet<>();
        if (interviewerUserId != null && !interviewerUserId.trim().isEmpty()) {
            recipientIds.add(interviewerUserId);
        }
        if (roomManagerUserId != null && !roomManagerUserId.trim().isEmpty()) {
            recipientIds.add(roomManagerUserId);
        }

        List<User> outsourcingManagers = userMapper.getActiveUsersByRoleKeyword("外包招聘管理");
        if (outsourcingManagers == null || outsourcingManagers.isEmpty()) {
            outsourcingManagers = userMapper.getActiveUsersByRoleName("外包招聘管理岗");
        }
        if (outsourcingManagers == null || outsourcingManagers.isEmpty()) {
            outsourcingManagers = userMapper.getActiveUsersByRoleName("外包招聘管理");
        }
        if (outsourcingManagers != null) {
            for (User user : outsourcingManagers) {
                if (user != null && user.getUserId() != null && !user.getUserId().trim().isEmpty()) {
                    recipientIds.add(user.getUserId());
                }
            }
        }

        for (String userId : recipientIds) {
            createMessage("面试评价审批提醒", content, userId, null);
        }
    }

    private void createMessage(String title, String content, String targetUserId, String targetUserRole) {
        Message message = new Message();
        message.setTitle(title);
        message.setContent(content);
        message.setType("INTERVIEW");
        message.setStatus("UNREAD");
        message.setPriority("NORMAL");
        message.setTargetUserId(targetUserId);
        message.setTargetUserRole(targetUserRole);
        message.setCreateUserId(SYSTEM_USER_ID);
        message.setCreateUserName(SYSTEM_USER_NAME);
        message.setCreateTime(new Date());
        message.setIsDeleted(0);
        messageService.createMessage(message);
    }

    private String defaultValue(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }
}
