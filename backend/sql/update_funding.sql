CREATE TABLE `funding_ledger` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint DEFAULT NULL COMMENT '关联项目ID(提现等操作可为空)',
  `order_id` bigint DEFAULT NULL COMMENT '关联订单ID(如果是用户支付/退款)',
  `user_id` bigint NOT NULL COMMENT '交易涉及的用户ID(支持者或发起人)',
  `amount` decimal(10,2) NOT NULL COMMENT '交易金额',
  `type` tinyint NOT NULL COMMENT '交易类型: 1-用户支付, 2-平台退款, 3-阶段拨付给发起人',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-处理中, 1-成功, 2-失败',
  `remark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注/交易流水号',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资金交易流水表(资金隔离/透明可追溯)';

CREATE TABLE `project_payout` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `sponsor_id` bigint NOT NULL COMMENT '发起人ID',
  `stage` tinyint NOT NULL COMMENT '拨付阶段: 1-首期, 2-中期, 3-尾期等',
  `amount` decimal(10,2) NOT NULL COMMENT '拨付金额',
  `payout_ratio` decimal(4,2) NOT NULL COMMENT '拨付比例(如0.30代表30%)',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-待拨付, 1-已拨付',
  `condition_desc` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '拨付条件说明',
  `payout_time` datetime DEFAULT NULL COMMENT '实际拨付时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目分阶段拨付表';

ALTER TABLE `project` MODIFY COLUMN `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-待审核, 1-筹款中, 2-已驳回, 3-已取消, 4-已下架, 5-已成功, 6-筹款失败';
ALTER TABLE `support_order` MODIFY COLUMN `status` tinyint NOT NULL DEFAULT '0' COMMENT '0-待支付 1-已支付 2-已取消 3-已退款';
