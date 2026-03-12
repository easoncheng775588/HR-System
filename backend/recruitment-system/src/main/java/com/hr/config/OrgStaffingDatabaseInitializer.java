package com.hr.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class OrgStaffingDatabaseInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(OrgStaffingDatabaseInitializer.class);
    private static final String COMPANY_NAME = "永隆信息有限公司";

    private final JdbcTemplate jdbcTemplate;

    public OrgStaffingDatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            createTables();
            seedOrgUnits();
        } catch (Exception e) {
            logger.error("Failed to initialize org staffing tables", e);
        }
    }

    private void createTables() {
        String createOrgUnitSql = "CREATE TABLE IF NOT EXISTS org_unit ("
            + "unit_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Unit ID', "
            + "unit_name VARCHAR(100) NOT NULL UNIQUE COMMENT 'Organization Unit Name', "
            + "unit_type VARCHAR(20) NOT NULL COMMENT 'ROOT, TEAM, GROUP, CATEGORY or LEAF', "
            + "parent_unit_name VARCHAR(100) COMMENT 'Parent Unit Name', "
            + "status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Status', "
            + "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time', "
            + "create_user_id VARCHAR(20) COMMENT 'Create User ID', "
            + "create_user_name VARCHAR(50) COMMENT 'Create User Name', "
            + "update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time', "
            + "update_user_id VARCHAR(20) COMMENT 'Update User ID', "
            + "update_user_name VARCHAR(50) COMMENT 'Update User Name'"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Organization Unit Table'";

        String createStaffingSql = "CREATE TABLE IF NOT EXISTS org_staffing ("
            + "staffing_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Staffing ID', "
            + "org_unit_name VARCHAR(100) NOT NULL UNIQUE COMMENT 'Team or Group Name', "
            + "total_headcount INT NOT NULL DEFAULT 0 COMMENT 'Total Headcount', "
            + "vacancy_headcount INT NOT NULL DEFAULT 0 COMMENT 'Vacancy Headcount', "
            + "outsourcing_headcount INT NOT NULL DEFAULT 0 COMMENT 'Outsourcing Headcount', "
            + "employee_headcount INT NOT NULL DEFAULT 0 COMMENT 'Employee Headcount', "
            + "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time', "
            + "create_user_id VARCHAR(20) COMMENT 'Create User ID', "
            + "create_user_name VARCHAR(50) COMMENT 'Create User Name', "
            + "update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time', "
            + "update_user_id VARCHAR(20) COMMENT 'Update User ID', "
            + "update_user_name VARCHAR(50) COMMENT 'Update User Name'"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Organization Staffing Table'";

        jdbcTemplate.execute(createOrgUnitSql);
        jdbcTemplate.execute(createStaffingSql);
    }

    private void seedOrgUnits() {
        insertOrUpdateOrgUnit(COMPANY_NAME, "ROOT", null);

        // 团队层级
        insertOrUpdateOrgUnit("零售业务开发团队", "TEAM", COMPANY_NAME);
        insertOrUpdateOrgUnit("批业务开发团队", "TEAM", COMPANY_NAME);
        insertOrUpdateOrgUnit("基础业务开发团队", "TEAM", COMPANY_NAME);
        insertOrUpdateOrgUnit("数据开发团队", "TEAM", COMPANY_NAME);
        insertOrUpdateOrgUnit("测试团队", "TEAM", COMPANY_NAME);
        insertOrUpdateOrgUnit("技术管理团队", "TEAM", COMPANY_NAME);
        insertOrUpdateOrgUnit("人力资源团队", "TEAM", COMPANY_NAME);
        insertOrUpdateOrgUnit("综合管理团队", "TEAM", COMPANY_NAME);
        insertOrUpdateOrgUnit("直属人员", "CATEGORY", COMPANY_NAME);
        insertOrUpdateOrgUnit("其他", "CATEGORY", COMPANY_NAME);

        // 室组层级
        insertOrUpdateOrgUnit("零售平台开发室", "GROUP", "零售业务开发团队");

        insertOrUpdateOrgUnit("批发财富业务开发室", "GROUP", "批业务开发团队");
        insertOrUpdateOrgUnit("批发网络应用开发室", "GROUP", "批业务开发团队");
        insertOrUpdateOrgUnit("信贷业务开发室", "GROUP", "批业务开发团队");
        insertOrUpdateOrgUnit("财资应用开发室", "GROUP", "批业务开发团队");

        insertOrUpdateOrgUnit("基础业务开发室", "GROUP", "基础业务开发团队");
        insertOrUpdateOrgUnit("运营业务开发室", "GROUP", "基础业务开发团队");
        insertOrUpdateOrgUnit("合规业务开发室", "GROUP", "基础业务开发团队");
        insertOrUpdateOrgUnit("基础产品开发室", "GROUP", "基础业务开发团队");
        insertOrUpdateOrgUnit("支付业务开发室", "GROUP", "基础业务开发团队");
        insertOrUpdateOrgUnit("办公系统开发室", "GROUP", "基础业务开发团队");

        insertOrUpdateOrgUnit("经营业务开发室", "GROUP", "数据开发团队");

        insertOrUpdateOrgUnit("业务测试一室", "GROUP", "测试团队");
        insertOrUpdateOrgUnit("业务测试二室", "GROUP", "测试团队");

        // 分类节点
        insertOrUpdateOrgUnit("部门总经理", "LEAF", "直属人员");
        insertOrUpdateOrgUnit("分管总", "LEAF", "直属人员");
        insertOrUpdateOrgUnit("外包厂商人员", "LEAF", "其他");
        insertOrUpdateOrgUnit("供应商HR", "LEAF", "其他");
    }

    private void insertOrUpdateOrgUnit(String unitName, String unitType, String parentUnitName) {
        try {
            jdbcTemplate.update(
                "INSERT INTO org_unit (unit_name, unit_type, parent_unit_name, status, create_user_id, create_user_name, update_user_id, update_user_name) "
                    + "SELECT ?, ?, ?, 'ACTIVE', '1001', 'SYSTEM', '1001', 'SYSTEM' "
                    + "WHERE NOT EXISTS (SELECT 1 FROM org_unit WHERE unit_name = ?)",
                unitName, unitType, parentUnitName, unitName
            );

            jdbcTemplate.update(
                "UPDATE org_unit SET unit_type = ?, parent_unit_name = ?, status = 'ACTIVE' WHERE unit_name = ?",
                unitType, parentUnitName, unitName
            );
        } catch (Exception e) {
            logger.warn("Seed org unit failed for {}: {}", unitName, e.getMessage());
        }
    }
}
