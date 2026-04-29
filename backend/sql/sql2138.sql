-- ----------------------------
-- 测试数据脚本 (为功能需求 39-59 生成)
-- 包含用户、项目、分类、订单、回报档位、公告等数据
-- ----------------------------

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1. 插入测试用户 (管理员、发起人、支持者)
INSERT INTO `sys_user` (`id`, `account`, `password`, `nickname`, `phone`, `role`, `status`) VALUES
(39, 'admin39', '$2a$10$QQLCe751k6pvUwkNXHACxelYan2J3Y5cGM4BOuZlB85foy.G0Mq.y', '超级管理员', '13800000039', 3, 1),
(40, 'sponsor40', '$2a$10$QQLCe751k6pvUwkNXHACxelYan2J3Y5cGM4BOuZlB85foy.G0Mq.y', '明星发起人', '13800000040', 2, 1),
(41, 'user41', '$2a$10$QQLCe751k6pvUwkNXHACxelYan2J3Y5cGM4BOuZlB85foy.G0Mq.y', '热情支持者', '13800000041', 1, 1),
(42, 'baduser42', '$2a$10$QQLCe751k6pvUwkNXHACxelYan2J3Y5cGM4BOuZlB85foy.G0Mq.y', '违规用户', '13800000042', 1, 0); -- 已禁用

-- 2. 插入项目分类
INSERT INTO `project_category` (`id`, `name`, `sort`) VALUES
(1, '科技创新', 1),
(2, '公益助学', 2),
(3, '文化艺术', 3);

-- 3. 插入测试项目
INSERT INTO `project` (`id`, `sponsor_id`, `category_id`, `title`, `summary`, `cover_image`, `content`, `target_amount`, `current_amount`, `supporter_count`, `end_time`, `status`, `heat`) VALUES
(39, 40, 1, '全息投影智能音箱', '未来的桌面伴侣，支持AI语音交互。', 'https://images.unsplash.com/photo-1544413660-299165566b1d', '<p>全息投影技术...</p>', 100000.00, 75000.00, 150, '2026-12-31 23:59:59', 1, 9500),
(40, 40, 2, '山区净水计划', '为偏远山区学校安装净水设备。', 'https://images.unsplash.com/photo-1504814532849-cff240bbc503', '<p>水是生命之源...</p>', 50000.00, 50000.00, 500, '2026-05-01 23:59:59', 5, 8200),
(41, 40, 3, '独立电影《远方》', '一部关于寻找自我的公路电影。', 'https://images.unsplash.com/photo-1485846234645-a62644f84728', '<p>电影简介...</p>', 200000.00, 10000.00, 20, '2026-10-01 23:59:59', 1, 3000);

-- 4. 插入项目回报档位
INSERT INTO `project_reward` (`id`, `project_id`, `amount`, `content`, `reward_count`, `delivery_time`) VALUES
(1, 39, 99.00, '感谢信 + 专属电子壁纸', -1, 10),
(2, 39, 499.00, '全息投影智能音箱一台 (早鸟价)', 500, 30),
(3, 40, 50.00, '公益感谢证书', -1, 5),
(4, 41, 100.00, '电影首映礼门票一张', 200, 60);

-- 5. 插入订单数据 (模拟不同支付渠道)
INSERT INTO `support_order` (`id`, `order_no`, `user_id`, `project_id`, `amount`, `message`, `status`, `pay_channel`, `pay_time`) VALUES
(39, 'ORD20260339', 41, 39, 499.00, '支持科技创新！', 1, 'WECHAT', '2026-03-20 10:00:00'),
(40, 'ORD20260340', 41, 40, 50.00, '尽一份绵薄之力。', 1, 'ALIPAY', '2026-03-21 11:00:00'),
(41, 'ORD20260341', 41, 41, 100.00, '期待电影上映！', 1, 'BANK_CARD', '2026-03-22 12:00:00');

-- 6. 插入系统公告
INSERT INTO `sys_notice` (`id`, `title`, `content`, `status`) VALUES
(1, '平台春季筹款活动正式开启', '<p>欢迎参与2026春季筹款季，多重好礼等你来拿！</p>', 1),
(2, '关于规范项目审核标准的通知', '<p>为了平台健康发展，我们将进一步加强项目资质审核...</p>', 1);

-- 7. 插入用户点赞与评论
INSERT INTO `user_comment` (`id`, `user_id`, `project_id`, `content`, `likes_count`, `status`) VALUES
(39, 41, 39, '这个产品看起来太酷了，什么时候发货？', 10, 1),
(40, 40, 39, '感谢支持！预计众筹结束后30天内发货。', 5, 1);

INSERT INTO `user_like` (`id`, `user_id`, `target_type`, `target_id`) VALUES
(1, 41, 2, 40),
(2, 40, 2, 39);

SET FOREIGN_KEY_CHECKS = 1;
