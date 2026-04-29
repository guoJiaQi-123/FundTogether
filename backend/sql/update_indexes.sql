USE fund_together;

-- support_order
ALTER TABLE support_order ADD INDEX idx_user_id (user_id);
ALTER TABLE support_order ADD INDEX idx_project_id (project_id);
ALTER TABLE support_order ADD INDEX idx_status (status);

-- project
ALTER TABLE project ADD INDEX idx_status (status);
ALTER TABLE project ADD INDEX idx_sponsor_id (sponsor_id);
ALTER TABLE project ADD INDEX idx_category_id (category_id);

-- funding_ledger
ALTER TABLE funding_ledger ADD INDEX idx_type_status (type, status);

-- sys_user
ALTER TABLE sys_user ADD INDEX idx_role (role);
ALTER TABLE sys_user ADD INDEX idx_status (status);

