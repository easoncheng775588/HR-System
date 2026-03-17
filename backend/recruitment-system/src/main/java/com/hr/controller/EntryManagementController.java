package com.hr.controller;

import com.hr.common.Response;
import com.hr.entity.EntryRecordUpdateRequest;
import com.hr.service.EntryManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/entry-management")
@CrossOrigin(origins = "*")
public class EntryManagementController {

    @Autowired
    private EntryManagementService entryManagementService;

    @GetMapping("/list")
    public Response<?> getEntryList(@RequestParam(value = "viewerId", required = false) String viewerId,
                                    @RequestParam(value = "viewerRole", required = false) String viewerRole) {
        try {
            return Response.success(entryManagementService.getEntryList(viewerId, viewerRole));
        } catch (Exception e) {
            return Response.fail("获取入场列表失败: " + e.getMessage());
        }
    }

    @PutMapping("/{entryRecordId}")
    public Response<?> updateEntry(@PathVariable Long entryRecordId, @RequestBody EntryRecordUpdateRequest request) {
        try {
            return Response.success(entryManagementService.updateEntry(entryRecordId, request));
        } catch (Exception e) {
            return Response.fail("更新入场信息失败: " + e.getMessage());
        }
    }
}
