package com.hr.service;

import com.hr.entity.ArrivalConfirmation;
import com.hr.entity.ArrivalConfirmationDetailVO;
import com.hr.entity.ArrivalConfirmationFormOptionsVO;
import com.hr.entity.ArrivalConfirmationListItem;
import com.hr.entity.SubmitArrivalConfirmationRequest;
import com.hr.entity.WorkflowApproveRequest;

import java.util.List;

public interface ArrivalConfirmationService {
    ArrivalConfirmation submit(SubmitArrivalConfirmationRequest request);

    List<ArrivalConfirmationListItem> getList(String viewerId, String viewerRole);

    ArrivalConfirmationFormOptionsVO getFormOptions(String operatorUserId, String operatorUserRole);

    ArrivalConfirmationDetailVO getDetail(Long arrivalConfirmationId);

    void approve(Long arrivalConfirmationId, WorkflowApproveRequest request);

    void delete(Long arrivalConfirmationId, String operatorUserId, String operatorUserRole);
}
