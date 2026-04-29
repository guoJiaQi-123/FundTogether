-- 如果表不存在可以先执行 database.sql
USE `fund_together`;

-- 插入一条管理员测试数据
INSERT INTO `sys_user` (`account`, `password`, `nickname`, `phone`, `email`, `role`, `status`) 
VALUES ('admin', '$2a$10$xyz', '系统管理员', '13800138000', 'admin@test.com', 3, 1);

-- 插入一条普通支持者测试数据
INSERT INTO `sys_user` (`account`, `password`, `nickname`, `phone`, `email`, `role`, `status`) 
VALUES ('user1', '$2a$10$xyz', '普通用户A', '13800138001', 'user1@test.com', 1, 1);

-- 插入一条项目发起人测试数据
INSERT INTO `sys_user` (`account`, `password`, `nickname`, `phone`, `email`, `role`, `status`) 
VALUES ('sponsor1', '$2a$10$xyz', '发起人A', '13800138002', 'sponsor1@test.com', 2, 1);

-- 注意：由于系统采用了 BCryptPasswordEncoder，真实的密码加密串应该通过系统生成。
-- 测试时请通过前端注册页面注册新账号来测试完整的注册流程，这也会将新注册用户写入该表中。
