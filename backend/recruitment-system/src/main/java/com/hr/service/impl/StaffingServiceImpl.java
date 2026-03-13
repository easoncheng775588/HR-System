package com.hr.service.impl;

import com.hr.entity.Staffing;
import com.hr.mapper.OrgUnitMapper;
import com.hr.mapper.StaffingMapper;
import com.hr.service.StaffingService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StaffingServiceImpl implements StaffingService {

    private static final List<String> TEMPLATE_HEADERS = Arrays.asList(
        "团队/室组名称", "总编制数", "空缺编制数", "外包编制数", "员工编制数"
    );

    @Autowired
    private StaffingMapper staffingMapper;

    @Autowired
    private OrgUnitMapper orgUnitMapper;

    @Override
    public List<Staffing> getAllStaffings() {
        return staffingMapper.getAllStaffings();
    }

    @Override
    public Staffing getStaffingById(Long staffingId) {
        return staffingMapper.getStaffingById(staffingId);
    }

    @Override
    public Staffing getStaffingByOrgUnitName(String orgUnitName) {
        return staffingMapper.getStaffingByOrgUnitName(orgUnitName);
    }

    @Override
    @Transactional
    public int createStaffing(Staffing staffing) {
        validateStaffing(staffing, null);
        fillAuditForCreate(staffing);
        return staffingMapper.insertStaffing(staffing);
    }

    @Override
    @Transactional
    public int updateStaffing(Long staffingId, Staffing staffing) {
        Staffing existing = staffingMapper.getStaffingById(staffingId);
        if (existing == null) {
            return 0;
        }
        staffing.setStaffingId(staffingId);
        validateStaffing(staffing, staffingId);
        fillAuditForUpdate(staffing);
        return staffingMapper.updateStaffing(staffing);
    }

    @Override
    @Transactional
    public int deleteStaffing(Long staffingId) {
        return staffingMapper.deleteStaffing(staffingId);
    }

    @Override
    public byte[] generateTemplate() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("编制管理导入模板");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < TEMPLATE_HEADERS.size(); i++) {
                headerRow.createCell(i).setCellValue(TEMPLATE_HEADERS.get(i));
            }

            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("技术管理团队");
            exampleRow.createCell(1).setCellValue(100);
            exampleRow.createCell(2).setCellValue(10);
            exampleRow.createCell(3).setCellValue(30);
            exampleRow.createCell(4).setCellValue(60);

            for (int i = 0; i < TEMPLATE_HEADERS.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate template: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public Map<String, Object> importFromTemplate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("导入文件不能为空");
        }

        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();

        try (InputStream in = file.getInputStream(); Workbook workbook = new XSSFWorkbook(in)) {
            Sheet sheet = workbook.getSheetAt(0);
            validateTemplateHeader(sheet);

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                try {
                    Staffing staffing = parseStaffingRow(row);
                    upsertStaffing(staffing);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    errors.add("第" + (rowIndex + 1) + "行: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read import file: " + e.getMessage(), e);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("errors", errors);
        return result;
    }

    private void upsertStaffing(Staffing staffing) {
        Staffing existing = staffingMapper.getStaffingByOrgUnitName(staffing.getOrgUnitName());
        if (existing == null) {
            fillAuditForCreate(staffing);
            staffingMapper.insertStaffing(staffing);
            return;
        }

        staffing.setStaffingId(existing.getStaffingId());
        fillAuditForUpdate(staffing);
        staffingMapper.updateStaffing(staffing);
    }

    private Staffing parseStaffingRow(Row row) {
        String orgUnitName = getCellString(row.getCell(0));
        if (orgUnitName.isEmpty()) {
            throw new RuntimeException("团队/室组名称不能为空");
        }
        Integer orgUnitExists = orgUnitMapper.countByUnitName(orgUnitName);
        if (orgUnitExists == null || orgUnitExists == 0) {
            throw new RuntimeException("团队/室组不存在: " + orgUnitName);
        }

        int total = getCellInt(row.getCell(1), "总编制数");
        int vacancy = getCellInt(row.getCell(2), "空缺编制数");
        int outsourcing = getCellInt(row.getCell(3), "外包编制数");
        int employee = getCellInt(row.getCell(4), "员工编制数");

        Staffing staffing = new Staffing();
        staffing.setOrgUnitName(orgUnitName);
        staffing.setTotalHeadcount(total);
        staffing.setVacancyHeadcount(vacancy);
        staffing.setOutsourcingHeadcount(outsourcing);
        staffing.setEmployeeHeadcount(employee);
        validateHeadcount(staffing);
        return staffing;
    }

    private void validateTemplateHeader(Sheet sheet) {
        Row header = sheet.getRow(0);
        if (header == null) {
            throw new RuntimeException("模板表头缺失");
        }
        for (int i = 0; i < TEMPLATE_HEADERS.size(); i++) {
            String actual = getCellString(header.getCell(i));
            String expected = TEMPLATE_HEADERS.get(i);
            if (!expected.equals(actual)) {
                throw new RuntimeException("模板第" + (i + 1) + "列表头不匹配，期望: " + expected);
            }
        }
    }

    private boolean isRowEmpty(Row row) {
        for (int i = 0; i < TEMPLATE_HEADERS.size(); i++) {
            if (!getCellString(row.getCell(i)).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String getCellString(Cell cell) {
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((int) cell.getNumericCellValue());
        }
        if (cell.getCellType() == CellType.BLANK) {
            return "";
        }
        return cell.toString().trim();
    }

    private int getCellInt(Cell cell, String fieldName) {
        String text = getCellString(cell);
        if (text.isEmpty()) {
            throw new RuntimeException(fieldName + "不能为空");
        }
        try {
            int value = Integer.parseInt(text);
            if (value < 0) {
                throw new RuntimeException(fieldName + "不能为负数");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new RuntimeException(fieldName + "必须为非负整数");
        }
    }

    private void validateStaffing(Staffing staffing, Long excludeId) {
        if (staffing == null || staffing.getOrgUnitName() == null || staffing.getOrgUnitName().trim().isEmpty()) {
            throw new RuntimeException("团队/室组名称不能为空");
        }

        Integer orgUnitExists = orgUnitMapper.countByUnitName(staffing.getOrgUnitName());
        if (orgUnitExists == null || orgUnitExists == 0) {
            throw new RuntimeException("团队/室组不存在");
        }

        if (staffing.getTotalHeadcount() == null || staffing.getVacancyHeadcount() == null
            || staffing.getOutsourcingHeadcount() == null || staffing.getEmployeeHeadcount() == null) {
            throw new RuntimeException("编制字段不能为空");
        }

        if (staffing.getTotalHeadcount() < 0 || staffing.getVacancyHeadcount() < 0
            || staffing.getOutsourcingHeadcount() < 0 || staffing.getEmployeeHeadcount() < 0) {
            throw new RuntimeException("编制字段不能为负数");
        }

        validateHeadcount(staffing);

        Integer duplicate = staffingMapper.countByOrgUnitName(staffing.getOrgUnitName(), excludeId);
        if (duplicate != null && duplicate > 0) {
            throw new RuntimeException("该团队/室组编制记录已存在");
        }
    }

    private void validateHeadcount(Staffing staffing) {
        int sum = staffing.getVacancyHeadcount() + staffing.getOutsourcingHeadcount() + staffing.getEmployeeHeadcount();
        if (sum > staffing.getTotalHeadcount()) {
            throw new RuntimeException("空缺+外包+员工编制不能大于总编制数");
        }
    }

    private void fillAuditForCreate(Staffing staffing) {
        Date now = new Date();
        staffing.setCreateTime(now);
        staffing.setUpdateTime(now);
        if (staffing.getCreateUserId() == null || staffing.getCreateUserId().trim().isEmpty()) {
            staffing.setCreateUserId("1001");
        }
        if (staffing.getCreateUserName() == null || staffing.getCreateUserName().trim().isEmpty()) {
            staffing.setCreateUserName("SYSTEM");
        }
        staffing.setUpdateUserId(staffing.getCreateUserId());
        staffing.setUpdateUserName(staffing.getCreateUserName());
    }

    private void fillAuditForUpdate(Staffing staffing) {
        staffing.setUpdateTime(new Date());
        if (staffing.getUpdateUserId() == null || staffing.getUpdateUserId().trim().isEmpty()) {
            staffing.setUpdateUserId("1001");
        }
        if (staffing.getUpdateUserName() == null || staffing.getUpdateUserName().trim().isEmpty()) {
            staffing.setUpdateUserName("SYSTEM");
        }
    }
}
