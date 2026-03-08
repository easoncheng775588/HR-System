package com.hr.mapper;

import com.hr.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface MessageMapper {
    int insert(Message message);
    int updateByPrimaryKey(Message message);
    int deleteByPrimaryKey(Long messageId);
    Message selectByPrimaryKey(Long messageId);
    List<Message> selectAll();
    List<Message> selectByTargetUserId(String targetUserId);
    List<Message> selectByTargetUserRole(String targetUserRole);
    List<Message> selectByStatus(String status);
    List<Message> selectByType(String type);
    int countByTargetUserIdAndStatus(String targetUserId, String status);
    int updateReadTime(Long messageId);
    int batchUpdateReadTime(List<Long> messageIds);
    int softDelete(Long messageId);
}