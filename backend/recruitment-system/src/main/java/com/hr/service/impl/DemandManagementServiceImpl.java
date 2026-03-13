package com.hr.service.impl;

import com.hr.entity.DemandDispatch;
import com.hr.entity.DemandOperationLog;
import com.hr.entity.DemandRequirement;
import com.hr.entity.Message;
import com.hr.entity.RecruitmentRequest;
import com.hr.entity.Supplier;
import com.hr.entity.User;
import com.hr.mapper.DemandDispatchMapper;
import com.hr.mapper.DemandOperationLogMapper;
import com.hr.mapper.DemandRequirementMapper;
import com.hr.mapper.SupplierMapper;
import com.hr.mapper.UserMapper;
import com.hr.service.DemandManagementService;
import com.hr.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DemandManagementServiceImpl implements DemandManagementService {

    private static final String SYSTEM_USER_ID = "1001";
    private static final String SYSTEM_USER_NAME = "系统用户";
    private static final String ROLE_OUTSOURCING_MANAGER = "外包招聘管理岗";
    private static final String ROLE_SUPPLIER_HR = "供应商HR";
    private static final String ROLE_SUPER_ADMIN = "超级管理员";

    @Autowired
    private DemandRequirementMapper demandRequirementMapper;

    @Autowired
    private DemandDispatchMapper demandDispatchMapper;

    @Autowired
    private DemandOperationLogMapper demandOperationLogMapper;

    @Autowired
    private SupplierMapper supplierMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MessageService messageService;

    @Override
    @Transactional
    public void createFromApprovedRecruitment(RecruitmentRequest request) {
        if (request == null || request.getRecruitmentRequestId() == null) {
            return;
        }
        DemandRequirement existed = demandRequirementMapper.selectBySourceRecruitmentRequestId(request.getRecruitmentRequestId());
        if (existed != null) {
            return;
        }

        Date now = new Date();
        DemandRequirement demand = new DemandRequirement();
        demand.setSourceRecruitmentRequestId(request.getRecruitmentRequestId());
        demand.setPositionOrgName(buildPositionOrgName(request));
        demand.setVacancyCount(request.getVacancyCount());
        demand.setTechnicalPlatform(request.getTechnicalPlatform());
        demand.setRecruitLevel(request.getProposedLevel());
        demand.setRecruitCount(request.getSupplementCount());
        demand.setPositionResponsibility(request.getPositionResponsibility());
        demand.setRecruitRequirement(request.getSkillRequirement());
        demand.setAcceptanceStatus("未接收");
        demand.setDispatchSupplierCount(0);
        demand.setReceivedSupplierCount(0);
        demand.setDemandStatus("待分发");
        demand.setCreateTime(now);
        demand.setCreateUserId(defaultValue(request.getUpdateUserId(), request.getCreateUserId(), SYSTEM_USER_ID));
        demand.setCreateUserName(defaultValue(request.getUpdateUserName(), request.getCreateUserName(), SYSTEM_USER_NAME));
        demand.setUpdateTime(now);
        demand.setUpdateUserId(demand.getCreateUserId());
        demand.setUpdateUserName(demand.getCreateUserName());
        demandRequirementMapper.insert(demand);

        insertLog(demand.getDemandId(), "AUTO_CREATE", "审批通过后自动生成需求", demand.getCreateUserId(), demand.getCreateUserName());
    }

    @Override
    public List<Map<String, Object>> getDemandList(String viewerId, String viewerRole) {
        User viewer = viewerId == null || viewerId.trim().isEmpty() ? null : userMapper.getUserById(viewerId);
        Set<String> roles = normalizeRoles(viewer, viewerRole);
        ensureDemandListAccess(viewerId, roles);

        List<DemandRequirement> demands;
        if (roles.contains(ROLE_SUPPLIER_HR) && !isSuperAdmin(viewerId, roles) && !roles.contains(ROLE_OUTSOURCING_MANAGER)) {
            demands = demandRequirementMapper.selectByHrUserId(viewerId);
        } else {
            demands = demandRequirementMapper.selectAll();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (DemandRequirement demand : demands) {
            result.add(buildDemandView(demand, viewerId));
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getSupplierOptions(String viewerId, String viewerRole) {
        User viewer = viewerId == null || viewerId.trim().isEmpty() ? null : userMapper.getUserById(viewerId);
        Set<String> roles = normalizeRoles(viewer, viewerRole);
        ensureSupplierOptionsAccess(viewerId, roles);

        List<Supplier> suppliers = supplierMapper.getAllSuppliers();
        List<Map<String, Object>> options = new ArrayList<>();
        for (Supplier supplier : suppliers) {
            if (!"ACTIVE".equalsIgnoreCase(supplier.getStatus())) {
                continue;
            }
            Map<String, Object> option = new LinkedHashMap<>();
            option.put("supplierId", supplier.getSupplierId());
            option.put("supplierName", supplier.getSupplierName());
            option.put("hrCount", supplier.getHrCount());
            options.add(option);
        }
        return options;
    }

    @Override
    @Transactional
    public void dispatchToSuppliers(Long demandId, List<Long> supplierIds, String operatorUserId, String operatorUserName) {
        User operator = userMapper.getUserById(operatorUserId);
        Set<String> roles = normalizeRoles(operator, null);
        ensureDispatchAccess(operatorUserId, roles);

        DemandRequirement demand = demandRequirementMapper.selectByPrimaryKey(demandId);
        if (demand == null) {
            throw new RuntimeException("需求不存在");
        }
        if (supplierIds == null || supplierIds.isEmpty()) {
            throw new RuntimeException("请选择供应商");
        }

        Date now = new Date();
        for (Long supplierId : new LinkedHashSet<>(supplierIds)) {
            Supplier supplier = supplierMapper.getSupplierById(supplierId);
            if (supplier == null) {
                throw new RuntimeException("供应商不存在: " + supplierId);
            }
            if (!"ACTIVE".equalsIgnoreCase(supplier.getStatus())) {
                throw new RuntimeException("供应商未启用: " + supplier.getSupplierName());
            }
            List<String> userIds = supplierMapper.getUserIdsBySupplierId(supplierId);
            if (userIds == null || userIds.isEmpty()) {
                throw new RuntimeException("供应商【" + supplier.getSupplierName() + "】未配置HR用户");
            }

            List<User> activeHrUsers = new ArrayList<>();
            for (String hrUserId : userIds) {
                User hrUser = userMapper.getUserById(hrUserId);
                if (hrUser != null && "ACTIVE".equalsIgnoreCase(hrUser.getStatus())) {
                    activeHrUsers.add(hrUser);
                }
            }
            if (activeHrUsers.isEmpty()) {
                throw new RuntimeException("供应商【" + supplier.getSupplierName() + "】无可用HR用户");
            }

            demandDispatchMapper.deleteByDemandIdAndSupplierId(demandId, supplierId);
            for (User hrUser : activeHrUsers) {
                DemandDispatch dispatch = new DemandDispatch();
                dispatch.setDemandId(demandId);
                dispatch.setSupplierId(supplierId);
                dispatch.setSupplierName(supplier.getSupplierName());
                dispatch.setHrUserId(hrUser.getUserId());
                dispatch.setHrUserName(hrUser.getRealName());
                dispatch.setReceiveStatus("PENDING");
                dispatch.setDispatchTime(now);
                dispatch.setCreateUserId(operatorUserId);
                dispatch.setCreateUserName(operatorUserName);
                dispatch.setCreateTime(now);
                dispatch.setUpdateUserId(operatorUserId);
                dispatch.setUpdateUserName(operatorUserName);
                dispatch.setUpdateTime(now);
                demandDispatchMapper.insert(dispatch);
                sendDispatchMessage(demand, supplier.getSupplierName(), hrUser.getUserId(), hrUser.getRealName());
            }
        }

        refreshDemandStatus(demand, operatorUserId, operatorUserName);
        insertLog(demandId, "DISPATCH", "分发给供应商", operatorUserId, operatorUserName);
    }

    @Override
    @Transactional
    public void confirmReceive(Long demandId, String operatorUserId, String operatorUserName) {
        User operator = userMapper.getUserById(operatorUserId);
        Set<String> roles = normalizeRoles(operator, null);
        ensureConfirmReceiveAccess(operatorUserId, roles);

        DemandRequirement demand = demandRequirementMapper.selectByPrimaryKey(demandId);
        if (demand == null) {
            throw new RuntimeException("需求不存在");
        }

        List<DemandDispatch> mine = demandDispatchMapper.selectByDemandIdAndHrUserId(demandId, operatorUserId);
        if (mine == null || mine.isEmpty()) {
            throw new RuntimeException("当前用户无可接收的分发需求");
        }

        demandDispatchMapper.updateReceiveStatusByDemandIdAndHrUserId(demandId, operatorUserId, "ACCEPTED", operatorUserId, operatorUserName);
        refreshDemandStatus(demand, operatorUserId, operatorUserName);

        Set<String> supplierNames = mine.stream()
            .map(DemandDispatch::getSupplierName)
            .filter(name -> name != null && !name.trim().isEmpty())
            .collect(Collectors.toCollection(LinkedHashSet::new));
        for (String supplierName : supplierNames) {
            sendReceiveConfirmMessageToOutsourcingManagers(supplierName, demand.getPositionOrgName());
        }
        insertLog(demandId, "CONFIRM_RECEIVE", "供应商HR确认接收", operatorUserId, operatorUserName);
    }

    private Map<String, Object> buildDemandView(DemandRequirement demand, String viewerId) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("demandId", demand.getDemandId());
        row.put("positionOrgName", demand.getPositionOrgName());
        row.put("vacancyCount", demand.getVacancyCount());
        row.put("technicalPlatform", demand.getTechnicalPlatform());
        row.put("recruitLevel", demand.getRecruitLevel());
        row.put("recruitCount", demand.getRecruitCount());
        row.put("positionResponsibility", demand.getPositionResponsibility());
        row.put("recruitRequirement", demand.getRecruitRequirement());
        row.put("acceptanceStatus", demand.getAcceptanceStatus());
        row.put("demandStatus", demand.getDemandStatus());
        row.put("createTime", demand.getCreateTime());
        row.put("updateTime", demand.getUpdateTime());
        row.put("createUserId", demand.getCreateUserId());
        row.put("createUserName", demand.getCreateUserName());
        row.put("updateUserId", demand.getUpdateUserId());
        row.put("updateUserName", demand.getUpdateUserName());

        List<DemandDispatch> dispatches = demandDispatchMapper.selectByDemandId(demand.getDemandId());
        Set<String> allSuppliers = dispatches.stream()
            .map(DemandDispatch::getSupplierName)
            .filter(name -> name != null && !name.trim().isEmpty())
            .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> acceptedSuppliers = dispatches.stream()
            .filter(item -> "ACCEPTED".equals(item.getReceiveStatus()))
            .map(DemandDispatch::getSupplierName)
            .filter(name -> name != null && !name.trim().isEmpty())
            .collect(Collectors.toCollection(LinkedHashSet::new));

        row.put("dispatchSuppliers", String.join("、", allSuppliers));
        row.put("acceptedSuppliers", String.join("、", acceptedSuppliers));

        boolean canConfirm = false;
        if (viewerId != null && !viewerId.trim().isEmpty()) {
            canConfirm = dispatches.stream().anyMatch(item ->
                viewerId.equals(item.getHrUserId()) && !"ACCEPTED".equals(item.getReceiveStatus()));
        }
        row.put("canConfirm", canConfirm);
        return row;
    }

    private void refreshDemandStatus(DemandRequirement demand, String operatorUserId, String operatorUserName) {
        List<DemandDispatch> dispatches = demandDispatchMapper.selectByDemandId(demand.getDemandId());
        Set<Long> allSupplierIds = new LinkedHashSet<>();
        Set<Long> acceptedSupplierIds = new LinkedHashSet<>();
        for (DemandDispatch dispatch : dispatches) {
            if (dispatch.getSupplierId() == null) {
                continue;
            }
            allSupplierIds.add(dispatch.getSupplierId());
            if ("ACCEPTED".equals(dispatch.getReceiveStatus())) {
                acceptedSupplierIds.add(dispatch.getSupplierId());
            }
        }

        demand.setDispatchSupplierCount(allSupplierIds.size());
        demand.setReceivedSupplierCount(acceptedSupplierIds.size());
        if (allSupplierIds.isEmpty()) {
            demand.setAcceptanceStatus("未接收");
            demand.setDemandStatus("待分发");
        } else if (acceptedSupplierIds.isEmpty()) {
            demand.setAcceptanceStatus("未接收");
            demand.setDemandStatus("已分发");
        } else if (acceptedSupplierIds.size() == allSupplierIds.size()) {
            demand.setAcceptanceStatus("全部确认接收");
            demand.setDemandStatus("接收完成");
        } else if (acceptedSupplierIds.size() > 1) {
            demand.setAcceptanceStatus("部分确认接收");
            demand.setDemandStatus("已分发");
        } else {
            demand.setAcceptanceStatus("未接收");
            demand.setDemandStatus("已分发");
        }
        demand.setUpdateTime(new Date());
        demand.setUpdateUserId(operatorUserId);
        demand.setUpdateUserName(operatorUserName);
        demandRequirementMapper.updateByPrimaryKey(demand);
    }

    private void insertLog(Long demandId, String operationType, String operationDetail, String operatorUserId, String operatorUserName) {
        DemandOperationLog log = new DemandOperationLog();
        log.setDemandId(demandId);
        log.setOperationType(operationType);
        log.setOperationDetail(operationDetail);
        log.setOperatorUserId(operatorUserId);
        log.setOperatorUserName(operatorUserName);
        log.setOperationTime(new Date());
        demandOperationLogMapper.insert(log);
    }

    private void sendDispatchMessage(DemandRequirement demand, String supplierName, String targetUserId, String targetUserName) {
        Message message = new Message();
        message.setTitle("需求分发提醒");
        message.setContent("需求分发提醒：" + supplierName + "已收到" + nullSafe(demand.getPositionOrgName()) + "需求，请及时确认接收。");
        message.setType("SYSTEM");
        message.setTargetUserId(targetUserId);
        message.setCreateUserId(SYSTEM_USER_ID);
        message.setCreateUserName(SYSTEM_USER_NAME);
        messageService.createMessage(message);
    }

    private void sendReceiveConfirmMessageToOutsourcingManagers(String supplierName, String positionOrgName) {
        Set<String> sent = new HashSet<>();
        List<User> managers = new ArrayList<>();
        managers.addAll(userMapper.getActiveUsersByRoleName("外包招聘管理岗"));
        managers.addAll(userMapper.getActiveUsersByRoleName("外包招聘管理"));
        managers.addAll(userMapper.getActiveUsersByRoleName("外包招聘岗"));
        for (User manager : managers) {
            if (manager == null || manager.getUserId() == null || !sent.add(manager.getUserId())) {
                continue;
            }
            Message message = new Message();
            message.setTitle("需求接收提醒");
            message.setContent("@" + supplierName + "@已确认@" + nullSafe(positionOrgName) + "@需求，请及时查看！");
            message.setType("SYSTEM");
            message.setTargetUserId(manager.getUserId());
            message.setCreateUserId(SYSTEM_USER_ID);
            message.setCreateUserName(SYSTEM_USER_NAME);
            messageService.createMessage(message);
        }
    }

    private Set<String> normalizeRoles(User user, String viewerRole) {
        Set<String> roles = new LinkedHashSet<>();
        if (user != null) {
            if (SYSTEM_USER_ID.equals(user.getUserId())) {
                roles.add(ROLE_SUPER_ADMIN);
            }
            for (String roleName : userMapper.getRoleNamesByUserId(user.getUserId())) {
                roles.add(normalizeRole(roleName));
            }
            roles.add(normalizeRole(user.getPosition()));
        }
        if (viewerRole != null && !viewerRole.trim().isEmpty()) {
            roles.add(normalizeRole(viewerRole));
        }
        roles.remove("");
        return roles;
    }

    private String normalizeRole(String roleName) {
        if (roleName == null) {
            return "";
        }
        String value = roleName.trim();
        if (value.contains("外包招聘")) {
            return ROLE_OUTSOURCING_MANAGER;
        }
        if (value.contains("供应商HR")) {
            return ROLE_SUPPLIER_HR;
        }
        if (value.contains("超级管理员") || value.contains("系统管理员")) {
            return ROLE_SUPER_ADMIN;
        }
        return value;
    }

    private boolean isSuperAdmin(String userId, Set<String> roles) {
        return SYSTEM_USER_ID.equals(userId) || roles.contains(ROLE_SUPER_ADMIN);
    }

    private void ensureDemandListAccess(String viewerId, Set<String> roles) {
        if (isSuperAdmin(viewerId, roles) || roles.contains(ROLE_OUTSOURCING_MANAGER) || roles.contains(ROLE_SUPPLIER_HR)) {
            return;
        }
        throw new RuntimeException("无权限访问需求管理");
    }

    private void ensureSupplierOptionsAccess(String viewerId, Set<String> roles) {
        if (isSuperAdmin(viewerId, roles) || roles.contains(ROLE_OUTSOURCING_MANAGER)) {
            return;
        }
        throw new RuntimeException("无权限获取供应商列表");
    }

    private void ensureDispatchAccess(String operatorUserId, Set<String> roles) {
        if (isSuperAdmin(operatorUserId, roles) || roles.contains(ROLE_OUTSOURCING_MANAGER)) {
            return;
        }
        throw new RuntimeException("仅外包招聘管理岗可分发需求");
    }

    private void ensureConfirmReceiveAccess(String operatorUserId, Set<String> roles) {
        if (isSuperAdmin(operatorUserId, roles) || roles.contains(ROLE_SUPPLIER_HR)) {
            return;
        }
        throw new RuntimeException("仅供应商HR可确认接收");
    }

    private String buildPositionOrgName(RecruitmentRequest request) {
        String category = request.getCategory() == null ? "-" : request.getCategory().trim();
        String orgUnit = request.getOrgUnitName() == null || request.getOrgUnitName().trim().isEmpty()
            ? request.getApplicationDepartment() : request.getOrgUnitName();
        if (orgUnit == null || orgUnit.trim().isEmpty()) {
            orgUnit = "-";
        }
        return category + " / " + orgUnit;
    }

    private String defaultValue(String first, String second, String fallback) {
        if (first != null && !first.trim().isEmpty()) {
            return first;
        }
        if (second != null && !second.trim().isEmpty()) {
            return second;
        }
        return fallback;
    }

    private String nullSafe(String value) {
        return value == null ? "-" : value;
    }
}
