# Findings

- 当前流程中心仅支持 `RECRUITMENT_REQUEST` 与 `INTERVIEW_EVALUATION` 两种流程码，新增到岗确认需扩展第三条流程线。
- 现有入场管理表 `entry_record` 已建立，字段可承接流程完成后的回写：`actual_entry_date`、`arrival_status`。
- 候选人与供应商信息可从 `resume` 表取到，其中 `create_user_id/create_user_name/supplier_name` 可用于锁定供应商HR审批人。
- 组织架构已有 `org_unit`，室经理/团队经理识别逻辑可复用 `UserMapper + role` 查询。
- 供应商数据在 `sys_supplier` / `sys_supplier_hr`，目前已有供应商与绑定用户查询能力。
