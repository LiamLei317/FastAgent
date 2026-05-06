-- Fast Agent 数据库初始化脚本
-- 创建数据库（如果需要）
-- CREATE DATABASE IF NOT EXISTS fast_agent DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- USE fast_agent;

-- 会话表（用户设计）
CREATE TABLE IF NOT EXISTS `chat_session` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `title` varchar(255) DEFAULT NULL COMMENT '会话标题（第一句提问生成）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 消息表（用户设计）
CREATE TABLE IF NOT EXISTS `chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` bigint NOT NULL COMMENT '会话ID（外键关联 chat_session.id）',
  `role` varchar(20) NOT NULL COMMENT 'user / assistant',
  `content` text NOT NULL COMMENT '消息内容',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入测试数据（可选）
-- INSERT INTO `chat_session` (user_id, title) VALUES 
-- ('user001', '测试会话');
-- 
-- INSERT INTO `chat_message` (session_id, role, content) VALUES 
-- (1, 'user', '你好，请介绍一下自己'),
-- (1, 'assistant', '你好！我是一个AI助手，很高兴为您服务。');
