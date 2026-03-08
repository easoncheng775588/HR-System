package com.hr.controller;

import com.hr.entity.OrgUnit;
import com.hr.service.OrgUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/org-units")
@CrossOrigin(origins = "*")
public class OrgUnitController {

    @Autowired
    private OrgUnitService orgUnitService;

    @GetMapping("/active")
    public Map<String, Object> getActiveOrgUnits() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<OrgUnit> units = orgUnitService.getActiveOrgUnits();
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", units);
        } catch (Exception e) {
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "Failed to get org units: " + e.getMessage());
            result.put("body", null);
        }
        return result;
    }
}

