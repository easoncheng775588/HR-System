package com.hr.controller;

import com.hr.common.Response;
import com.hr.service.DemandManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/demand-management")
@CrossOrigin(origins = "*")
public class DemandManagementController {

    @Autowired
    private DemandManagementService demandManagementService;

    @GetMapping("/list")
    public Response<?> getDemandList(@RequestParam(value = "viewerId", required = false) String viewerId,
                                     @RequestParam(value = "viewerRole", required = false) String viewerRole) {
        try {
            return Response.success(demandManagementService.getDemandList(viewerId, viewerRole));
        } catch (Exception e) {
            return Response.fail("获取需求列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/suppliers")
    public Response<?> getSupplierOptions(@RequestParam(value = "viewerId", required = false) String viewerId,
                                          @RequestParam(value = "viewerRole", required = false) String viewerRole) {
        try {
            return Response.success(demandManagementService.getSupplierOptions(viewerId, viewerRole));
        } catch (Exception e) {
            return Response.fail("获取供应商列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/{demandId}/dispatch")
    public Response<?> dispatch(@PathVariable Long demandId, @RequestBody Map<String, Object> params) {
        try {
            @SuppressWarnings("unchecked")
            List<Number> rawSupplierIds = (List<Number>) params.get("supplierIds");
            List<Long> supplierIds = rawSupplierIds == null ? List.of() : rawSupplierIds.stream().map(Number::longValue).toList();
            String operatorUserId = String.valueOf(params.getOrDefault("operatorUserId", ""));
            String operatorUserName = String.valueOf(params.getOrDefault("operatorUserName", ""));
            demandManagementService.dispatchToSuppliers(demandId, supplierIds, operatorUserId, operatorUserName);
            return Response.success("分发成功");
        } catch (Exception e) {
            return Response.fail("分发失败: " + e.getMessage());
        }
    }

    @PostMapping("/{demandId}/confirm")
    public Response<?> confirmReceive(@PathVariable Long demandId, @RequestBody Map<String, Object> params) {
        try {
            String operatorUserId = String.valueOf(params.getOrDefault("operatorUserId", ""));
            String operatorUserName = String.valueOf(params.getOrDefault("operatorUserName", ""));
            demandManagementService.confirmReceive(demandId, operatorUserId, operatorUserName);
            return Response.success("确认接收成功");
        } catch (Exception e) {
            return Response.fail("确认接收失败: " + e.getMessage());
        }
    }
}
