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
public class SupplierDatabaseInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(SupplierDatabaseInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    public SupplierDatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            String createSupplierSql = "CREATE TABLE IF NOT EXISTS sys_supplier (" +
                "supplier_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Supplier ID', " +
                "supplier_name VARCHAR(100) NOT NULL UNIQUE COMMENT 'Supplier Name', " +
                "status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Status', " +
                "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time', " +
                "create_user_id VARCHAR(20) COMMENT 'Create User ID', " +
                "create_user_name VARCHAR(50) COMMENT 'Create User Name', " +
                "update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time', " +
                "update_user_id VARCHAR(20) COMMENT 'Update User ID', " +
                "update_user_name VARCHAR(50) COMMENT 'Update User Name'" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Supplier Table'";

            String createSupplierHrSql = "CREATE TABLE IF NOT EXISTS sys_supplier_hr (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID', " +
                "supplier_id BIGINT NOT NULL COMMENT 'Supplier ID', " +
                "user_id VARCHAR(20) NOT NULL COMMENT 'User ID', " +
                "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time', " +
                "create_user_id VARCHAR(20) COMMENT 'Create User ID', " +
                "create_user_name VARCHAR(50) COMMENT 'Create User Name'" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Supplier HR Mapping Table'";

            jdbcTemplate.execute(createSupplierSql);
            jdbcTemplate.execute(createSupplierHrSql);

            try {
                jdbcTemplate.execute("CREATE INDEX idx_supplier_hr_supplier_id ON sys_supplier_hr(supplier_id)");
            } catch (Exception e) {
                logger.info("Index idx_supplier_hr_supplier_id already exists or creation failed: {}", e.getMessage());
            }

            try {
                jdbcTemplate.execute("CREATE INDEX idx_supplier_hr_user_id ON sys_supplier_hr(user_id)");
            } catch (Exception e) {
                logger.info("Index idx_supplier_hr_user_id already exists or creation failed: {}", e.getMessage());
            }
        } catch (Exception e) {
            logger.error("Failed to initialize supplier tables: {}", e.getMessage(), e);
        }
    }
}

