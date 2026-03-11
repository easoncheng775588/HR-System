package com.hr.service.impl;

import com.hr.entity.RecruitmentRequest;
import com.hr.mapper.ApprovalHistoryMapper;
import com.hr.mapper.OrgUnitMapper;
import com.hr.mapper.RecruitmentRequestMapper;
import com.hr.mapper.UserMapper;
import com.hr.mapper.WorkflowProcessLogMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecruitmentRequestServiceImplTest {

    @Mock
    private RecruitmentRequestMapper recruitmentRequestMapper;

    @Mock
    private ApprovalHistoryMapper approvalHistoryMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private OrgUnitMapper orgUnitMapper;

    @Mock
    private WorkflowProcessLogMapper workflowProcessLogMapper;

    @InjectMocks
    private RecruitmentRequestServiceImpl recruitmentRequestService;

    @Captor
    private ArgumentCaptor<RecruitmentRequest> requestCaptor;

    @Test
    void submitRequestSetsDefaultPublishStatusBeforeInsert() {
        RecruitmentRequest request = new RecruitmentRequest();
        request.setRequestTitle("Java开发");
        request.setTotalRecruitmentCount(1);
        request.setVacancyCount(1);
        request.setTeam("平台研发");
        request.setTechnicalPlatform("Java");
        request.setCategory("社招");
        request.setSupplementCount(1);
        request.setUrgentRequirement("NO");
        request.setProposedLevel("P6");
        request.setExperienceYears("3年");
        request.setSkillRequirement("Spring Boot");
        request.setPositionResponsibility("负责后端开发");
        request.setCreateUserId("1001");
        request.setCreateUserName("系统用户");

        when(recruitmentRequestMapper.insert(any(RecruitmentRequest.class))).thenAnswer(invocation -> {
            RecruitmentRequest inserted = invocation.getArgument(0);
            inserted.setRecruitmentRequestId(1L);
            return 1;
        });

        recruitmentRequestService.submitRequest(request);

        verify(recruitmentRequestMapper).insert(requestCaptor.capture());
        assertEquals("NOT_PUBLISHED", requestCaptor.getValue().getPositionPublishStatus());
        assertEquals("PENDING", requestCaptor.getValue().getApprovalStatus());
    }

    @Test
    void updatePublishStatusNormalizesLegacyUnpublishedValue() {
        RecruitmentRequest existingRequest = new RecruitmentRequest();
        existingRequest.setRecruitmentRequestId(2L);
        existingRequest.setPositionPublishStatus("PUBLISHED");

        RecruitmentRequest updatedRequest = new RecruitmentRequest();
        updatedRequest.setRecruitmentRequestId(2L);
        updatedRequest.setPositionPublishStatus("NOT_PUBLISHED");

        when(recruitmentRequestMapper.selectByPrimaryKey(2L))
            .thenReturn(existingRequest)
            .thenReturn(updatedRequest);

        recruitmentRequestService.updatePublishStatus(2L, "UNPUBLISHED");

        verify(recruitmentRequestMapper).updateByPrimaryKey(requestCaptor.capture());
        assertEquals("NOT_PUBLISHED", requestCaptor.getValue().getPositionPublishStatus());
    }
}
