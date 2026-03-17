package com.hr.service.impl;

import com.hr.entity.ArrivalConfirmation;
import com.hr.entity.ArrivalConfirmationApprovalHistory;
import com.hr.entity.ArrivalConfirmationCandidateOption;
import com.hr.entity.ArrivalConfirmationDetailVO;
import com.hr.entity.ArrivalConfirmationFormOptionsVO;
import com.hr.entity.ArrivalConfirmationListItem;
import com.hr.entity.ArrivalConfirmationOrgUnitOption;
import com.hr.entity.ArrivalConfirmationStatusEnum;
import com.hr.entity.ArrivalConfirmationSupplierOption;
import com.hr.entity.ArrivalConfirmationUserOption;
import com.hr.entity.EntryRecord;
import com.hr.entity.OrgUnit;
import com.hr.entity.RecruitmentRequest;
import com.hr.entity.Resume;
import com.hr.entity.Supplier;
import com.hr.entity.SubmitArrivalConfirmationRequest;
import com.hr.entity.User;
import com.hr.entity.WorkflowApproveRequest;
import com.hr.mapper.ArrivalConfirmationApprovalHistoryMapper;
import com.hr.mapper.ArrivalConfirmationMapper;
import com.hr.mapper.EntryRecordMapper;
import com.hr.mapper.OrgUnitMapper;
import com.hr.mapper.RecruitmentRequestMapper;
import com.hr.mapper.ResumeMapper;
import com.hr.mapper.UserMapper;
import com.hr.mapper.WorkflowProcessLogMapper;
import com.hr.service.ArrivalConfirmationService;
import com.hr.service.InterviewPermissionService;
import com.hr.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class ArrivalConfirmationServiceImpl implements ArrivalConfirmationService {

    private static final String PROCESS_CODE = "ARRIVAL_CONFIRMATION";
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String HR_TEAM_NAME = "人力资源团队";
    private static final String NORMAL_ARRIVAL_STATUS = "正常入场";
    private static final String SYSTEM_USER_NAME = "系统用户";

    @Autowired
    private ArrivalConfirmationMapper arrivalConfirmationMapper;

    @Autowired
    private ArrivalConfirmationApprovalHistoryMapper arrivalConfirmationApprovalHistoryMapper;

    @Autowired
    private EntryRecordMapper entryRecordMapper;

    @Autowired
    private ResumeMapper resumeMapper;

    @Autowired
    private RecruitmentRequestMapper recruitmentRequestMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrgUnitMapper orgUnitMapper;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private InterviewPermissionService interviewPermissionService;

    @Autowired
    private WorkflowProcessLogMapper workflowProcessLogMapper;

    @Override
    @Transactional
    public ArrivalConfirmation submit(SubmitArrivalConfirmationRequest request) {
        if (request == null) {
            throw new RuntimeException("提交参数不能为空");
        }
        String operatorUserId = trimToNull(request.getOperatorUserId());
        if (operatorUserId == null) {
            throw new RuntimeException("操作人不能为空");
        }
        User operator = userMapper.getUserById(operatorUserId);
        if (operator == null) {
            throw new RuntimeException("操作人不存在");
        }
        Set<String> roles = interviewPermissionService.normalizeRoles(operator, request.getOperatorUserRole());
        ensureSubmitPermission(operatorUserId, roles);

        Long entryRecordId = request.getEntryRecordId();
        if (entryRecordId == null) {
            throw new RuntimeException("到岗人员不能为空");
        }
        EntryRecord entryRecord = entryRecordMapper.selectByPrimaryKey(entryRecordId);
        if (entryRecord == null) {
            throw new RuntimeException("到岗人员不存在");
        }

        ArrivalConfirmation latest = arrivalConfirmationMapper.selectLatestByEntryRecordId(entryRecordId);
        if (latest != null && !ArrivalConfirmationStatusEnum.REJECTED.name().equals(latest.getApprovalStatus())) {
            throw new RuntimeException("该到岗人员已有进行中的到岗确认流程");
        }

        Resume resume = entryRecord.getResumeId() == null ? null : resumeMapper.selectByPrimaryKey(entryRecord.getResumeId());
        if (resume == null) {
            throw new RuntimeException("候选人简历不存在");
        }

        Supplier supplier = request.getSupplierId() == null ? null : supplierService.getSupplierById(request.getSupplierId());
        if (supplier == null) {
            throw new RuntimeException("所属外包供应商不能为空");
        }

        String actualSupplierHrUserId = trimToNull(resume.getCreateUserId());
        if (actualSupplierHrUserId == null) {
            throw new RuntimeException("候选人简历缺少供应商HR信息");
        }
        if (!actualSupplierHrUserId.equals(trimToNull(request.getSupplierHrUserId()))) {
            throw new RuntimeException("供应商HR必须为该候选人简历创建人");
        }
        if (supplier.getUserIds() == null || !supplier.getUserIds().contains(actualSupplierHrUserId)) {
            throw new RuntimeException("所选供应商与候选人供应商HR不匹配");
        }
        User supplierHr = userMapper.getUserById(actualSupplierHrUserId);
        if (supplierHr == null) {
            throw new RuntimeException("供应商HR不存在");
        }

        String targetOrgUnitName = trimToNull(request.getTargetOrgUnitName());
        if (targetOrgUnitName == null) {
            throw new RuntimeException("用人团队/部室不能为空");
        }
        OrgUnit orgUnit = orgUnitMapper.getByUnitName(targetOrgUnitName);
        if (orgUnit == null) {
            throw new RuntimeException("用人团队/部室不存在");
        }

        String roomManagerUserId = trimToNull(request.getRoomManagerUserId());
        if (roomManagerUserId == null) {
            throw new RuntimeException("所属室经理不能为空");
        }
        User roomManager = userMapper.getUserById(roomManagerUserId);
        if (roomManager == null) {
            throw new RuntimeException("所属室经理不存在");
        }
        Set<String> roomManagerRoles = interviewPermissionService.normalizeRoles(roomManager, InterviewPermissionService.ROLE_ROOM_MANAGER);
        if (!interviewPermissionService.hasRole(roomManagerRoles, InterviewPermissionService.ROLE_ROOM_MANAGER)
            && !interviewPermissionService.isSuperAdmin(roomManagerUserId, roomManagerRoles)) {
            throw new RuntimeException("所选人员不是室经理");
        }

        User hrTeamManager = resolveHrTeamManager();
        if (hrTeamManager == null) {
            throw new RuntimeException("人力资源团队经理不存在");
        }

        String positionLevel = trimToNull(request.getPositionLevel());
        if (positionLevel == null) {
            throw new RuntimeException("人员级别不能为空");
        }

        Date entryDate = parseDate(request.getEntryDate());
        if (entryDate == null) {
            throw new RuntimeException("人员进场日期不能为空");
        }

        Date now = new Date();
        ArrivalConfirmation target = new ArrivalConfirmation();
        target.setEntryRecordId(entryRecordId);
        target.setResumeId(entryRecord.getResumeId());
        target.setSourceRecruitmentRequestId(entryRecord.getSourceRecruitmentRequestId());
        target.setCandidateName(defaultValue(entryRecord.getCandidateName(), resume.getCandidateName(), resume.getApplicantName(), "候选人"));
        target.setSupplierId(supplier.getSupplierId());
        target.setSupplierName(defaultValue(supplier.getSupplierName(), resume.getSupplierName(), ""));
        target.setSupplierHrUserId(supplierHr.getUserId());
        target.setSupplierHrUserName(defaultValue(supplierHr.getRealName(), resume.getCreateUserName(), ""));
        target.setTargetOrgUnitName(targetOrgUnitName);
        target.setRoomManagerUserId(roomManager.getUserId());
        target.setRoomManagerUserName(defaultValue(roomManager.getRealName(), ""));
        target.setHrTeamManagerUserId(hrTeamManager.getUserId());
        target.setHrTeamManagerUserName(defaultValue(hrTeamManager.getRealName(), ""));
        target.setEntryDate(entryDate);
        target.setPositionLevel(positionLevel);
        target.setApprovalStatus(ArrivalConfirmationStatusEnum.PENDING_SUPPLIER_HR.name());
        target.setCurrentApprovalLevel(1);
        target.setCreateTime(now);
        target.setCreateUserId(operatorUserId);
        target.setCreateUserName(defaultValue(request.getOperatorUserName(), operator.getRealName(), SYSTEM_USER_NAME));
        target.setUpdateTime(now);
        target.setUpdateUserId(operatorUserId);
        target.setUpdateUserName(target.getCreateUserName());
        arrivalConfirmationMapper.insert(target);

        logProcess(target.getArrivalConfirmationId(), 1, "外包招聘管理岗发起", "SUBMIT", "SUCCESS", operatorUserId, target.getCreateUserName(), request.getOperatorUserRole(), "发起到岗确认");
        return arrivalConfirmationMapper.selectByPrimaryKey(target.getArrivalConfirmationId());
    }

    @Override
    public List<ArrivalConfirmationListItem> getList(String viewerId, String viewerRole) {
        if (trimToNull(viewerId) == null) {
            throw new RuntimeException("查看人不能为空");
        }
        User viewer = userMapper.getUserById(viewerId);
        if (viewer == null) {
            throw new RuntimeException("查看人不存在");
        }
        Set<String> roles = interviewPermissionService.normalizeRoles(viewer, viewerRole);
        ensureListAccess(viewerId, roles);

        List<ArrivalConfirmationListItem> result = new ArrayList<>();
        for (ArrivalConfirmation confirmation : arrivalConfirmationMapper.selectAll()) {
            if (!canViewConfirmation(confirmation, viewer, viewerId, roles)) {
                continue;
            }
            ArrivalConfirmationListItem item = new ArrivalConfirmationListItem();
            item.setArrivalConfirmationId(confirmation.getArrivalConfirmationId());
            item.setCandidateName(confirmation.getCandidateName());
            item.setSupplierName(confirmation.getSupplierName());
            item.setSupplierHrUserName(confirmation.getSupplierHrUserName());
            item.setTargetOrgUnitName(confirmation.getTargetOrgUnitName());
            item.setRoomManagerUserName(confirmation.getRoomManagerUserName());
            item.setEntryDate(confirmation.getEntryDate());
            item.setPositionLevel(confirmation.getPositionLevel());
            item.setCreateUserName(confirmation.getCreateUserName());
            item.setCreateTime(confirmation.getCreateTime());
            item.setApprovalStatus(confirmation.getApprovalStatus());
            item.setCurrentApprovalNode(resolveCurrentNode(confirmation));
            item.setCanDelete(canDelete(confirmation, viewerId, roles));
            result.add(item);
        }
        return result;
    }

    @Override
    public ArrivalConfirmationFormOptionsVO getFormOptions(String operatorUserId, String operatorUserRole) {
        if (trimToNull(operatorUserId) == null) {
            throw new RuntimeException("操作人不能为空");
        }
        User operator = userMapper.getUserById(operatorUserId);
        if (operator == null) {
            throw new RuntimeException("操作人不存在");
        }
        Set<String> roles = interviewPermissionService.normalizeRoles(operator, operatorUserRole);
        ensureSubmitPermission(operatorUserId, roles);

        ArrivalConfirmationFormOptionsVO vo = new ArrivalConfirmationFormOptionsVO();
        vo.setCandidates(buildCandidateOptions());
        vo.setSuppliers(buildSupplierOptions());
        vo.setSupplierHrs(buildUserOptions(userMapper.getActiveUsersByRoleKeyword(InterviewPermissionService.ROLE_SUPPLIER_HR)));
        vo.setRoomManagers(buildUserOptions(userMapper.getActiveUsersByRoleKeyword(InterviewPermissionService.ROLE_ROOM_MANAGER)));
        vo.setOrgUnits(buildOrgUnitOptions(orgUnitMapper.getActiveOrgUnits()));
        return vo;
    }

    @Override
    public ArrivalConfirmationDetailVO getDetail(Long arrivalConfirmationId) {
        ArrivalConfirmation confirmation = arrivalConfirmationMapper.selectByPrimaryKey(arrivalConfirmationId);
        if (confirmation == null) {
            throw new RuntimeException("到岗确认不存在");
        }
        ArrivalConfirmationDetailVO detailVO = new ArrivalConfirmationDetailVO();
        detailVO.setArrivalConfirmation(confirmation);
        detailVO.setApprovalHistory(arrivalConfirmationApprovalHistoryMapper.selectByArrivalConfirmationId(arrivalConfirmationId));
        return detailVO;
    }

    @Override
    @Transactional
    public void approve(Long arrivalConfirmationId, WorkflowApproveRequest request) {
        ArrivalConfirmation confirmation = arrivalConfirmationMapper.selectByPrimaryKey(arrivalConfirmationId);
        if (confirmation == null) {
            throw new RuntimeException("到岗确认不存在");
        }
        if (ArrivalConfirmationStatusEnum.APPROVED.name().equals(confirmation.getApprovalStatus())) {
            throw new RuntimeException("流程已结束，不能重复审批");
        }
        if (ArrivalConfirmationStatusEnum.REJECTED.name().equals(confirmation.getApprovalStatus())) {
            throw new RuntimeException("流程已拒绝，不能重复审批");
        }

        String operatorUserId = trimToNull(request.getApprovalUserId());
        if (operatorUserId == null) {
            throw new RuntimeException("审批人不能为空");
        }
        User operator = userMapper.getUserById(operatorUserId);
        if (operator == null) {
            throw new RuntimeException("审批人不存在");
        }
        Set<String> roles = interviewPermissionService.normalizeRoles(operator, request.getApprovalUserRole());
        ensureApprovalPermission(confirmation, operatorUserId, roles);

        String action = defaultValue(request.getAction(), "").toUpperCase(Locale.ROOT);
        if (!"APPROVE".equals(action) && !"REJECT".equals(action)) {
            throw new RuntimeException("不支持的审批动作");
        }

        Date now = new Date();
        String operatorName = defaultValue(request.getApprovalUserName(), operator.getRealName(), SYSTEM_USER_NAME);
        String operatorRole = defaultValue(request.getApprovalUserRole(), "");
        String approvalComment = defaultValue(request.getApprovalComment(), "");
        Integer approvalLevel = confirmation.getCurrentApprovalLevel() == null ? 1 : confirmation.getCurrentApprovalLevel();
        insertApprovalHistory(confirmation.getArrivalConfirmationId(), approvalLevel, operatorUserId, operatorName, operatorRole, action, approvalComment, now);

        if ("APPROVE".equals(action)) {
            if (ArrivalConfirmationStatusEnum.PENDING_SUPPLIER_HR.name().equals(confirmation.getApprovalStatus())) {
                confirmation.setApprovalStatus(ArrivalConfirmationStatusEnum.PENDING_HR_TEAM_MANAGER.name());
                confirmation.setCurrentApprovalLevel(2);
                logProcess(confirmation.getArrivalConfirmationId(), 1, "供应商HR审批", "APPROVE", "SUCCESS", operatorUserId, operatorName, operatorRole, approvalComment);
                logProcess(confirmation.getArrivalConfirmationId(), 2, "流转人力资源团队经理审批", "TRANSFER", "SUCCESS", operatorUserId, operatorName, operatorRole, "流转到人力资源团队经理审批");
            } else if (ArrivalConfirmationStatusEnum.PENDING_HR_TEAM_MANAGER.name().equals(confirmation.getApprovalStatus())) {
                confirmation.setApprovalStatus(ArrivalConfirmationStatusEnum.PENDING_ROOM_MANAGER.name());
                confirmation.setCurrentApprovalLevel(3);
                logProcess(confirmation.getArrivalConfirmationId(), 2, "人力资源团队经理审批", "APPROVE", "SUCCESS", operatorUserId, operatorName, operatorRole, approvalComment);
                logProcess(confirmation.getArrivalConfirmationId(), 3, "流转用人室经理审批", "TRANSFER", "SUCCESS", operatorUserId, operatorName, operatorRole, "流转到用人室经理审批");
            } else if (ArrivalConfirmationStatusEnum.PENDING_ROOM_MANAGER.name().equals(confirmation.getApprovalStatus())) {
                confirmation.setApprovalStatus(ArrivalConfirmationStatusEnum.APPROVED.name());
                confirmation.setCurrentApprovalLevel(4);
                logProcess(confirmation.getArrivalConfirmationId(), 3, "用人室经理审批", "APPROVE", "SUCCESS", operatorUserId, operatorName, operatorRole, approvalComment);
                logProcess(confirmation.getArrivalConfirmationId(), 4, "流程结束", "FINISH", "SUCCESS", operatorUserId, operatorName, operatorRole, "到岗确认审批完成");
                backwriteEntryRecord(confirmation, operatorUserId, operatorName);
            } else {
                throw new RuntimeException("当前流程状态不支持审批");
            }
        } else {
            confirmation.setApprovalStatus(ArrivalConfirmationStatusEnum.REJECTED.name());
            logProcess(confirmation.getArrivalConfirmationId(), approvalLevel, "审批驳回", "REJECT", "REJECTED", operatorUserId, operatorName, operatorRole, approvalComment);
        }

        confirmation.setUpdateTime(now);
        confirmation.setUpdateUserId(operatorUserId);
        confirmation.setUpdateUserName(operatorName);
        arrivalConfirmationMapper.updateByPrimaryKey(confirmation);
    }

    @Override
    @Transactional
    public void delete(Long arrivalConfirmationId, String operatorUserId, String operatorUserRole) {
        if (arrivalConfirmationId == null) {
            throw new RuntimeException("到岗确认ID不能为空");
        }
        String actualOperatorUserId = trimToNull(operatorUserId);
        if (actualOperatorUserId == null) {
            throw new RuntimeException("操作人不能为空");
        }
        User operator = userMapper.getUserById(actualOperatorUserId);
        if (operator == null) {
            throw new RuntimeException("操作人不存在");
        }
        Set<String> roles = interviewPermissionService.normalizeRoles(operator, operatorUserRole);
        ArrivalConfirmation confirmation = arrivalConfirmationMapper.selectByPrimaryKey(arrivalConfirmationId);
        if (confirmation == null) {
            throw new RuntimeException("到岗确认不存在");
        }
        if (!ArrivalConfirmationStatusEnum.REJECTED.name().equals(confirmation.getApprovalStatus())) {
            throw new RuntimeException("仅已拒绝的流程允许删除");
        }
        if (!interviewPermissionService.isSuperAdmin(actualOperatorUserId, roles)
            && !actualOperatorUserId.equals(trimToNull(confirmation.getCreateUserId()))) {
            throw new RuntimeException("无权限删除该到岗确认");
        }
        arrivalConfirmationMapper.deleteByPrimaryKey(arrivalConfirmationId);
        logProcess(arrivalConfirmationId, confirmation.getCurrentApprovalLevel(), "删除流程", "DELETE", "SUCCESS", actualOperatorUserId, defaultValue(operator.getRealName(), SYSTEM_USER_NAME), operatorUserRole, "删除已拒绝的到岗确认");
    }

    private void ensureSubmitPermission(String operatorUserId, Set<String> roles) {
        if (interviewPermissionService.isSuperAdmin(operatorUserId, roles)) {
            return;
        }
        if (interviewPermissionService.hasRole(roles, InterviewPermissionService.ROLE_OUTSOURCING_MANAGER)) {
            return;
        }
        throw new RuntimeException("仅外包招聘管理岗可发起到岗确认");
    }

    private void ensureListAccess(String viewerId, Set<String> roles) {
        if (interviewPermissionService.isSuperAdmin(viewerId, roles)
            || interviewPermissionService.hasRole(roles, InterviewPermissionService.ROLE_OUTSOURCING_MANAGER)
            || interviewPermissionService.hasRole(roles, InterviewPermissionService.ROLE_ROOM_MANAGER)
            || interviewPermissionService.hasRole(roles, InterviewPermissionService.ROLE_TEAM_MANAGER)
            || interviewPermissionService.hasRole(roles, InterviewPermissionService.ROLE_SUPPLIER_HR)) {
            return;
        }
        throw new RuntimeException("无权限访问到岗确认");
    }

    private boolean canViewConfirmation(ArrivalConfirmation confirmation, User viewer, String viewerId, Set<String> roles) {
        if (interviewPermissionService.isSuperAdmin(viewerId, roles)
            || interviewPermissionService.hasRole(roles, InterviewPermissionService.ROLE_OUTSOURCING_MANAGER)) {
            return true;
        }
        if (interviewPermissionService.hasRole(roles, InterviewPermissionService.ROLE_SUPPLIER_HR)) {
            return viewerId.equals(trimToNull(confirmation.getSupplierHrUserId()));
        }

        RecruitmentRequest request = confirmation.getSourceRecruitmentRequestId() == null
            ? null
            : recruitmentRequestMapper.selectByPrimaryKey(confirmation.getSourceRecruitmentRequestId());
        if (request == null) {
            return false;
        }
        if (interviewPermissionService.hasRole(roles, InterviewPermissionService.ROLE_ROOM_MANAGER)) {
            return viewerId.equals(trimToNull(request.getCreateUserId()));
        }
        if (interviewPermissionService.hasRole(roles, InterviewPermissionService.ROLE_TEAM_MANAGER)) {
            if (viewerId.equals(trimToNull(request.getCreateUserId()))) {
                return true;
            }
            String requestTeam = resolveRequestTeam(request);
            String viewerTeam = resolveUserTeam(viewer);
            return requestTeam != null && requestTeam.equals(viewerTeam);
        }
        return false;
    }

    private boolean canDelete(ArrivalConfirmation confirmation, String viewerId, Set<String> roles) {
        if (!ArrivalConfirmationStatusEnum.REJECTED.name().equals(confirmation.getApprovalStatus())) {
            return false;
        }
        if (interviewPermissionService.isSuperAdmin(viewerId, roles)) {
            return true;
        }
        return viewerId.equals(trimToNull(confirmation.getCreateUserId()));
    }

    private void ensureApprovalPermission(ArrivalConfirmation confirmation, String operatorUserId, Set<String> roles) {
        if (interviewPermissionService.isSuperAdmin(operatorUserId, roles)) {
            return;
        }
        String status = defaultValue(confirmation.getApprovalStatus(), "");
        if (ArrivalConfirmationStatusEnum.PENDING_SUPPLIER_HR.name().equals(status)) {
            if (operatorUserId.equals(trimToNull(confirmation.getSupplierHrUserId()))) {
                return;
            }
            throw new RuntimeException("仅供应商HR可审批当前环节");
        }
        if (ArrivalConfirmationStatusEnum.PENDING_HR_TEAM_MANAGER.name().equals(status)) {
            if (operatorUserId.equals(trimToNull(confirmation.getHrTeamManagerUserId()))) {
                return;
            }
            throw new RuntimeException("仅人力资源团队经理可审批当前环节");
        }
        if (ArrivalConfirmationStatusEnum.PENDING_ROOM_MANAGER.name().equals(status)) {
            if (operatorUserId.equals(trimToNull(confirmation.getRoomManagerUserId()))) {
                return;
            }
            throw new RuntimeException("仅用人室经理可审批当前环节");
        }
        throw new RuntimeException("当前流程状态不支持审批");
    }

    private void insertApprovalHistory(Long arrivalConfirmationId,
                                       Integer approvalLevel,
                                       String approverId,
                                       String approverName,
                                       String approverRole,
                                       String action,
                                       String comment,
                                       Date approvalTime) {
        ArrivalConfirmationApprovalHistory history = new ArrivalConfirmationApprovalHistory();
        history.setArrivalConfirmationId(arrivalConfirmationId);
        history.setApprovalLevel(approvalLevel);
        history.setApproverId(approverId);
        history.setApproverName(approverName);
        history.setApproverRole(approverRole);
        history.setAction(action);
        history.setComment(comment);
        history.setApprovalTime(approvalTime);
        arrivalConfirmationApprovalHistoryMapper.insert(history);
    }

    private void backwriteEntryRecord(ArrivalConfirmation confirmation, String operatorUserId, String operatorUserName) {
        EntryRecord entryRecord = confirmation.getEntryRecordId() == null ? null : entryRecordMapper.selectByPrimaryKey(confirmation.getEntryRecordId());
        if (entryRecord == null) {
            throw new RuntimeException("关联入场记录不存在");
        }
        entryRecord.setActualEntryDate(confirmation.getEntryDate());
        entryRecord.setArrivalStatus(NORMAL_ARRIVAL_STATUS);
        entryRecord.setUpdateTime(new Date());
        entryRecord.setUpdateUserId(operatorUserId);
        entryRecord.setUpdateUserName(operatorUserName);
        entryRecordMapper.updateByPrimaryKey(entryRecord);
    }

    private List<ArrivalConfirmationCandidateOption> buildCandidateOptions() {
        List<EntryRecord> entryRecords = entryRecordMapper.selectAll();
        if (entryRecords == null || entryRecords.isEmpty()) {
            return new ArrayList<>();
        }
        List<Supplier> suppliers = supplierService.getAllSuppliers();
        Map<String, Supplier> supplierByName = new HashMap<>();
        for (Supplier supplier : suppliers) {
            if (trimToNull(supplier.getSupplierName()) != null) {
                supplierByName.put(trimToNull(supplier.getSupplierName()), supplier);
            }
        }

        List<ArrivalConfirmationCandidateOption> options = new ArrayList<>();
        for (EntryRecord entryRecord : entryRecords) {
            if (entryRecord == null || entryRecord.getResumeId() == null) {
                continue;
            }
            Resume resume = resumeMapper.selectByPrimaryKey(entryRecord.getResumeId());
            if (resume == null) {
                continue;
            }
            Supplier supplier = supplierByName.get(trimToNull(resume.getSupplierName()));
            User supplierHr = trimToNull(resume.getCreateUserId()) == null ? null : userMapper.getUserById(resume.getCreateUserId());
            User recommendedRoomManager = userMapper.getActiveRoomManagerByDepartmentAndRoleKeyword(resolveLeafDepartment(entryRecord.getHiredDepartment()), InterviewPermissionService.ROLE_ROOM_MANAGER);

            ArrivalConfirmationCandidateOption option = new ArrivalConfirmationCandidateOption();
            option.setEntryRecordId(entryRecord.getEntryRecordId());
            option.setResumeId(entryRecord.getResumeId());
            option.setSourceRecruitmentRequestId(entryRecord.getSourceRecruitmentRequestId());
            option.setCandidateName(defaultValue(entryRecord.getCandidateName(), resume.getCandidateName(), resume.getApplicantName(), "候选人"));
            option.setSupplierId(supplier == null ? null : supplier.getSupplierId());
            option.setSupplierName(defaultValue(resume.getSupplierName(), supplier == null ? null : supplier.getSupplierName(), ""));
            option.setSupplierHrUserId(defaultValue(resume.getCreateUserId(), supplierHr == null ? null : supplierHr.getUserId(), ""));
            option.setSupplierHrUserName(defaultValue(resume.getCreateUserName(), supplierHr == null ? null : supplierHr.getRealName(), ""));
            option.setTargetOrgUnitName(defaultValue(resolveLeafDepartment(entryRecord.getHiredDepartment()), ""));
            option.setRecommendedRoomManagerUserId(recommendedRoomManager == null ? null : recommendedRoomManager.getUserId());
            option.setRecommendedRoomManagerUserName(recommendedRoomManager == null ? null : recommendedRoomManager.getRealName());
            options.add(option);
        }
        return options;
    }

    private List<ArrivalConfirmationSupplierOption> buildSupplierOptions() {
        List<ArrivalConfirmationSupplierOption> options = new ArrayList<>();
        for (Supplier supplier : supplierService.getAllSuppliers()) {
            if (supplier == null || !"ACTIVE".equals(defaultValue(supplier.getStatus(), "ACTIVE"))) {
                continue;
            }
            ArrivalConfirmationSupplierOption option = new ArrivalConfirmationSupplierOption();
            option.setSupplierId(supplier.getSupplierId());
            option.setSupplierName(supplier.getSupplierName());
            options.add(option);
        }
        return options;
    }

    private List<ArrivalConfirmationUserOption> buildUserOptions(List<User> users) {
        Map<String, ArrivalConfirmationUserOption> unique = new LinkedHashMap<>();
        if (users == null) {
            return new ArrayList<>();
        }
        for (User user : users) {
            if (user == null || trimToNull(user.getUserId()) == null) {
                continue;
            }
            ArrivalConfirmationUserOption option = new ArrivalConfirmationUserOption();
            option.setUserId(user.getUserId());
            option.setRealName(defaultValue(user.getRealName(), ""));
            option.setDepartment(defaultValue(user.getDepartment(), user.getTeamName(), ""));
            unique.put(user.getUserId(), option);
        }
        return new ArrayList<>(unique.values());
    }

    private List<ArrivalConfirmationOrgUnitOption> buildOrgUnitOptions(List<OrgUnit> units) {
        List<ArrivalConfirmationOrgUnitOption> options = new ArrayList<>();
        for (OrgUnit unit : units) {
            if (unit == null) {
                continue;
            }
            ArrivalConfirmationOrgUnitOption option = new ArrivalConfirmationOrgUnitOption();
            option.setUnitName(unit.getUnitName());
            option.setParentUnitName(unit.getParentUnitName());
            options.add(option);
        }
        return options;
    }

    private User resolveHrTeamManager() {
        for (User user : userMapper.getActiveUsersByRoleKeyword(InterviewPermissionService.ROLE_TEAM_MANAGER)) {
            if (user == null) {
                continue;
            }
            String team = resolveUserTeam(user);
            if (HR_TEAM_NAME.equals(team) || HR_TEAM_NAME.equals(trimToNull(user.getDepartment()))) {
                return user;
            }
        }
        return null;
    }

    private String resolveCurrentNode(ArrivalConfirmation confirmation) {
        String status = defaultValue(confirmation.getApprovalStatus(), "");
        if (ArrivalConfirmationStatusEnum.PENDING_SUPPLIER_HR.name().equals(status)) {
            return "供应商HR审批";
        }
        if (ArrivalConfirmationStatusEnum.PENDING_HR_TEAM_MANAGER.name().equals(status)) {
            return "人力资源团队经理审批";
        }
        if (ArrivalConfirmationStatusEnum.PENDING_ROOM_MANAGER.name().equals(status)) {
            return "用人室经理审批";
        }
        if (ArrivalConfirmationStatusEnum.REJECTED.name().equals(status)) {
            return "已拒绝";
        }
        if (ArrivalConfirmationStatusEnum.APPROVED.name().equals(status)) {
            return "流程结束";
        }
        return "-";
    }

    private String resolveRequestTeam(RecruitmentRequest request) {
        String team = trimToNull(request.getTeam());
        if (team != null) {
            return team.contains("/") ? trimToNull(team.split("/")[0]) : team;
        }
        String orgUnit = trimToNull(request.getOrgUnitName());
        if (orgUnit != null) {
            return resolveTeamName(orgUnit);
        }
        String applicationDepartment = trimToNull(request.getApplicationDepartment());
        if (applicationDepartment != null) {
            return resolveTeamName(applicationDepartment);
        }
        return null;
    }

    private String resolveUserTeam(User user) {
        String team = trimToNull(user.getTeamName());
        if (team != null) {
            return team;
        }
        return resolveTeamName(user.getDepartment());
    }

    private String resolveTeamName(String departmentOrUnit) {
        String value = trimToNull(departmentOrUnit);
        if (value == null) {
            return null;
        }
        if (value.contains("/")) {
            String[] parts = value.split("/");
            if (parts.length > 0) {
                String first = trimToNull(parts[0]);
                if (first != null) {
                    return first;
                }
            }
        }
        OrgUnit unit = orgUnitMapper.getByUnitName(value);
        if (unit == null) {
            return value;
        }
        String parent = trimToNull(unit.getParentUnitName());
        return parent == null ? value : parent;
    }

    private String resolveLeafDepartment(String departmentOrUnit) {
        String value = trimToNull(departmentOrUnit);
        if (value == null) {
            return null;
        }
        if (value.contains("/")) {
            String[] parts = value.split("/");
            return trimToNull(parts[parts.length - 1]);
        }
        return value;
    }

    private Date parseDate(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        try {
            return new SimpleDateFormat(DATE_PATTERN, Locale.ROOT).parse(trimmed);
        } catch (ParseException e) {
            throw new RuntimeException("人员进场日期格式不正确");
        }
    }

    private void logProcess(Long businessId,
                            Integer currentLevel,
                            String currentNode,
                            String actionType,
                            String actionResult,
                            String operatorId,
                            String operatorName,
                            String operatorRole,
                            String comment) {
        com.hr.entity.WorkflowProcessLog log = new com.hr.entity.WorkflowProcessLog();
        log.setProcessCode(PROCESS_CODE);
        log.setBusinessId(businessId);
        log.setNodeOrder(currentLevel);
        log.setNodeName(currentNode);
        log.setActionType(actionType);
        log.setActionResult(actionResult);
        log.setOperatorId(defaultValue(operatorId, ""));
        log.setOperatorName(defaultValue(operatorName, SYSTEM_USER_NAME));
        log.setOperatorRole(defaultValue(operatorRole, ""));
        log.setActionComment(defaultValue(comment, ""));
        log.setActionTime(new Date());
        workflowProcessLogMapper.insert(log);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String defaultValue(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            String trimmed = trimToNull(value);
            if (trimmed != null) {
                return trimmed;
            }
        }
        return "";
    }
}
