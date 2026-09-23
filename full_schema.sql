-- Database Schema for Appointment Management System (AMS) - Full Reconstruction
-- Generated based on DB_SCHEMA_v2.sql and backend/sql/* patches
-- Target: MySQL 8.0+

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================
-- 1. System Tables (RBAC & Store & Logs)
-- =============================================

-- Table: sys_store (店铺表)
DROP TABLE IF EXISTS `sys_store`;
CREATE TABLE `sys_store` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` VARCHAR(100) NOT NULL COMMENT '店铺名称',
  `address` VARCHAR(255) NOT NULL COMMENT '地址',
  `phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1:营业中, 0:停业',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='店铺表';

-- Table: sys_user (用户表)
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键, 自增',
  `store_id` BIGINT(20) DEFAULT NULL COMMENT '所属门店ID (超管为空)',
  `username` VARCHAR(50) NOT NULL COMMENT '登录账号/手机号',
  `password` VARCHAR(128) NOT NULL COMMENT 'MD5(pwd+salt) 密文',
  `salt` VARCHAR(32) NOT NULL COMMENT '随机盐值 (16位+)',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1:正常, 0:禁用',
  `membership_level` VARCHAR(20) DEFAULT 'NORMAL' COMMENT '会员等级 (NORMAL, GOLD, PLATINUM)',
  `discount_rate` DECIMAL(3,2) DEFAULT 1.00 COMMENT '折扣率',
  `violation_count` INT(11) DEFAULT 0 COMMENT '总违约次数',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_store_id` (`store_id`),
  CONSTRAINT `fk_user_store` FOREIGN KEY (`store_id`) REFERENCES `sys_store` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- Table: sys_role (角色表)
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `code` VARCHAR(20) NOT NULL COMMENT '角色编码 (ROLE_ADMIN)',
  `name` VARCHAR(20) NOT NULL COMMENT '角色名称 (管理员)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- Table: sys_permission (权限表)
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `code` VARCHAR(50) NOT NULL COMMENT '权限标识 (user:add)',
  `name` VARCHAR(50) NOT NULL COMMENT '权限名称',
  `type` VARCHAR(10) NOT NULL COMMENT 'MENU/BUTTON',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- Table: sys_user_role (用户角色关联表)
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `role_id` BIGINT(20) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`, `role_id`),
  KEY `idx_role_id` (`role_id`),
  CONSTRAINT `fk_ur_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ur_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- Table: sys_role_permission (角色权限关联表)
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission` (
  `role_id` BIGINT(20) NOT NULL COMMENT '角色ID',
  `perm_id` BIGINT(20) NOT NULL COMMENT '权限ID',
  PRIMARY KEY (`role_id`, `perm_id`),
  KEY `idx_perm_id` (`perm_id`),
  CONSTRAINT `fk_rp_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_rp_perm` FOREIGN KEY (`perm_id`) REFERENCES `sys_permission` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- Table: sys_notification (通知任务表)
DROP TABLE IF EXISTS `sys_notification`;
CREATE TABLE `sys_notification` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `store_id` BIGINT(20) DEFAULT NULL COMMENT '所属门店ID',
  `recipient` VARCHAR(100) NOT NULL COMMENT '接收者 (Email/Phone/UserId)',
  `type` VARCHAR(20) NOT NULL COMMENT 'EMAIL, SMS, IN_APP',
  `title` VARCHAR(100) DEFAULT NULL COMMENT '标题',
  `content` TEXT NOT NULL COMMENT '内容',
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, SENT, FAILED',
  `retry_count` INT(11) NOT NULL DEFAULT 0 COMMENT '已重试次数 (Max 3)',
  `next_retry_time` DATETIME DEFAULT NULL COMMENT '下次重试时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_notif_store` FOREIGN KEY (`store_id`) REFERENCES `sys_store` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知任务表';

-- Table: sys_audit_log (安全审计日志表)
DROP TABLE IF EXISTS `sys_audit_log`;
CREATE TABLE `sys_audit_log` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `store_id` BIGINT(20) DEFAULT NULL COMMENT '所属门店ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '操作人ID',
  `action` VARCHAR(50) NOT NULL COMMENT '动作 (如 UPDATE_PRICE)',
  `target` VARCHAR(50) DEFAULT NULL COMMENT '目标对象 (如 Order:123)',
  `client_ip` VARCHAR(50) DEFAULT NULL COMMENT '客户端IP',
  `details` TEXT DEFAULT NULL COMMENT '变更详情 (JSON)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_action` (`action`),
  CONSTRAINT `fk_audit_store` FOREIGN KEY (`store_id`) REFERENCES `sys_store` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_audit_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='安全审计日志表';

-- Table: sys_operation_log (操作日志表 - Added)
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `store_id` BIGINT COMMENT '门店ID',
    `user_id` BIGINT COMMENT '操作用户ID',
    `username` VARCHAR(50) COMMENT '操作用户名',
    `module` VARCHAR(50) COMMENT '功能模块',
    `business_type` VARCHAR(50) COMMENT '业务类型',
    `method` VARCHAR(100) COMMENT '请求方法名',
    `request_method` VARCHAR(10) COMMENT '请求方式',
    `oper_param` TEXT COMMENT '请求参数',
    `json_result` TEXT COMMENT '返回结果',
    `status` INT DEFAULT 0 COMMENT '操作状态(0正常 1异常)',
    `error_msg` TEXT COMMENT '错误消息',
    `oper_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    `cost_time` BIGINT COMMENT '消耗时间(ms)',
    `ip_addr` VARCHAR(50) COMMENT '主机地址',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_oper_time` (`oper_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志记录';

-- Table: sys_i18n_term (国际化词条表)
DROP TABLE IF EXISTS `sys_i18n_term`;
CREATE TABLE `sys_i18n_term` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `store_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '所属门店ID (0为全局)',
  `term_key` VARCHAR(100) NOT NULL COMMENT '词条Key',
  `zh` VARCHAR(500) NOT NULL COMMENT '中文值',
  `jp` VARCHAR(500) NOT NULL COMMENT '日文值',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_store_key` (`store_id`, `term_key`),
  KEY `idx_store_id` (`store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='国际化词条表';

-- =============================================
-- 2. Core Business Tables (Service, Tech, Room)
-- =============================================

-- Table: ams_service (服务项目表)
DROP TABLE IF EXISTS `ams_service`;
CREATE TABLE `ams_service` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `store_id` BIGINT(20) NOT NULL COMMENT '所属门店ID',
  `name` VARCHAR(100) NOT NULL COMMENT '服务名称',
  `duration` INT(11) NOT NULL COMMENT '时长 (分钟)',
  `price` DECIMAL(10,2) NOT NULL COMMENT '基准价格',
  `description` TEXT DEFAULT NULL COMMENT '描述',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1:上架, 0:下架',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_store_id` (`store_id`),
  CONSTRAINT `fk_service_store` FOREIGN KEY (`store_id`) REFERENCES `sys_store` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务项目表';

-- Table: ams_technician_info (技师扩展信息表)
DROP TABLE IF EXISTS `ams_technician_info`;
CREATE TABLE `ams_technician_info` (
  `user_id` BIGINT(20) NOT NULL COMMENT '关联 sys_user.id',
  `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
  `level` VARCHAR(20) NOT NULL COMMENT '职级 (Director/Senior)',
  `status` VARCHAR(20) NOT NULL DEFAULT 'IDLE' COMMENT 'IDLE(空闲), BUSY(忙碌), LEAVE(请假)',
  `wheel_seq` INT(11) DEFAULT NULL COMMENT '轮牌当前顺位 (越小越优先)',
  `intro_cn` TEXT DEFAULT NULL COMMENT '中文简介',
  `intro_jp` TEXT DEFAULT NULL COMMENT '日文简介',
  `last_job_end_time` DATETIME NULL COMMENT '上次工作结束时间',
  PRIMARY KEY (`user_id`),
  CONSTRAINT `fk_tech_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技师扩展信息表';

-- Table: ams_technician_clock_record (技师上下班打卡记录表 - Legacy?)
DROP TABLE IF EXISTS `ams_technician_clock_record`;
CREATE TABLE `ams_technician_clock_record` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `store_id` BIGINT(20) NOT NULL COMMENT '所属门店ID',
  `tech_id` BIGINT(20) NOT NULL COMMENT '技师ID (sys_user.id)',
  `type` VARCHAR(10) NOT NULL COMMENT 'IN/OUT',
  `clock_time` DATETIME NOT NULL COMMENT '打卡时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_tech_time` (`tech_id`, `clock_time`),
  CONSTRAINT `fk_clock_store` FOREIGN KEY (`store_id`) REFERENCES `sys_store` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_clock_tech` FOREIGN KEY (`tech_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技师上下班打卡记录表';

-- Table: ams_attendance (考勤记录表 - New)
DROP TABLE IF EXISTS `ams_attendance`;
CREATE TABLE `ams_attendance` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `store_id` BIGINT,
    `tech_id` BIGINT NOT NULL,
    `type` VARCHAR(20) NOT NULL COMMENT 'CLOCK_IN, CLOCK_OUT',
    `time` DATETIME NOT NULL,
    `location` VARCHAR(255) COMMENT '打卡地点',
    `status` VARCHAR(20) DEFAULT 'NORMAL' COMMENT '打卡状态: NORMAL, LATE, EARLY_LEAVE, MISSING',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤记录表';

-- Table: ams_technician_schedule (技师排班表 - Legacy/Base)
DROP TABLE IF EXISTS `ams_technician_schedule`;
CREATE TABLE `ams_technician_schedule` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `store_id` BIGINT(20) NOT NULL COMMENT '所属门店ID',
  `tech_id` BIGINT(20) NOT NULL COMMENT '技师ID (sys_user.id)',
  `date` DATE NOT NULL COMMENT '排班日期',
  `status` VARCHAR(20) NOT NULL COMMENT 'IDLE/BUSY/LEAVE',
  `start_time` TIME DEFAULT NULL COMMENT '开始时间',
  `end_time` TIME DEFAULT NULL COMMENT '结束时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_store_tech_date` (`store_id`, `tech_id`, `date`),
  KEY `idx_tech_id` (`tech_id`),
  CONSTRAINT `fk_sched_store` FOREIGN KEY (`store_id`) REFERENCES `sys_store` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_sched_tech` FOREIGN KEY (`tech_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技师排班表';

-- Table: ams_technician_shift (技师排班表 - New)
DROP TABLE IF EXISTS `ams_technician_shift`;
CREATE TABLE `ams_technician_shift` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tech_id` BIGINT(20) NOT NULL COMMENT '技师ID (sys_user.id)',
  `shift_date` DATE NOT NULL COMMENT '排班日期',
  `start_time` TIME NOT NULL COMMENT '开始时间',
  `end_time` TIME NOT NULL COMMENT '结束时间',
  `type` VARCHAR(20) NOT NULL DEFAULT 'WORK' COMMENT '类型: WORK(工作), LEAVE(请假), BREAK(休息)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_tech_date` (`tech_id`, `shift_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='技师排班表(新)';

-- Table: ams_leave_request (请假申请表)
DROP TABLE IF EXISTS `ams_leave_request`;
CREATE TABLE `ams_leave_request` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `store_id` BIGINT,
    `tech_id` BIGINT NOT NULL,
    `type` VARCHAR(20) NOT NULL COMMENT 'SICK, CASUAL, etc',
    `start_time` DATETIME NOT NULL,
    `end_time` DATETIME NOT NULL,
    `reason` VARCHAR(255),
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING, APPROVED, REJECTED',
    `audit_by` BIGINT COMMENT '审核人ID',
    `audit_time` DATETIME COMMENT '审核时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请假申请表';

-- Table: ams_blacklist (黑名单表)
DROP TABLE IF EXISTS `ams_blacklist`;
CREATE TABLE `ams_blacklist` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `store_id` BIGINT(20) NOT NULL COMMENT '所属门店ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID (sys_user.id)',
  `reason` VARCHAR(255) NOT NULL COMMENT '拉黑原因',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_store_user` (`store_id`, `user_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_bl_store` FOREIGN KEY (`store_id`) REFERENCES `sys_store` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_bl_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='黑名单表';

-- Table: ams_room (房间表)
DROP TABLE IF EXISTS `ams_room`;
CREATE TABLE `ams_room` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `store_id` BIGINT(20) NOT NULL COMMENT '所属门店ID',
  `name` VARCHAR(50) NOT NULL COMMENT '房间号/名',
  `type` VARCHAR(20) NOT NULL COMMENT 'SINGLE(单人), DOUBLE(双人)',
  `capacity` INT(11) NOT NULL COMMENT '容纳人数',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1:可用, 0:维护中',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_store_name` (`store_id`, `name`),
  CONSTRAINT `fk_room_store` FOREIGN KEY (`store_id`) REFERENCES `sys_store` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房间表';

-- =============================================
-- 3. Order & Transaction
-- =============================================

-- Table: ams_appointment (预约订单/聚合单表)
DROP TABLE IF EXISTS `ams_appointment`;
CREATE TABLE `ams_appointment` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `store_id` BIGINT(20) NOT NULL COMMENT '所属门店ID',
  `customer_id` BIGINT(20) NOT NULL COMMENT '客户ID',
  `start_time` DATETIME NOT NULL COMMENT '预约开始时间',
  `end_time` DATETIME NOT NULL COMMENT '预约结束时间',
  `people_count` INT(11) NOT NULL COMMENT '总人数',
  `status` INT(11) NOT NULL DEFAULT 5 COMMENT '2:已完成, 3:已取消, 4:违约, 5:待消费',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `creator_id` BIGINT(20) DEFAULT NULL COMMENT '创建人ID',
  `payment_status` VARCHAR(20) DEFAULT 'UNPAID' COMMENT '支付状态: UNPAID, PAID, REFUNDED',
  `refund_status` INT DEFAULT 0 COMMENT '退款状态: 0-未退款，1-退款中，2-已退款，3-退款失败',
  `contact_name` VARCHAR(50) DEFAULT NULL COMMENT '联系人姓名',
  `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系人电话',
  `payment_method` VARCHAR(20) DEFAULT NULL COMMENT '支付方式(ALIPAY, OFFLINE)',
  `source` VARCHAR(20) DEFAULT 'CLIENT' COMMENT '来源(CLIENT, ADMIN)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  PRIMARY KEY (`id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_customer_id` (`customer_id`),
  KEY `idx_time_range` (`start_time`, `end_time`),
  KEY `idx_payment_status` (`payment_status`),
  CONSTRAINT `fk_appt_store` FOREIGN KEY (`store_id`) REFERENCES `sys_store` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_appt_customer` FOREIGN KEY (`customer_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约订单表';

-- Table: ams_appointment_item (预约明细表)
DROP TABLE IF EXISTS `ams_appointment_item`;
CREATE TABLE `ams_appointment_item` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `appt_id` BIGINT(20) NOT NULL COMMENT '关联主订单ID',
  `service_id` BIGINT(20) NOT NULL COMMENT '服务项目ID',
  `tech_id` BIGINT(20) DEFAULT NULL COMMENT '技师ID (未分配则为空)',
  `room_id` BIGINT(20) DEFAULT NULL COMMENT '房间ID',
  `price` DECIMAL(10,2) NOT NULL COMMENT '明细金额',
  PRIMARY KEY (`id`),
  KEY `idx_appt_id` (`appt_id`),
  KEY `idx_service_id` (`service_id`),
  KEY `idx_tech_id` (`tech_id`),
  KEY `idx_room_id` (`room_id`),
  CONSTRAINT `fk_item_appt` FOREIGN KEY (`appt_id`) REFERENCES `ams_appointment` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_item_service` FOREIGN KEY (`service_id`) REFERENCES `ams_service` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_item_tech` FOREIGN KEY (`tech_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_item_room` FOREIGN KEY (`room_id`) REFERENCES `ams_room` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约明细表';

-- Table: ams_waiting_list (排队候补表)
DROP TABLE IF EXISTS `ams_waiting_list`;
CREATE TABLE `ams_waiting_list` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `store_id` BIGINT(20) NOT NULL COMMENT '所属门店ID',
  `customer_id` BIGINT(20) NOT NULL COMMENT '客户ID',
  `tech_id` BIGINT(20) DEFAULT NULL COMMENT '指定技师ID (可选)',
  `expected_date` DATE NOT NULL COMMENT '期望日期',
  `time_range` VARCHAR(50) NOT NULL COMMENT '期望时段 (e.g. "14:00-18:00")',
  `people_count` INT(11) NOT NULL COMMENT '人数',
  `status` VARCHAR(20) NOT NULL DEFAULT 'WAITING' COMMENT 'WAITING, NOTIFIED, EXPIRED, CONVERTED',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `expiry_time` DATETIME NOT NULL COMMENT '过期时间',
  PRIMARY KEY (`id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_customer_id` (`customer_id`),
  CONSTRAINT `fk_wait_store` FOREIGN KEY (`store_id`) REFERENCES `sys_store` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_wait_customer` FOREIGN KEY (`customer_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_wait_tech` FOREIGN KEY (`tech_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='排队候补表';

-- Table: ams_member_info (会员扩展表)
DROP TABLE IF EXISTS `ams_member_info`;
CREATE TABLE `ams_member_info` (
  `user_id` BIGINT(20) NOT NULL COMMENT '关联 sys_user.id',
  `store_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '所属门店ID (0为通用)',
  `level` VARCHAR(20) NOT NULL DEFAULT 'SILVER' COMMENT '会员等级',
  `balance` DECIMAL(10,2) DEFAULT 0.00 COMMENT '余额',
  `violation_count_month` INT(11) DEFAULT 0 COMMENT '当月违约次数',
  `restriction_end_time` DATETIME DEFAULT NULL COMMENT '预约限制结束时间',
  `allergy_history` TEXT DEFAULT NULL COMMENT '过敏史',
  `service_preference` TEXT DEFAULT NULL COMMENT '服务偏好',
  PRIMARY KEY (`user_id`),
  KEY `idx_store_id` (`store_id`),
  CONSTRAINT `fk_member_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员扩展表';

-- Table: ams_violation_record (违约记录表)
DROP TABLE IF EXISTS `ams_violation_record`;
CREATE TABLE `ams_violation_record` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `store_id` BIGINT(20) NOT NULL COMMENT '所属门店ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '关联用户ID',
  `appt_id` BIGINT(20) NOT NULL COMMENT '关联预约ID',
  `type` VARCHAR(20) NOT NULL COMMENT 'LATE_CANCEL(超时取消), NO_SHOW(爽约)',
  `violation_time` DATETIME NOT NULL COMMENT '违约发生时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_appt_id` (`appt_id`),
  CONSTRAINT `fk_viol_store` FOREIGN KEY (`store_id`) REFERENCES `sys_store` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_viol_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_viol_appt` FOREIGN KEY (`appt_id`) REFERENCES `ams_appointment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='违约记录表';

-- Table: ams_membership_level (会员等级规则表)
DROP TABLE IF EXISTS `ams_membership_level`;
CREATE TABLE `ams_membership_level` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `store_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '所属门店ID (0为通用)',
  `name` VARCHAR(50) NOT NULL COMMENT '等级名称',
  `min_spend` DECIMAL(10,2) NOT NULL COMMENT '升级所需最低消费',
  `discount` DECIMAL(3,2) NOT NULL COMMENT '折扣率 (如 0.90)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_store_name` (`store_id`, `name`),
  CONSTRAINT `fk_level_store` FOREIGN KEY (`store_id`) REFERENCES `sys_store` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员等级规则表';

-- Table: ams_transaction (流水表)
DROP TABLE IF EXISTS `ams_transaction`;
CREATE TABLE `ams_transaction` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `store_id` BIGINT(20) NOT NULL COMMENT '所属门店ID',
  `appt_id` BIGINT(20) NOT NULL COMMENT '关联预约',
  `type` VARCHAR(20) NOT NULL COMMENT 'PAY/REFUND',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '金额',
  `pay_method` VARCHAR(20) DEFAULT NULL COMMENT 'CASH/CARD/ONLINE',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '交易时间',
  PRIMARY KEY (`id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_appt_id` (`appt_id`),
  CONSTRAINT `fk_trans_store` FOREIGN KEY (`store_id`) REFERENCES `sys_store` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_trans_appt` FOREIGN KEY (`appt_id`) REFERENCES `ams_appointment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流水表';

-- Table: ams_review (评价表)
DROP TABLE IF EXISTS `ams_review`;
CREATE TABLE `ams_review` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `store_id` BIGINT(20) NOT NULL COMMENT '所属门店ID',
  `appt_id` BIGINT(20) NOT NULL COMMENT '关联预约',
  `rating` INT(11) NOT NULL COMMENT '1-5星',
  `content` VARCHAR(500) DEFAULT NULL COMMENT '评价内容',
  `is_anonymous` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否匿名',
  `reply_content` VARCHAR(500) DEFAULT NULL COMMENT '店长回复',
  `is_pushed` TINYINT(1) DEFAULT 0 COMMENT '是否已推送差评预警',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
  PRIMARY KEY (`id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_appt_id` (`appt_id`),
  CONSTRAINT `fk_review_store` FOREIGN KEY (`store_id`) REFERENCES `sys_store` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_review_appt` FOREIGN KEY (`appt_id`) REFERENCES `ams_appointment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';

-- Table: ams_payment_method (支付方式配置 - Added)
DROP TABLE IF EXISTS `ams_payment_method`;
CREATE TABLE `ams_payment_method` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `name` VARCHAR(50) NOT NULL COMMENT '支付名称',
    `code` VARCHAR(50) NOT NULL UNIQUE COMMENT '支付编码',
    `icon` VARCHAR(255) COMMENT '图标URL',
    `status` INT DEFAULT 1 COMMENT '状态(1启用 0禁用)',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `config` TEXT COMMENT '配置信息(JSON)',
    `remark` VARCHAR(255) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付方式配置';

-- Table: ams_pricing_rule (定价规则表 - Added)
DROP TABLE IF EXISTS `ams_pricing_rule`;
CREATE TABLE `ams_pricing_rule` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `store_id` BIGINT(20) DEFAULT NULL COMMENT '所属门店ID',
  `service_id` BIGINT(20) DEFAULT NULL COMMENT '关联服务ID(为空则全局)',
  `rule_type` VARCHAR(20) NOT NULL COMMENT '规则类型: TIERED(阶梯), GROUP(团体优惠)',
  `min_people` INT(11) DEFAULT 1 COMMENT '最小人数',
  `max_people` INT(11) DEFAULT 999 COMMENT '最大人数',
  `adjustment_type` VARCHAR(20) NOT NULL COMMENT '调整类型: PRICE_OVERRIDE(一口价), DISCOUNT_RATE(折扣率), DISCOUNT_AMOUNT(立减)',
  `adjustment_value` DECIMAL(10,2) NOT NULL COMMENT '调整值',
  `priority` INT(11) DEFAULT 0 COMMENT '优先级(数值越大越高)',
  `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_service_people` (`service_id`, `min_people`, `max_people`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定价规则表';

-- =============================================
-- 4. Triggers
-- =============================================

DROP TRIGGER IF EXISTS trg_ams_appointment_status_guard;
DELIMITER $$
CREATE TRIGGER trg_ams_appointment_status_guard
BEFORE UPDATE ON ams_appointment
FOR EACH ROW
BEGIN
    IF NEW.status IS NOT NULL AND NEW.status <> OLD.status THEN
        -- 仅从 待消费(5) 允许进入终态
        IF OLD.status = 5 THEN
            IF NEW.status NOT IN (2, 3, 4) THEN
                SIGNAL SQLSTATE '45000'
                    SET MESSAGE_TEXT = '非法状态变更：待消费仅允许变更为已完成/已取消/违约';
            END IF;
        -- 已完成/已取消/违约 为终态，禁止任何进一步变化
        ELSEIF OLD.status IN (2, 3, 4) THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = '非法状态变更：终态订单不允许变更状态';
        -- 其他历史遗留状态（如 0/1）不允许继续沿用
        ELSE
            -- 允许旧状态变更以支持迁移，或可在此处严格限制
            -- SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '非法状态变更：仅支持从待消费进入终态';
        END IF;
    END IF;
END$$
DELIMITER ;

-- =============================================
-- 5. Initial Data
-- =============================================

-- 5.1 Initial Store
INSERT INTO `sys_store` (`id`, `name`, `address`, `phone`, `status`) VALUES
(1, 'Trae Kasa 旗舰店', '东京都市中心1-1-1', '03-1234-5678', 1);

-- 5.2 Initial Roles
INSERT INTO `sys_role` (`id`, `code`, `name`) VALUES
(1, 'ROLE_ADMIN', '超级管理员'),
(2, 'ROLE_MANAGER', '店长'),
(3, 'ROLE_TECH', '技师'),
(4, 'ROLE_CUSTOMER', '客户');

-- 5.3 Initial Users
-- Password: MD5("123456" + "1a2b3c4d5e6f7g8h") = e10adc3949ba59abbe56e057f20f883e
INSERT INTO `sys_user` (`id`, `store_id`, `username`, `password`, `salt`, `nickname`, `status`) VALUES
(1, NULL, 'admin', 'e10adc3949ba59abbe56e057f20f883e', '1a2b3c4d5e6f7g8h', 'Admin', 1),
(2, 1, 'manager', 'e10adc3949ba59abbe56e057f20f883e', '1a2b3c4d5e6f7g8h', 'Manager', 1),
(3, 1, 'tech001', 'e10adc3949ba59abbe56e057f20f883e', '1a2b3c4d5e6f7g8h', 'Tech One', 1),
(4, 1, 'tech002', 'e10adc3949ba59abbe56e057f20f883e', '1a2b3c4d5e6f7g8h', 'Tech Two', 1),
(5, 1, 'customer001', 'e10adc3949ba59abbe56e057f20f883e', '1a2b3c4d5e6f7g8h', 'Guest', 1);

-- 5.4 User-Role Assignments
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1, 1), -- Admin -> ROLE_ADMIN
(2, 2), -- Manager -> ROLE_MANAGER
(3, 3), -- Tech1 -> ROLE_TECH
(4, 3), -- Tech2 -> ROLE_TECH
(5, 4); -- Customer -> ROLE_CUSTOMER

-- 5.5 Services
INSERT INTO `ams_service` (`id`, `store_id`, `name`, `duration`, `price`, `description`, `status`) VALUES
(1, 1, '全身精油SPA', 60, 8800.00, '深层放松，舒缓疲劳', 1),
(2, 1, '面部护理', 45, 6800.00, '补水保湿，提亮肤色', 1),
(3, 1, '足底按摩', 30, 4500.00, '缓解足部疲劳', 1);

-- 5.6 Rooms
INSERT INTO `ams_room` (`id`, `store_id`, `name`, `type`, `capacity`, `status`) VALUES
(1, 1, 'R101', 'SINGLE', 1, 1),
(2, 1, 'R102', 'DOUBLE', 2, 1),
(3, 1, 'R103', 'SINGLE', 1, 1);

-- 5.7 Technician Info
INSERT INTO `ams_technician_info` (`user_id`, `real_name`, `level`, `status`, `intro_cn`) VALUES
(3, '田中 太郎', 'Senior', 'IDLE', '拥有10年从业经验，擅长指压。'),
(4, '佐藤 花子', 'Director', 'IDLE', '技术总监，擅长精油护理。');

-- 5.8 Membership Levels
INSERT INTO `ams_membership_level` (`id`, `store_id`, `name`, `min_spend`, `discount`) VALUES
(1, 0, 'SILVER', 0.00, 1.00),
(2, 0, 'GOLD', 50000.00, 0.95),
(3, 0, 'PLATINUM', 200000.00, 0.88);

-- 5.9 Payment Methods
INSERT IGNORE INTO `ams_payment_method` (`name`, `code`, `status`, `sort_order`) VALUES 
('支付宝', 'ALIPAY', 1, 1),
('微信支付', 'WECHAT', 1, 2),
('现金/线下', 'OFFLINE', 1, 3),
('会员余额', 'BALANCE', 1, 4);

SET FOREIGN_KEY_CHECKS = 1;
