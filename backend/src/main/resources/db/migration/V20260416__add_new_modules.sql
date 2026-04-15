-- 项目分类表: 添加缺失的 icon 列
SET @dbname = DATABASE();
SET @tablename = 'project_category';
SET @columnname = 'icon';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
  'SELECT 1',
  'ALTER TABLE `project_category` ADD COLUMN `icon` VARCHAR(255) DEFAULT NULL COMMENT ''分类图标URL'' AFTER `name`'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 初始化分类数据 (仅当不存在时)
INSERT INTO `project_category` (`name`, `icon`, `sort`)
SELECT '科技', NULL, 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `project_category` WHERE `name` = '科技');
INSERT INTO `project_category` (`name`, `icon`, `sort`)
SELECT '设计', NULL, 2 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `project_category` WHERE `name` = '设计');
INSERT INTO `project_category` (`name`, `icon`, `sort`)
SELECT '影视', NULL, 3 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `project_category` WHERE `name` = '影视');
INSERT INTO `project_category` (`name`, `icon`, `sort`)
SELECT '音乐', NULL, 4 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `project_category` WHERE `name` = '音乐');
INSERT INTO `project_category` (`name`, `icon`, `sort`)
SELECT '游戏', NULL, 5 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `project_category` WHERE `name` = '游戏');
INSERT INTO `project_category` (`name`, `icon`, `sort`)
SELECT '出版', NULL, 6 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `project_category` WHERE `name` = '出版');
INSERT INTO `project_category` (`name`, `icon`, `sort`)
SELECT '公益', NULL, 7 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `project_category` WHERE `name` = '公益');
INSERT INTO `project_category` (`name`, `icon`, `sort`)
SELECT '农业', NULL, 8 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `project_category` WHERE `name` = '农业');
INSERT INTO `project_category` (`name`, `icon`, `sort`)
SELECT '其他', NULL, 99 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `project_category` WHERE `name` = '其他');

-- 新增表: 提现订单
CREATE TABLE IF NOT EXISTS `withdrawal_order` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_no` VARCHAR(64) NOT NULL COMMENT '提现单号',
    `user_id` BIGINT NOT NULL COMMENT '发起人ID',
    `amount` DECIMAL(12,2) NOT NULL COMMENT '提现金额',
    `type` TINYINT NOT NULL COMMENT '1-支付宝 2-银行卡',
    `account_name` VARCHAR(100) DEFAULT NULL COMMENT '账户名',
    `account_no` VARCHAR(100) NOT NULL COMMENT '账号',
    `bank_name` VARCHAR(100) DEFAULT NULL COMMENT '银行名称',
    `status` TINYINT DEFAULT 0 COMMENT '0-待审核 1-已通过 2-已驳回',
    `reject_reason` VARCHAR(255) DEFAULT NULL COMMENT '驳回原因',
    `process_time` DATETIME DEFAULT NULL COMMENT '处理时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提现订单';

-- 新增表: 用户收藏
CREATE TABLE IF NOT EXISTS `user_favorite` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `project_id` BIGINT NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    UNIQUE INDEX `uk_user_project` (`user_id`, `project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏';

-- 新增表: 项目举报
CREATE TABLE IF NOT EXISTS `project_report` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `reporter_id` BIGINT NOT NULL COMMENT '举报人ID',
    `project_id` BIGINT NOT NULL COMMENT '被举报项目ID',
    `reason` TEXT NOT NULL COMMENT '举报原因',
    `status` TINYINT DEFAULT 0 COMMENT '0-待处理 1-已处理',
    `handle_result` VARCHAR(255) DEFAULT NULL COMMENT '处理结果',
    `handler_id` BIGINT DEFAULT NULL COMMENT '处理人ID',
    `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    INDEX `idx_project_id` (`project_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目举报';

-- 新增表: 管理员操作日志
CREATE TABLE IF NOT EXISTS `admin_operation_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `admin_id` BIGINT NOT NULL,
    `admin_name` VARCHAR(100) DEFAULT NULL,
    `operation` VARCHAR(100) NOT NULL COMMENT '操作类型',
    `target` VARCHAR(255) DEFAULT NULL COMMENT '操作对象',
    `detail` TEXT DEFAULT NULL COMMENT '操作详情',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_admin_id` (`admin_id`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员操作日志';
