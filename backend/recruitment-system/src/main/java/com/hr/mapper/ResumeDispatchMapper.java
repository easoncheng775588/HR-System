package com.hr.mapper;

import com.hr.entity.ResumeDispatch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface ResumeDispatchMapper {
    int insert(ResumeDispatch dispatch);

    List<ResumeDispatch> selectByResumeId(@Param("resumeId") Long resumeId);

    List<ResumeDispatch> selectByResumeIds(@Param("resumeIds") List<Long> resumeIds);

    List<ResumeDispatch> selectByInterviewerId(@Param("interviewerId") String interviewerId);

    List<ResumeDispatch> selectConfirmed();

    ResumeDispatch selectConfirmedByResumeId(@Param("resumeId") Long resumeId);

    int deleteByResumeId(@Param("resumeId") Long resumeId);

    int updateStatusByResumeAndInterviewer(@Param("resumeId") Long resumeId,
                                           @Param("interviewerId") String interviewerId,
                                           @Param("dispatchStatus") String dispatchStatus,
                                           @Param("confirmTime") Date confirmTime,
                                           @Param("updateUserId") String updateUserId,
                                           @Param("updateUserName") String updateUserName);

    int confirmChoiceByResumeAndInterviewer(@Param("resumeId") Long resumeId,
                                            @Param("interviewerId") String interviewerId,
                                            @Param("confirmTime") Date confirmTime,
                                            @Param("interviewMethod") String interviewMethod,
                                            @Param("meetingNo") String meetingNo,
                                            @Param("availableStartTime") Date availableStartTime,
                                            @Param("availableEndTime") Date availableEndTime,
                                            @Param("updateUserId") String updateUserId,
                                            @Param("updateUserName") String updateUserName);

    int updateConfirmedInterviewTime(@Param("dispatchId") Long dispatchId,
                                     @Param("confirmedInterviewTime") Date confirmedInterviewTime,
                                     @Param("confirmUserId") String confirmUserId,
                                     @Param("confirmUserName") String confirmUserName,
                                     @Param("confirmTime") Date confirmTime);
}
