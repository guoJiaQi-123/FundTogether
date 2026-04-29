-- Update sys_user table with additional profile fields
ALTER TABLE sys_user
ADD COLUMN `education` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '学历',
ADD COLUMN `birthday` date DEFAULT NULL COMMENT '生日',
ADD COLUMN `company` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '公司',
ADD COLUMN `profession` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '职业',
ADD COLUMN `location` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '所在地',
ADD COLUMN `gender` tinyint DEFAULT '0' COMMENT '性别: 0-未知, 1-男, 2-女';
