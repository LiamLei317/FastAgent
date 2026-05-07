-- 会话表
CREATE TABLE IF NOT EXISTS `chat_session`
(
    `id`          VARCHAR(64) NOT NULL COMMENT '会话ID（全局唯一）',
    `user_id`     VARCHAR(64) NOT NULL COMMENT '用户ID',
    `title`       VARCHAR(1024) DEFAULT NULL COMMENT '会话标题',
    `status`      TINYINT      DEFAULT 1 COMMENT '状态 1-正常 0-禁用',
    `is_deleted`  TINYINT      DEFAULT 0 COMMENT '是否删除 0-否 1-是',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci COMMENT ='AI 会话表';

-- 消息表
CREATE TABLE IF NOT EXISTS `chat_message`
(
    `id`          BIGINT        NOT NULL AUTO_INCREMENT,
    `session_id`   VARCHAR(64)   NOT NULL COMMENT '会话ID',
    `role`        VARCHAR(20)   NOT NULL COMMENT 'user/assistant/system',
    `content`     TEXT          NOT NULL COMMENT '消息内容',
    `content_type` TINYINT      DEFAULT 0 COMMENT '消息类型 0-文本 1-图片 2-文件',
    `status`      TINYINT       DEFAULT 1 COMMENT '1-正常 0-异常/撤回',
    `create_time` DATETIME      DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_session_id` (`session_id`)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci COMMENT ='AI 对话消息表';