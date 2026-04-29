CREATE TABLE IF NOT EXISTS `project_recommend` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `project_id` BIGINT NOT NULL COMMENT '关联项目ID',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` INT DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`),
  UNIQUE KEY `uk_project_id_deleted` (`project_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目推荐管理表';
