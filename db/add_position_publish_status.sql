ALTER TABLE recruitment_request ADD COLUMN position_publish_status VARCHAR(20) DEFAULT 'UNPUBLISHED' COMMENT '岗位发布状态：UNPUBLISHED-未发布，PUBLISHED-已发布';

-- 更新现有记录的发布状态为未发布
UPDATE recruitment_request SET position_publish_status = 'UNPUBLISHED' WHERE position_publish_status IS NULL;