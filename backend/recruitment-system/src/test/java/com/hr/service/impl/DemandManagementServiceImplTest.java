package com.hr.service.impl;

import com.hr.entity.DemandDispatch;
import com.hr.entity.DemandRequirement;
import com.hr.entity.Message;
import com.hr.entity.Supplier;
import com.hr.entity.User;
import com.hr.mapper.DemandDispatchMapper;
import com.hr.mapper.DemandOperationLogMapper;
import com.hr.mapper.DemandRequirementMapper;
import com.hr.mapper.SupplierMapper;
import com.hr.mapper.UserMapper;
import com.hr.service.MessageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemandManagementServiceImplTest {

    @Mock
    private DemandRequirementMapper demandRequirementMapper;

    @Mock
    private DemandDispatchMapper demandDispatchMapper;

    @Mock
    private DemandOperationLogMapper demandOperationLogMapper;

    @Mock
    private SupplierMapper supplierMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private MessageService messageService;

    @InjectMocks
    private DemandManagementServiceImpl demandManagementService;

    @Captor
    private ArgumentCaptor<DemandRequirement> demandCaptor;

    @Captor
    private ArgumentCaptor<Message> messageCaptor;

    @Test
    void confirmReceiveTreatsSingleAcceptedAsUnreceivedWhenMultipleSuppliers() {
        DemandRequirement demand = new DemandRequirement();
        demand.setDemandId(1L);
        demand.setPositionOrgName("基础开发 / 办公系统开发室");

        when(userMapper.getUserById("9001")).thenReturn(activeUser("9001", "供应商HRA", "供应商HR"));
        when(userMapper.getRoleNamesByUserId("9001")).thenReturn(Collections.singletonList("供应商HR"));
        when(demandRequirementMapper.selectByPrimaryKey(1L)).thenReturn(demand);
        when(demandDispatchMapper.selectByDemandIdAndHrUserId(1L, "9001"))
            .thenReturn(Collections.singletonList(dispatch(1L, 11L, "供应商A", "9001", "PENDING")));
        when(demandDispatchMapper.selectByDemandId(1L))
            .thenReturn(Arrays.asList(
                dispatch(1L, 11L, "供应商A", "9001", "ACCEPTED"),
                dispatch(1L, 12L, "供应商B", "9002", "PENDING")
            ));
        when(userMapper.getActiveUsersByRoleName("外包招聘管理岗")).thenReturn(Collections.emptyList());
        when(userMapper.getActiveUsersByRoleName("外包招聘管理")).thenReturn(Collections.emptyList());
        when(userMapper.getActiveUsersByRoleName("外包招聘岗")).thenReturn(Collections.emptyList());

        demandManagementService.confirmReceive(1L, "9001", "供应商HRA");

        verify(demandRequirementMapper).updateByPrimaryKey(demandCaptor.capture());
        assertEquals("未接收", demandCaptor.getValue().getAcceptanceStatus());
        assertEquals("已分发", demandCaptor.getValue().getDemandStatus());
        assertEquals(Integer.valueOf(2), demandCaptor.getValue().getDispatchSupplierCount());
        assertEquals(Integer.valueOf(1), demandCaptor.getValue().getReceivedSupplierCount());
    }

    @Test
    void confirmReceiveSendsFormattedMessageToOutsourcingManager() {
        DemandRequirement demand = new DemandRequirement();
        demand.setDemandId(2L);
        demand.setPositionOrgName("系统研发岗 / 办公系统开发室");

        when(userMapper.getUserById("9001")).thenReturn(activeUser("9001", "供应商HRA", "供应商HR"));
        when(userMapper.getRoleNamesByUserId("9001")).thenReturn(Collections.singletonList("供应商HR"));
        when(demandRequirementMapper.selectByPrimaryKey(2L)).thenReturn(demand);
        when(demandDispatchMapper.selectByDemandIdAndHrUserId(2L, "9001"))
            .thenReturn(Collections.singletonList(dispatch(2L, 11L, "供应商A", "9001", "PENDING")));
        when(demandDispatchMapper.selectByDemandId(2L))
            .thenReturn(Collections.singletonList(dispatch(2L, 11L, "供应商A", "9001", "ACCEPTED")));
        when(userMapper.getActiveUsersByRoleName("外包招聘管理岗"))
            .thenReturn(Collections.singletonList(activeUser("1002", "外包经理", "外包招聘管理岗")));
        when(userMapper.getActiveUsersByRoleName("外包招聘管理")).thenReturn(Collections.emptyList());
        when(userMapper.getActiveUsersByRoleName("外包招聘岗")).thenReturn(Collections.emptyList());

        demandManagementService.confirmReceive(2L, "9001", "供应商HRA");

        verify(messageService).createMessage(messageCaptor.capture());
        assertEquals("@供应商A@已确认@系统研发岗 / 办公系统开发室@需求，请及时查看！", messageCaptor.getValue().getContent());
    }

    @Test
    void dispatchRejectsSupplierWithoutHrMapping() {
        DemandRequirement demand = new DemandRequirement();
        demand.setDemandId(3L);
        Supplier supplier = new Supplier();
        supplier.setSupplierId(20L);
        supplier.setSupplierName("缺少HR供应商");
        supplier.setStatus("ACTIVE");

        when(userMapper.getUserById("1002")).thenReturn(activeUser("1002", "外包经理", "外包招聘管理岗"));
        when(userMapper.getRoleNamesByUserId("1002")).thenReturn(Collections.singletonList("外包招聘管理岗"));
        when(demandRequirementMapper.selectByPrimaryKey(3L)).thenReturn(demand);
        when(supplierMapper.getSupplierById(20L)).thenReturn(supplier);
        when(supplierMapper.getUserIdsBySupplierId(20L)).thenReturn(Collections.emptyList());

        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> demandManagementService.dispatchToSuppliers(3L, List.of(20L), "1002", "外包经理")
        );
        assertEquals("供应商【缺少HR供应商】未配置HR用户", exception.getMessage());
        verify(demandDispatchMapper, never()).insert(any());
        verify(demandRequirementMapper, never()).updateByPrimaryKey(any());
    }

    private User activeUser(String userId, String realName, String position) {
        User user = new User();
        user.setUserId(userId);
        user.setRealName(realName);
        user.setPosition(position);
        user.setStatus("ACTIVE");
        return user;
    }

    private DemandDispatch dispatch(Long demandId, Long supplierId, String supplierName, String hrUserId, String status) {
        DemandDispatch dispatch = new DemandDispatch();
        dispatch.setDemandId(demandId);
        dispatch.setSupplierId(supplierId);
        dispatch.setSupplierName(supplierName);
        dispatch.setHrUserId(hrUserId);
        dispatch.setReceiveStatus(status);
        return dispatch;
    }
}
