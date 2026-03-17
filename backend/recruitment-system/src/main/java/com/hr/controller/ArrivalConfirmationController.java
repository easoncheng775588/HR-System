package com.hr.controller;

import com.hr.common.Response;
import com.hr.entity.SubmitArrivalConfirmationRequest;
import com.hr.entity.WorkflowApproveRequest;
import com.hr.service.ArrivalConfirmationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/arrival-confirmation")
@CrossOrigin(origins = "*")
public class ArrivalConfirmationController {

    @Autowired
    private ArrivalConfirmationService arrivalConfirmationService;

    @GetMapping("/list")
    public Response<?> getList(@RequestParam(value = "viewerId", required = false) String viewerId,
                               @RequestParam(value = "viewerRole", required = false) String viewerRole) {
        try {
            return Response.success(arrivalConfirmationService.getList(viewerId, viewerRole));
        } catch (Exception e) {
            return Response.fail("获取到岗确认列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/options")
    public Response<?> getFormOptions(@RequestParam(value = "operatorUserId", required = false) String operatorUserId,
                                      @RequestParam(value = "operatorUserRole", required = false) String operatorUserRole) {
        try {
            return Response.success(arrivalConfirmationService.getFormOptions(operatorUserId, operatorUserRole));
        } catch (Exception e) {
            return Response.fail("获取到岗确认表单选项失败: " + e.getMessage());
        }
    }

    @GetMapping("/{arrivalConfirmationId}")
    public Response<?> getDetail(@PathVariable Long arrivalConfirmationId) {
        try {
            return Response.success(arrivalConfirmationService.getDetail(arrivalConfirmationId));
        } catch (Exception e) {
            return Response.fail("获取到岗确认详情失败: " + e.getMessage());
        }
    }

    @PostMapping("/submit")
    public Response<?> submit(@RequestBody SubmitArrivalConfirmationRequest request) {
        try {
            return Response.success(arrivalConfirmationService.submit(request));
        } catch (Exception e) {
            return Response.fail("发起到岗确认失败: " + e.getMessage());
        }
    }

    @PostMapping("/{arrivalConfirmationId}/approve")
    public Response<?> approve(@PathVariable Long arrivalConfirmationId, @RequestBody WorkflowApproveRequest request) {
        try {
            arrivalConfirmationService.approve(arrivalConfirmationId, request);
            return Response.success("审批成功");
        } catch (Exception e) {
            return Response.fail("审批到岗确认失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{arrivalConfirmationId}")
    public Response<?> delete(@PathVariable Long arrivalConfirmationId,
                              @RequestParam(value = "operatorUserId", required = false) String operatorUserId,
                              @RequestParam(value = "operatorUserRole", required = false) String operatorUserRole) {
        try {
            arrivalConfirmationService.delete(arrivalConfirmationId, operatorUserId, operatorUserRole);
            return Response.success("删除成功");
        } catch (Exception e) {
            return Response.fail("删除到岗确认失败: " + e.getMessage());
        }
    }
}
