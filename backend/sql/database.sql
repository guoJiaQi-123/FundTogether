-- MySQL dump 10.13  Distrib 8.4.8, for macos15.7 (arm64)
--
-- Host: 127.0.0.1    Database: fund_together
-- ------------------------------------------------------
-- Server version	8.4.8

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `fund_account`
--

DROP TABLE IF EXISTS `fund_account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fund_account` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `total_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '总筹款金额',
  `frozen_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '托管中金额(冻结)',
  `available_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '可拨付金额(解冻)',
  `paid_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '已拨付金额',
  `refunded_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '已退款金额',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '账户状态: 0-正常筹款中, 1-结算成功(可拨付), 2-结算失败(需退款)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目资金托管账户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fund_account`
--

LOCK TABLES `fund_account` WRITE;
/*!40000 ALTER TABLE `fund_account` DISABLE KEYS */;
/*!40000 ALTER TABLE `fund_account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fund_ledger`
--

DROP TABLE IF EXISTS `fund_ledger`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fund_ledger` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `user_id` bigint DEFAULT NULL COMMENT '关联用户ID(如果是用户支付/退款)',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联业务单号(支付单号/拨付单号)',
  `type` tinyint NOT NULL COMMENT '交易类型: 1-用户支付(入账), 2-失败退款(出账), 3-阶段拨付(出账)',
  `amount` decimal(10,2) NOT NULL COMMENT '交易金额',
  `balance` decimal(10,2) NOT NULL COMMENT '交易后项目账户总余额',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '交易备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '交易时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资金交易流水表(透明可追溯)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fund_ledger`
--

LOCK TABLES `fund_ledger` WRITE;
/*!40000 ALTER TABLE `fund_ledger` DISABLE KEYS */;
/*!40000 ALTER TABLE `fund_ledger` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fund_payout_phase`
--

DROP TABLE IF EXISTS `fund_payout_phase`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fund_payout_phase` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `phase_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '阶段名称(如: 首期30%, 中期40%, 尾款30%)',
  `phase_index` int NOT NULL COMMENT '阶段序号(1,2,3...)',
  `payout_ratio` decimal(5,2) NOT NULL COMMENT '拨付比例(百分比)',
  `payout_amount` decimal(10,2) NOT NULL COMMENT '应拨付金额',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-待申请, 1-审核中, 2-已拨付, 3-已驳回',
  `audit_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审核/驳回原因',
  `apply_time` datetime DEFAULT NULL COMMENT '申请时间',
  `pay_time` datetime DEFAULT NULL COMMENT '拨付时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目分阶段拨付表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fund_payout_phase`
--

LOCK TABLES `fund_payout_phase` WRITE;
/*!40000 ALTER TABLE `fund_payout_phase` DISABLE KEYS */;
/*!40000 ALTER TABLE `fund_payout_phase` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `funding_ledger`
--

DROP TABLE IF EXISTS `funding_ledger`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `funding_ledger` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint DEFAULT NULL COMMENT '关联项目ID(提现等操作可为空)',
  `order_id` bigint DEFAULT NULL COMMENT '关联订单ID(如果是用户支付/退款)',
  `user_id` bigint NOT NULL COMMENT '交易涉及的用户ID(支持者或发起人)',
  `amount` decimal(10,2) NOT NULL COMMENT '交易金额',
  `type` tinyint NOT NULL COMMENT '交易类型: 1-用户支付, 2-平台退款, 3-阶段拨付给发起人, 4-发起人提现',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-处理中, 1-成功, 2-失败',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注/交易流水号',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资金交易流水表(资金隔离/透明可追溯)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `funding_ledger`
--

LOCK TABLES `funding_ledger` WRITE;
/*!40000 ALTER TABLE `funding_ledger` DISABLE KEYS */;
/*!40000 ALTER TABLE `funding_ledger` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project`
--

DROP TABLE IF EXISTS `project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sponsor_id` bigint NOT NULL COMMENT '发起人ID',
  `category_id` bigint DEFAULT NULL COMMENT '分类ID',
  `title` varchar(100) NOT NULL COMMENT '项目标题',
  `summary` varchar(255) NOT NULL COMMENT '项目简介',
  `cover_image` varchar(255) NOT NULL COMMENT '项目封面',
  `video_url` varchar(255) DEFAULT NULL COMMENT '项目视频',
  `content` longtext NOT NULL COMMENT '项目详情',
  `target_amount` decimal(10,2) NOT NULL COMMENT '目标金额',
  `current_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '当前已筹金额',
  `supporter_count` int NOT NULL DEFAULT '0' COMMENT '支持人数',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-待审核, 1-筹款中, 2-已驳回, 3-已取消, 4-已下架, 5-已成功, 6-筹款失败',
  `heat` int NOT NULL DEFAULT '0' COMMENT '热度',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='众筹项目表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project`
--

LOCK TABLES `project` WRITE;
/*!40000 ALTER TABLE `project` DISABLE KEYS */;
/*!40000 ALTER TABLE `project` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_category`
--

DROP TABLE IF EXISTS `project_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_category`
--

LOCK TABLES `project_category` WRITE;
/*!40000 ALTER TABLE `project_category` DISABLE KEYS */;
INSERT INTO `project_category` VALUES (1,'科技创新',1,'2026-03-25 00:00:00','2026-03-25 00:00:00',0),(2,'公益助农',2,'2026-03-25 00:00:00','2026-03-25 00:00:00',0),(3,'文化艺术',3,'2026-03-25 00:00:00','2026-03-25 00:00:00',0);
/*!40000 ALTER TABLE `project_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_payout`
--

DROP TABLE IF EXISTS `project_payout`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project_payout` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `sponsor_id` bigint NOT NULL COMMENT '发起人ID',
  `stage` tinyint NOT NULL COMMENT '拨付阶段: 1-首期, 2-中期, 3-尾期等',
  `amount` decimal(10,2) NOT NULL COMMENT '拨付金额',
  `payout_ratio` decimal(4,2) NOT NULL COMMENT '拨付比例(如0.30代表30%)',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-待拨付, 1-已拨付',
  `condition_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '拨付条件说明',
  `payout_time` datetime DEFAULT NULL COMMENT '实际拨付时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目分阶段拨付表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_payout`
--

LOCK TABLES `project_payout` WRITE;
/*!40000 ALTER TABLE `project_payout` DISABLE KEYS */;
/*!40000 ALTER TABLE `project_payout` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_reward`
--

DROP TABLE IF EXISTS `project_reward`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project_reward` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `amount` decimal(10,2) NOT NULL COMMENT '支持金额',
  `content` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '回报内容描述',
  `reward_count` int NOT NULL DEFAULT '-1' COMMENT '限制名额(-1表示不限量)',
  `delivery_time` int NOT NULL COMMENT '预计发放时间(项目成功后多少天内)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目支持回报档位表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_reward`
--

LOCK TABLES `project_reward` WRITE;
/*!40000 ALTER TABLE `project_reward` DISABLE KEYS */;
/*!40000 ALTER TABLE `project_reward` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_update`
--

DROP TABLE IF EXISTS `project_update`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project_update` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '动态标题',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '动态内容',
  `images` json DEFAULT NULL COMMENT '图片URL数组(JSON格式)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目动态进展表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_update`
--

LOCK TABLES `project_update` WRITE;
/*!40000 ALTER TABLE `project_update` DISABLE KEYS */;
/*!40000 ALTER TABLE `project_update` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `support_order`
--

DROP TABLE IF EXISTS `support_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `support_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(64) NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL COMMENT '支持者ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `amount` decimal(10,2) NOT NULL COMMENT '支持金额',
  `message` varchar(255) DEFAULT NULL COMMENT '留言',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0-待支付 1-已支付 2-已取消 3-已退款',
  `pay_channel` varchar(32) DEFAULT NULL COMMENT '支付渠道',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支持订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `support_order`
--

LOCK TABLES `support_order` WRITE;
/*!40000 ALTER TABLE `support_order` DISABLE KEYS */;
/*!40000 ALTER TABLE `support_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_notice`
--

DROP TABLE IF EXISTS `sys_notice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_notice` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告标题',
  `content` longtext COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告内容(富文本)',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-未发布, 1-已发布',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='平台系统公告表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_notice`
--

LOCK TABLES `sys_notice` WRITE;
/*!40000 ALTER TABLE `sys_notice` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_notice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `account` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录账号(手机号/邮箱)',
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '加密密码',
  `nickname` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户昵称',
  `avatar` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像URL',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
  `email` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `role` tinyint NOT NULL DEFAULT '1' COMMENT '角色: 1-普通支持者, 2-项目发起人, 3-系统管理员',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 1-正常, 0-禁用',
  `disable_reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '禁用原因',
  `bio` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '个人简介',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除, 1-已删除',
  `education` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '学历',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `company` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '公司',
  `profession` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '职业',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '所在地',
  `gender` tinyint DEFAULT '0' COMMENT '性别: 0-未知, 1-男, 2-女',
  `balance` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '账户余额',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account` (`account`)
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户基础信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` VALUES (1,'admin','$2a$10$QQLCe751k6pvUwkNXHACxelYan2J3Y5cGM4BOuZlB85foy.G0Mq.y','郭家旗','http://localhost:8080/uploads/9346fc01-2780-4186-a52a-4008ee1edf94.png','13800138000','admin@test.com',3,1,NULL,'hhhh','2026-03-24 23:28:47','2026-04-08 23:52:03',0,'本科','2003-11-22','','','太原',1,0.00),(13,'admin13@test.com','$2a$10$X8mR0fN/Qd/fJ01s40QOPO/8H1/9X.w5N8E/7x4XyE/wR1m6E/O2e','Admin 13',NULL,NULL,NULL,3,1,NULL,NULL,'2026-03-25 00:39:47','2026-03-25 00:39:47',0,NULL,NULL,NULL,NULL,NULL,0,0.00),(39,'admin39','$2a$10$QQLCe751k6pvUwkNXHACxelYan2J3Y5cGM4BOuZlB85foy.G0Mq.y','超级管理员',NULL,'13800000039',NULL,3,1,NULL,NULL,'2026-03-26 15:08:42','2026-03-26 15:08:42',0,NULL,NULL,NULL,NULL,NULL,0,0.00);
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user_level`
--

DROP TABLE IF EXISTS `sys_user_level`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_level` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `level_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `min_amount` decimal(10,2) NOT NULL COMMENT '门槛金额',
  `icon` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图标或颜色',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  `max_amount` decimal(10,2) DEFAULT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户投资等级表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user_level`
--

LOCK TABLES `sys_user_level` WRITE;
/*!40000 ALTER TABLE `sys_user_level` DISABLE KEYS */;
INSERT INTO `sys_user_level` VALUES (1,'青铜投资人',0.00,'#cd7f32','2026-04-08 17:43:52','2026-04-08 17:43:52',0,NULL,NULL),(2,'白银投资人',1000.00,'#c0c0c0','2026-04-08 17:43:52','2026-04-08 17:43:52',0,NULL,NULL),(3,'黄金投资人',5000.00,'#ffd700','2026-04-08 17:43:52','2026-04-08 17:43:52',0,NULL,NULL),(4,'钻石投资人',10000.00,'#b9f2ff','2026-04-08 17:43:52','2026-04-08 17:43:52',0,NULL,NULL),(5,'黑金投资人',50000.00,'#000000','2026-04-08 17:43:52','2026-04-08 17:43:52',0,NULL,NULL);
/*!40000 ALTER TABLE `sys_user_level` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user_message`
--

DROP TABLE IF EXISTS `sys_user_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '接收方用户ID(0代表全站广播)',
  `type` tinyint NOT NULL COMMENT '消息类型: 1-系统公告, 2-项目动态, 3-评论回复, 4-订单及状态通知',
  `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息标题',
  `content` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容简述',
  `related_id` bigint DEFAULT NULL COMMENT '关联业务ID(公告ID/项目ID/订单ID等)',
  `is_read` tinyint NOT NULL DEFAULT '0' COMMENT '是否已读: 0-未读, 1-已读',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户站内信/通知消息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user_message`
--

LOCK TABLES `sys_user_message` WRITE;
/*!40000 ALTER TABLE `sys_user_message` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_user_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_auth_info`
--

DROP TABLE IF EXISTS `user_auth_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_auth_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `real_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '真实姓名',
  `id_card` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '身份证号',
  `id_card_front` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '身份证正面照片URL',
  `id_card_back` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '身份证反面照片URL',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '审核状态: 0-待审核, 1-审核通过, 2-审核驳回',
  `audit_reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '驳回原因',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户实名认证表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_auth_info`
--

LOCK TABLES `user_auth_info` WRITE;
/*!40000 ALTER TABLE `user_auth_info` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_auth_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_comment`
--

DROP TABLE IF EXISTS `user_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父评论ID(用于回复评论, 0为直接评论项目)',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论/留言内容',
  `likes_count` int NOT NULL DEFAULT '0' COMMENT '点赞数',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 1-正常, 0-发起人或管理员删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户项目评论及留言表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_comment`
--

LOCK TABLES `user_comment` WRITE;
/*!40000 ALTER TABLE `user_comment` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_like`
--

DROP TABLE IF EXISTS `user_like`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_like` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `target_type` tinyint NOT NULL COMMENT '目标类型: 1-项目, 2-评论',
  `target_id` bigint NOT NULL COMMENT '目标ID(项目ID或评论ID)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`,`target_type`,`target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户点赞记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_like`
--

LOCK TABLES `user_like` WRITE;
/*!40000 ALTER TABLE `user_like` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_like` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_payment_method`
--

DROP TABLE IF EXISTS `user_payment_method`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_payment_method` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `type` tinyint NOT NULL COMMENT '类型: 1-微信, 2-支付宝, 3-银行卡',
  `account` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '账号(微信号/支付宝号/银行卡号)',
  `name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '真实姓名(如持卡人)',
  `bank_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '银行名称(银行卡时填写)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户支付方式绑定表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_payment_method`
--

LOCK TABLES `user_payment_method` WRITE;
/*!40000 ALTER TABLE `user_payment_method` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_payment_method` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_recharge_order`
--

DROP TABLE IF EXISTS `user_recharge_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_recharge_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '充值单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `amount` decimal(10,2) NOT NULL COMMENT '充值金额',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-待支付, 1-已支付',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户充值订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_recharge_order`
--

LOCK TABLES `user_recharge_order` WRITE;
/*!40000 ALTER TABLE `user_recharge_order` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_recharge_order` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-08 23:52:56
