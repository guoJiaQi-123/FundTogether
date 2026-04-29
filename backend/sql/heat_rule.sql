-- 热度规则配置表
CREATE TABLE IF NOT EXISTS `heat_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `factor_key` varchar(50) NOT NULL COMMENT '因素标识',
  `factor_name` varchar(100) NOT NULL COMMENT '因素显示名称',
  `weight` decimal(5,2) NOT NULL DEFAULT '1.00' COMMENT '权重(0.00-100.00)',
  `enabled` tinyint NOT NULL DEFAULT '1' COMMENT '是否启用: 0-禁用, 1-启用',
  `description` varchar(255) DEFAULT NULL COMMENT '因素描述',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_factor_key` (`factor_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='热度规则配置表';

-- 初始化默认规则
INSERT INTO `heat_rule` (`factor_key`, `factor_name`, `weight`, `enabled`, `description`) VALUES
('supporter_count', '支持人数', 2.00, 1, '每个支持者贡献的分数'),
('funding_amount', '筹款金额', 0.10, 1, '每100元筹款额贡献的分数'),
('funding_progress', '筹款进度', 1.00, 1, '筹款完成百分比贡献的分数'),
('favorite_count', '收藏人数', 1.50, 1, '每个收藏者贡献的分数'),
('comment_count', '评论数量', 0.50, 1, '每条评论贡献的分数'),
('time_decay', '时间衰减', 1.00, 1, '项目上线天数衰减因子，越新分数越高');
