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
public class WorkflowEngineDatabaseInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowEngineDatabaseInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    public WorkflowEngineDatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            createNodeConfigTable();
            createProcessLogTable();
            seedRecruitmentFlowConfig();
        } catch (Exception e) {
            logger.error("Failed to initialize workflow engine tables", e);
        }
    }

    private void createNodeConfigTable() {
        String sql = "CREATE TABLE IF NOT EXISTS wf_process_node_config ("
            + "node_config_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Node config id', "
            + "process_code VARCHAR(64) NOT NULL COMMENT 'Process code', "
            + "node_order INT NOT NULL COMMENT 'Node order', "
            + "node_name VARCHAR(100) NOT NULL COMMENT 'Node name', "
            + "approver_role VARCHAR(100) NOT NULL COMMENT 'Approver role', "
            + "next_node_order INT DEFAULT NULL COMMENT 'Next node order', "
            + "is_final TINYINT NOT NULL DEFAULT 0 COMMENT 'Final node flag', "
            + "status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Status', "
            + "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time', "
            + "update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time', "
            + "UNIQUE KEY uk_process_node (process_code, node_order)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Workflow node config'";
        jdbcTemplate.execute(sql);
    }

    private void createProcessLogTable() {
        String sql = "CREATE TABLE IF NOT EXISTS wf_process_log ("
            + "log_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Log id', "
            + "process_code VARCHAR(64) NOT NULL COMMENT 'Process code', "
            + "business_id BIGINT NOT NULL COMMENT 'Business id', "
            + "node_order INT DEFAULT NULL COMMENT 'Node order', "
            + "node_name VARCHAR(100) DEFAULT NULL COMMENT 'Node name', "
            + "action_type VARCHAR(30) NOT NULL COMMENT 'Action type', "
            + "action_result VARCHAR(30) DEFAULT NULL COMMENT 'Action result', "
            + "operator_id VARCHAR(20) DEFAULT NULL COMMENT 'Operator id', "
            + "operator_name VARCHAR(50) DEFAULT NULL COMMENT 'Operator name', "
            + "operator_role VARCHAR(100) DEFAULT NULL COMMENT 'Operator role', "
            + "action_comment VARCHAR(500) DEFAULT NULL COMMENT 'Action comment', "
            + "action_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Action time', "
            + "INDEX idx_process_business (process_code, business_id), "
            + "INDEX idx_action_time (action_time)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Workflow action log'";
        jdbcTemplate.execute(sql);
    }

    private void seedRecruitmentFlowConfig() {
        insertNode("RECRUITMENT_REQUEST", 1, "一级审批", "编制管理岗", 2, 0);
        insertNode("RECRUITMENT_REQUEST", 2, "二级审批", "外包招聘管理岗", 3, 0);
        insertNode("RECRUITMENT_REQUEST", 3, "三级审批", "团队经理", null, 1);
    }

    private void insertNode(String processCode, Integer nodeOrder, String nodeName, String approverRole, Integer nextNodeOrder, Integer isFinal) {
        String sql = "INSERT INTO wf_process_node_config (process_code, node_order, node_name, approver_role, next_node_order, is_final, status) "
            + "SELECT ?, ?, ?, ?, ?, ?, 'ACTIVE' "
            + "WHERE NOT EXISTS (SELECT 1 FROM wf_process_node_config WHERE process_code = ? AND node_order = ?)";
        jdbcTemplate.update(sql, processCode, nodeOrder, nodeName, approverRole, nextNodeOrder, isFinal, processCode, nodeOrder);
    }
}
