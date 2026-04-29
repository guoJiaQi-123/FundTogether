USE `fund_together`;

-- Create tables if not exists
DROP TABLE IF EXISTS `project`;
CREATE TABLE `project` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `sponsor_id` bigint(20) NOT NULL COMMENT '发起人ID',
  `category_id` bigint(20) DEFAULT NULL COMMENT '分类ID',
  `title` varchar(100) NOT NULL COMMENT '项目标题',
  `summary` varchar(255) NOT NULL COMMENT '项目简介',
  `cover_image` varchar(255) NOT NULL COMMENT '项目封面',
  `video_url` varchar(255) DEFAULT NULL COMMENT '项目视频',
  `content` longtext NOT NULL COMMENT '项目详情',
  `target_amount` decimal(10,2) NOT NULL COMMENT '目标金额',
  `current_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '当前已筹金额',
  `supporter_count` int(11) NOT NULL DEFAULT '0' COMMENT '支持人数',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态: 0-待审核, 1-筹款中, 2-已驳回, 3-已取消, 4-已下架, 5-已完成',
  `heat` int(11) NOT NULL DEFAULT '0' COMMENT '热度',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='众筹项目表';

DROP TABLE IF EXISTS `support_order`;
CREATE TABLE `support_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_no` varchar(64) NOT NULL COMMENT '订单号',
  `user_id` bigint(20) NOT NULL COMMENT '支持者ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `amount` decimal(10,2) NOT NULL COMMENT '支持金额',
  `message` varchar(255) DEFAULT NULL COMMENT '留言',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '0-待支付 1-已支付 2-已取消',
  `pay_channel` varchar(32) DEFAULT NULL COMMENT '支付渠道',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支持订单表';

-- Insert some test data for projects
INSERT INTO `project` (`sponsor_id`, `category_id`, `title`, `summary`, `cover_image`, `content`, `target_amount`, `current_amount`, `supporter_count`, `start_time`, `end_time`, `status`, `heat`) VALUES
(3, 1, '智能助眠眼罩', '利用AI白噪音与温感助眠的创新眼罩', 'https://images.unsplash.com/photo-1541781774459-bb2af2f05b55', '<p>这是一款革命性的智能助眠眼罩...</p>', 50000.00, 15000.00, 120, '2023-10-01 10:00:00', '2023-11-30 23:59:59', 1, 800),
(3, 2, '乡村儿童图书角', '为偏远地区儿童建立阅读图书角', 'https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8', '<p>每人献出一点爱，点亮乡村儿童的阅读梦...</p>', 10000.00, 12000.00, 300, '2023-09-01 00:00:00', '2023-10-31 23:59:59', 1, 1500),
(3, 1, '便携式咖啡机(待审核)', '随时随地享受现磨咖啡', 'https://images.unsplash.com/photo-1511920170033-f8396924c348', '<p>超便携设计的电动咖啡机...</p>', 30000.00, 0.00, 0, NULL, '2024-01-31 23:59:59', 0, 0);

-- Insert some test data for support_orders
INSERT INTO `support_order` (`order_no`, `user_id`, `project_id`, `amount`, `message`, `status`, `pay_channel`, `pay_time`) VALUES
('ORD20231015001', 2, 1, 100.00, '期待早日发货！', 1, 'ALIPAY', '2023-10-15 12:30:00'),
('ORD20231016002', 2, 2, 50.00, '支持乡村教育', 1, 'WECHAT', '2023-10-16 09:15:00');
