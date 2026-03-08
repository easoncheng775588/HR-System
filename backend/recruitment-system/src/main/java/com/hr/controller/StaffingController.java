package com.hr.controller;

import com.hr.entity.Staffing;
import com.hr.service.StaffingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staffings")
@CrossOrigin(origins = "*")
public class StaffingController {

    @Autowired
    private StaffingService staffingService;

    @GetMapping
    public Map<String, Object> getAllStaffings() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Staffing> list = staffingService.getAllStaffings();
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", list);
        } catch (Exception e) {
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "Failed to get staffing list: " + e.getMessage());
            result.put("body", null);
        }
        return result;
    }

    @GetMapping("/{staffingId}")
    public Map<String, Object> getStaffingById(@PathVariable Long staffingId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Staffing staffing = staffingService.getStaffingById(staffingId);
            if (staffing == null) {
                result.put("returnCode", "ERR0004");
                result.put("errorMsg", "Staffing does not exist");
                result.put("body", null);
                return result;
            }
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", staffing);
        } catch (Exception e) {
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "Failed to get staffing detail: " + e.getMessage());
            result.put("body", null);
        }
        return result;
    }

    @GetMapping("/by-org-unit")
    public Map<String, Object> getStaffingByOrgUnit(@RequestParam("orgUnitName") String orgUnitName) {
        Map<String, Object> result = new HashMap<>();
        try {
            Staffing staffing = staffingService.getStaffingByOrgUnitName(orgUnitName);
            if (staffing == null) {
                result.put("returnCode", "ERR0004");
                result.put("errorMsg", "Staffing does not exist");
                result.put("body", null);
                return result;
            }
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", staffing);
        } catch (Exception e) {
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "Failed to get staffing by org unit: " + e.getMessage());
            result.put("body", null);
        }
        return result;
    }

    @PostMapping
    public Map<String, Object> createStaffing(@RequestBody Staffing staffing) {
        Map<String, Object> result = new HashMap<>();
        try {
            int count = staffingService.createStaffing(staffing);
            if (count > 0) {
                result.put("returnCode", "SUC0000");
                result.put("errorMsg", "");
                result.put("body", "Staffing created successfully");
            } else {
                result.put("returnCode", "ERR0002");
                result.put("errorMsg", "Failed to create staffing");
                result.put("body", null);
            }
        } catch (Exception e) {
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "Failed to create staffing: " + e.getMessage());
            result.put("body", null);
        }
        return result;
    }

    @PutMapping("/{staffingId}")
    public Map<String, Object> updateStaffing(@PathVariable Long staffingId, @RequestBody Staffing staffing) {
        Map<String, Object> result = new HashMap<>();
        try {
            int count = staffingService.updateStaffing(staffingId, staffing);
            if (count > 0) {
                result.put("returnCode", "SUC0000");
                result.put("errorMsg", "");
                result.put("body", "Staffing updated successfully");
            } else {
                result.put("returnCode", "ERR0004");
                result.put("errorMsg", "Staffing does not exist");
                result.put("body", null);
            }
        } catch (Exception e) {
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "Failed to update staffing: " + e.getMessage());
            result.put("body", null);
        }
        return result;
    }

    @DeleteMapping("/{staffingId}")
    public Map<String, Object> deleteStaffing(@PathVariable Long staffingId) {
        Map<String, Object> result = new HashMap<>();
        try {
            int count = staffingService.deleteStaffing(staffingId);
            if (count > 0) {
                result.put("returnCode", "SUC0000");
                result.put("errorMsg", "");
                result.put("body", "Staffing deleted successfully");
            } else {
                result.put("returnCode", "ERR0004");
                result.put("errorMsg", "Staffing does not exist");
                result.put("body", null);
            }
        } catch (Exception e) {
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "Failed to delete staffing: " + e.getMessage());
            result.put("body", null);
        }
        return result;
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        byte[] bytes = staffingService.generateTemplate();
        String filename = URLEncoder.encode("编制管理导入模板.xlsx", StandardCharsets.UTF_8);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(bytes);
    }

    @PostMapping("/import")
    public Map<String, Object> importFromTemplate(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> importResult = staffingService.importFromTemplate(file);
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", importResult);
        } catch (Exception e) {
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "Failed to import staffing data: " + e.getMessage());
            result.put("body", null);
        }
        return result;
    }
}
