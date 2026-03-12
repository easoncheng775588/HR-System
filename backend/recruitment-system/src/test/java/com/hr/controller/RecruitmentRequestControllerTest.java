package com.hr.controller;

import com.hr.common.Response;
import com.hr.entity.RecruitmentRequest;
import com.hr.service.RecruitmentRequestService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RecruitmentRequestControllerTest {

    private final RecruitmentRequestService recruitmentRequestService = mock(RecruitmentRequestService.class);
    private final RecruitmentRequestController controller = buildController();

    @Test
    void submitAllowsAutoFilledStaffingFieldsAndResolvesUserFromAuthorization() {
        RecruitmentRequest request = validRequest();

        when(recruitmentRequestService.submitRequest(any(RecruitmentRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Response<RecruitmentRequest> response = controller.submitRequest(request, bearer("2001"));

        assertEquals("SUC0000", response.getReturnCode());
        assertNotNull(response.getBody());
        assertEquals("2001", response.getBody().getCreateUserId());
        assertNull(response.getBody().getTotalRecruitmentCount());
        verify(recruitmentRequestService).submitRequest(any(RecruitmentRequest.class));
    }

    @Test
    void updateAllowsAutoFilledStaffingFieldsAndResolvesUpdaterFromAuthorization() {
        RecruitmentRequest request = validRequest();

        when(recruitmentRequestService.updateRequest(any(RecruitmentRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Response<RecruitmentRequest> response = controller.update(1L, request, bearer("2001"));

        assertEquals("SUC0000", response.getReturnCode());
        assertNotNull(response.getBody());
        assertEquals("2001", response.getBody().getUpdateUserId());
        assertNull(response.getBody().getTotalRecruitmentCount());
        verify(recruitmentRequestService).updateRequest(any(RecruitmentRequest.class));
    }

    private RecruitmentRequestController buildController() {
        RecruitmentRequestController target = new RecruitmentRequestController();
        ReflectionTestUtils.setField(target, "recruitmentRequestService", recruitmentRequestService);
        return target;
    }

    private RecruitmentRequest validRequest() {
        RecruitmentRequest request = new RecruitmentRequest();
        request.setRequestTitle("CLI提交-用人需求优化");
        request.setRequestType("NEW_DEMAND");
        request.setTechnicalPlatform("开放");
        request.setCategory("系统研发岗");
        request.setSupplementCount(1);
        request.setUrgentRequirement("YES");
        request.setProposedLevel("PG");
        request.setExperienceYears("3-5年");
        request.setSkillRequirement("具备良好沟通与协作能力");
        request.setPositionResponsibility("负责系统研发与交付");
        request.setRemark("提交备注");
        request.setInterviewerId("1001");
        request.setInterviewerName("超级管理员");
        return request;
    }

    private String bearer(String userId) {
        String token = Base64.getEncoder()
            .encodeToString((userId + ":" + System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8));
        return "Bearer " + token;
    }
}
