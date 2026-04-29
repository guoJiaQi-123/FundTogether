USE `fund_together`;

-- Insert some test data for requirements 11-20

-- Users
INSERT IGNORE INTO sys_user (id, account, password, nickname, role, status, created_at, updated_at) VALUES 
(11, 'sponsor11@test.com', '$2a$10$X8mR0fN/Qd/fJ01s40QOPO/8H1/9X.w5N8E/7x4XyE/wR1m6E/O2e', 'Sponsor 11', 2, 1, NOW(), NOW()),
(12, 'supporter12@test.com', '$2a$10$X8mR0fN/Qd/fJ01s40QOPO/8H1/9X.w5N8E/7x4XyE/wR1m6E/O2e', 'Supporter 12', 1, 1, NOW(), NOW()),
(13, 'admin13@test.com', '$2a$10$X8mR0fN/Qd/fJ01s40QOPO/8H1/9X.w5N8E/7x4XyE/wR1m6E/O2e', 'Admin 13', 3, 1, NOW(), NOW());

-- Projects
INSERT IGNORE INTO project (id, sponsor_id, category_id, title, summary, content, cover_image, target_amount, status, end_time, created_at, updated_at) VALUES
(11, 11, 1, '待审核项目11', '这是一个待审核项目，用于测试取消和审核', '<p>详细内容</p>', 'https://example.com/cover11.jpg', 10000.00, 0, DATE_ADD(NOW(), INTERVAL 30 DAY), NOW(), NOW()),
(12, 11, 1, '筹款中项目12', '这是一个筹款中的项目，用于测试下架和支持者名单', '<p>详细内容</p>', 'https://example.com/cover12.jpg', 20000.00, 1, DATE_ADD(NOW(), INTERVAL 30 DAY), NOW(), NOW());

-- Support Orders
INSERT IGNORE INTO support_order (id, order_no, user_id, project_id, amount, message, status, created_at, updated_at) VALUES
(11, 'ORDER202603251020001', 12, 12, 100.00, '支持一下！', 1, NOW(), NOW()),
(12, 'ORDER202603251020002', 12, 12, 200.00, '加油！', 1, NOW(), NOW());

-- User Payment Methods
INSERT IGNORE INTO user_payment_method (id, user_id, type, account, name, bank_name, created_at, updated_at) VALUES
(11, 12, 1, 'wx_supporter12', '张三', NULL, NOW(), NOW()),
(12, 12, 3, '6222020000000000000', '张三', '招商银行', NOW(), NOW());

