USE hr_system;

SET @add_team_name = (
    SELECT IF(
        EXISTS(
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'sys_user'
              AND COLUMN_NAME = 'team_name'
        ),
        'SELECT 1',
        "ALTER TABLE sys_user ADD COLUMN team_name VARCHAR(100) COMMENT '团队名称' AFTER department"
    )
);
PREPARE stmt FROM @add_team_name;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_group_name = (
    SELECT IF(
        EXISTS(
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'sys_user'
              AND COLUMN_NAME = 'group_name'
        ),
        'SELECT 1',
        "ALTER TABLE sys_user ADD COLUMN group_name VARCHAR(100) COMMENT '室组名称' AFTER team_name"
    )
);
PREPARE stmt FROM @add_group_name;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS org_unit (
    unit_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Unit ID',
    unit_name VARCHAR(100) NOT NULL UNIQUE COMMENT 'Organization Unit Name',
    unit_type VARCHAR(20) NOT NULL COMMENT 'ROOT, TEAM, GROUP, CATEGORY or LEAF',
    parent_unit_name VARCHAR(100) COMMENT 'Parent Unit Name',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Status',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    create_user_id VARCHAR(20) COMMENT 'Create User ID',
    create_user_name VARCHAR(50) COMMENT 'Create User Name',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    update_user_id VARCHAR(20) COMMENT 'Update User ID',
    update_user_name VARCHAR(50) COMMENT 'Update User Name'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Organization Unit Table';

INSERT INTO org_unit (unit_name, unit_type, parent_unit_name, status, create_user_id, create_user_name, update_user_id, update_user_name)
SELECT '永隆信息有限公司', 'ROOT', NULL, 'ACTIVE', '1001', 'SYSTEM', '1001', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM org_unit WHERE unit_name = '永隆信息有限公司');

UPDATE org_unit SET unit_type = 'ROOT', parent_unit_name = NULL, status = 'ACTIVE' WHERE unit_name = '永隆信息有限公司';

INSERT INTO org_unit (unit_name, unit_type, parent_unit_name, status, create_user_id, create_user_name, update_user_id, update_user_name)
SELECT '零售业务开发团队', 'TEAM', '永隆信息有限公司', 'ACTIVE', '1001', 'SYSTEM', '1001', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM org_unit WHERE unit_name = '零售业务开发团队');
UPDATE org_unit SET unit_type = 'TEAM', parent_unit_name = '永隆信息有限公司', status = 'ACTIVE' WHERE unit_name = '零售业务开发团队';

INSERT INTO org_unit (unit_name, unit_type, parent_unit_name, status, create_user_id, create_user_name, update_user_id, update_user_name)
SELECT '批业务开发团队', 'TEAM', '永隆信息有限公司', 'ACTIVE', '1001', 'SYSTEM', '1001', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM org_unit WHERE unit_name = '批业务开发团队');
UPDATE org_unit SET unit_type = 'TEAM', parent_unit_name = '永隆信息有限公司', status = 'ACTIVE' WHERE unit_name = '批业务开发团队';

INSERT INTO org_unit (unit_name, unit_type, parent_unit_name, status, create_user_id, create_user_name, update_user_id, update_user_name)
SELECT '基础业务开发团队', 'TEAM', '永隆信息有限公司', 'ACTIVE', '1001', 'SYSTEM', '1001', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM org_unit WHERE unit_name = '基础业务开发团队');
UPDATE org_unit SET unit_type = 'TEAM', parent_unit_name = '永隆信息有限公司', status = 'ACTIVE' WHERE unit_name = '基础业务开发团队';

INSERT INTO org_unit (unit_name, unit_type, parent_unit_name, status, create_user_id, create_user_name, update_user_id, update_user_name)
SELECT '数据开发团队', 'TEAM', '永隆信息有限公司', 'ACTIVE', '1001', 'SYSTEM', '1001', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM org_unit WHERE unit_name = '数据开发团队');
UPDATE org_unit SET unit_type = 'TEAM', parent_unit_name = '永隆信息有限公司', status = 'ACTIVE' WHERE unit_name = '数据开发团队';

INSERT INTO org_unit (unit_name, unit_type, parent_unit_name, status, create_user_id, create_user_name, update_user_id, update_user_name)
SELECT '测试团队', 'TEAM', '永隆信息有限公司', 'ACTIVE', '1001', 'SYSTEM', '1001', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM org_unit WHERE unit_name = '测试团队');
UPDATE org_unit SET unit_type = 'TEAM', parent_unit_name = '永隆信息有限公司', status = 'ACTIVE' WHERE unit_name = '测试团队';

INSERT INTO org_unit (unit_name, unit_type, parent_unit_name, status, create_user_id, create_user_name, update_user_id, update_user_name)
SELECT '技术管理团队', 'TEAM', '永隆信息有限公司', 'ACTIVE', '1001', 'SYSTEM', '1001', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM org_unit WHERE unit_name = '技术管理团队');
UPDATE org_unit SET unit_type = 'TEAM', parent_unit_name = '永隆信息有限公司', status = 'ACTIVE' WHERE unit_name = '技术管理团队';

INSERT INTO org_unit (unit_name, unit_type, parent_unit_name, status, create_user_id, create_user_name, update_user_id, update_user_name)
SELECT '人力资源团队', 'TEAM', '永隆信息有限公司', 'ACTIVE', '1001', 'SYSTEM', '1001', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM org_unit WHERE unit_name = '人力资源团队');
UPDATE org_unit SET unit_type = 'TEAM', parent_unit_name = '永隆信息有限公司', status = 'ACTIVE' WHERE unit_name = '人力资源团队';

INSERT INTO org_unit (unit_name, unit_type, parent_unit_name, status, create_user_id, create_user_name, update_user_id, update_user_name)
SELECT '综合管理团队', 'TEAM', '永隆信息有限公司', 'ACTIVE', '1001', 'SYSTEM', '1001', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM org_unit WHERE unit_name = '综合管理团队');
UPDATE org_unit SET unit_type = 'TEAM', parent_unit_name = '永隆信息有限公司', status = 'ACTIVE' WHERE unit_name = '综合管理团队';

INSERT INTO org_unit (unit_name, unit_type, parent_unit_name, status, create_user_id, create_user_name, update_user_id, update_user_name)
SELECT '直属人员', 'CATEGORY', '永隆信息有限公司', 'ACTIVE', '1001', 'SYSTEM', '1001', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM org_unit WHERE unit_name = '直属人员');
UPDATE org_unit SET unit_type = 'CATEGORY', parent_unit_name = '永隆信息有限公司', status = 'ACTIVE' WHERE unit_name = '直属人员';

INSERT INTO org_unit (unit_name, unit_type, parent_unit_name, status, create_user_id, create_user_name, update_user_id, update_user_name)
SELECT '其他', 'CATEGORY', '永隆信息有限公司', 'ACTIVE', '1001', 'SYSTEM', '1001', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM org_unit WHERE unit_name = '其他');
UPDATE org_unit SET unit_type = 'CATEGORY', parent_unit_name = '永隆信息有限公司', status = 'ACTIVE' WHERE unit_name = '其他';

INSERT INTO org_unit (unit_name, unit_type, parent_unit_name, status, create_user_id, create_user_name, update_user_id, update_user_name)
SELECT '零售平台开发室', 'GROUP', '零售业务开发团队', 'ACTIVE', '1001', 'SYSTEM', '1001', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM org_unit WHERE unit_name = '零售平台开发室');
UPDATE org_unit SET unit_type = 'GROUP', parent_unit_name = '零售业务开发团队', status = 'ACTIVE' WHERE unit_name = '零售平台开发室';

INSERT INTO org_unit (unit_name, unit_type, parent_unit_name, status, create_user_id, create_user_name, update_user_id, update_user_name)
SELECT '批发财富业务开发室', 'GROUP', '批业务开发团队', 'ACTIVE', '1001', 'SYSTEM', '1001', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM org_unit WHERE unit_name = '批发财富业务开发室');
UPDATE org_unit SET unit_type = 'GROUP', parent_unit_name = '批业务开发团队', status = 'ACTIVE' WHERE unit_name = '批发财富业务开发室';

INSERT INTO org_unit (unit_name, unit_type, parent_unit_name, status, create_user_id, create_user_name, update_user_id, update_user_name)
SELECT '批发网络应用开发室', 'GROUP', '批业务开发团队', 'ACTIVE', '1001', 'SYSTEM', '1001', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM org_unit WHERE unit_name = '批发网络应用开发室');
UPDATE org_unit SET unit_type = 'GROUP', parent_unit_name = '批业务开发团队', status = 'ACTIVE' WHERE unit_name = '批发网络应用开发室';

INSERT INTO org_unit (unit_name, unit_type, parent_unit_name, status, create_user_id, create_user_name, update_user_id, update_user_name)
SELECT '信贷业务开发室', 'GROUP', '批业务开发团队', 'ACTIVE', '1001', 'SYSTEM', '1001', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM org_unit WHERE unit_name = '信贷业务开发室');
UPDATE org_unit SET unit_type = 'GROUP', parent_unit_name = '批业务开发团队', status = 'ACTIVE' WHERE unit_name = '信贷业务开发室';

INSERT INTO org_unit (unit_name, unit_type, parent_unit_name, status, create_user_id, create_user_name, update_user_id, update_user_name)
SELECT '财资应用开发室', 'GROUP', '批业务开发团队', 'ACTIVE', '1001', 'SYSTEM', '1001', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM org_unit WHERE unit_name = '财资应用开发室');
UPDATE org_unit SET unit_type = 'GROUP', parent_unit_name = '批业务开发团队', status = 'ACTIVE' WHERE unit_name = '财资应用开发室';

INSERT INTO org_unit (unit_name, unit_type, parent_unit_name, status, create_user_id, create_user_name, update_user_id, update_user_name)
SELECT '基础业务开发室', 'GROUP', '基础业务开发团队', 'ACTIVE', '1001', 'SYSTEM', '1001', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM org_unit WHERE unit_name = '基础业务开发室');
UPDATE org_unit SET unit_type = 'GROUP', parent_unit_name = '基础业务开发团队', status = 'ACTIVE' WHERE unit_name = '基础业务开发室';

INSERT INTO org_unit (unit_name, unit_type, parent_unit_name, status, create_user_id, create_user_name, update_user_id, update_user_name)
SELECT '运营业务开发室', 'GROUP', '基础业务开发团队', 'ACTIVE', '1001', 'SYSTEM', '1001', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM org_unit WHERE unit_name = '运营业务开发室');
UPDATE org_unit SET unit_type = 'GROUP', parent_unit_name = '基础业务开发团队', status = 'ACTIVE' WHERE unit_name = '运营业务开发室';

INSERT INTO org_unit (unit_name, unit_type, parent_unit_name, status, create_user_id, create_user_name, update_user_id, update_user_name)
SELECT '合规业务开发室', 'GROUP', '基础业务开发团队', 'ACTIVE', '1001', 'SYSTEM', '1001', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM org_unit WHERE unit_name = '合规业务开发室');
UPDATE org_unit SET unit_type = 'GROUP', parent_unit_name = '基础业务开发团队', status = 'ACTIVE' WHERE unit_name = '合规业务开发室';

INSERT INTO org_unit (unit_name, unit_type, parent_unit_name, status, create_user_id, create_user_name, update_user_id, update_user_name)
SELECT '基础产品开发室', 'GROUP', '基础业务开发团队', 'ACTIVE', '1001', 'SYSTEM', '1001', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM org_unit WHERE unit_name = '基础产品开发室');
UPDATE org_unit SET unit_type = 'GROUP', parent_unit_name = '基础业务开发团队', status = 'ACTIVE' WHERE unit_name = '基础产品开发室';

INSERT INTO org_unit (unit_name, unit_type, parent_unit_name, status, create_user_id, create_user_name, update_user_id, update_user_name)
SELECT '支付业务开发室', 'GROUP', '基础业务开发团队', 'ACTIVE', '1001', 'SYSTEM', '1001', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM org_unit WHERE unit_name = '支付业务开发室');
UPDATE org_unit SET unit_type = 'GROUP', parent_unit_name = '基础业务开发团队', status = 'ACTIVE' WHERE unit_name = '支付业务开发室';

INSERT INTO org_unit (unit_name, unit_type, parent_unit_name, status, create_user_id, create_user_name, update_user_id, update_user_name)
SELECT '办公系统开发室', 'GROUP', '基础业务开发团队', 'ACTIVE', '1001', 'SYSTEM', '1001', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM org_unit WHERE unit_name = '办公系统开发室');
UPDATE org_unit SET unit_type = 'GROUP', parent_unit_name = '基础业务开发团队', status = 'ACTIVE' WHERE unit_name = '办公系统开发室';

INSERT INTO org_unit (unit_name, unit_type, parent_unit_name, status, create_user_id, create_user_name, update_user_id, update_user_name)
SELECT '经营业务开发室', 'GROUP', '数据开发团队', 'ACTIVE', '1001', 'SYSTEM', '1001', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM org_unit WHERE unit_name = '经营业务开发室');
UPDATE org_unit SET unit_type = 'GROUP', parent_unit_name = '数据开发团队', status = 'ACTIVE' WHERE unit_name = '经营业务开发室';

INSERT INTO org_unit (unit_name, unit_type, parent_unit_name, status, create_user_id, create_user_name, update_user_id, update_user_name)
SELECT '业务测试一室', 'GROUP', '测试团队', 'ACTIVE', '1001', 'SYSTEM', '1001', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM org_unit WHERE unit_name = '业务测试一室');
UPDATE org_unit SET unit_type = 'GROUP', parent_unit_name = '测试团队', status = 'ACTIVE' WHERE unit_name = '业务测试一室';

INSERT INTO org_unit (unit_name, unit_type, parent_unit_name, status, create_user_id, create_user_name, update_user_id, update_user_name)
SELECT '业务测试二室', 'GROUP', '测试团队', 'ACTIVE', '1001', 'SYSTEM', '1001', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM org_unit WHERE unit_name = '业务测试二室');
UPDATE org_unit SET unit_type = 'GROUP', parent_unit_name = '测试团队', status = 'ACTIVE' WHERE unit_name = '业务测试二室';

INSERT INTO org_unit (unit_name, unit_type, parent_unit_name, status, create_user_id, create_user_name, update_user_id, update_user_name)
SELECT '部门总经理', 'LEAF', '直属人员', 'ACTIVE', '1001', 'SYSTEM', '1001', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM org_unit WHERE unit_name = '部门总经理');
UPDATE org_unit SET unit_type = 'LEAF', parent_unit_name = '直属人员', status = 'ACTIVE' WHERE unit_name = '部门总经理';

INSERT INTO org_unit (unit_name, unit_type, parent_unit_name, status, create_user_id, create_user_name, update_user_id, update_user_name)
SELECT '分管总', 'LEAF', '直属人员', 'ACTIVE', '1001', 'SYSTEM', '1001', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM org_unit WHERE unit_name = '分管总');
UPDATE org_unit SET unit_type = 'LEAF', parent_unit_name = '直属人员', status = 'ACTIVE' WHERE unit_name = '分管总';

INSERT INTO org_unit (unit_name, unit_type, parent_unit_name, status, create_user_id, create_user_name, update_user_id, update_user_name)
SELECT '外包厂商人员', 'LEAF', '其他', 'ACTIVE', '1001', 'SYSTEM', '1001', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM org_unit WHERE unit_name = '外包厂商人员');
UPDATE org_unit SET unit_type = 'LEAF', parent_unit_name = '其他', status = 'ACTIVE' WHERE unit_name = '外包厂商人员';

INSERT INTO org_unit (unit_name, unit_type, parent_unit_name, status, create_user_id, create_user_name, update_user_id, update_user_name)
SELECT '供应商HR', 'LEAF', '其他', 'ACTIVE', '1001', 'SYSTEM', '1001', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM org_unit WHERE unit_name = '供应商HR');
UPDATE org_unit SET unit_type = 'LEAF', parent_unit_name = '其他', status = 'ACTIVE' WHERE unit_name = '供应商HR';
