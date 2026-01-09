DROP TABLE IF EXISTS `tortoise_token_total_record`;
DROP TABLE IF EXISTS `tortoise_llm_usage`;
DROP TABLE IF EXISTS `tortoise_message`;
DROP TABLE IF EXISTS `tortoise_memory`;
DROP TABLE IF EXISTS `tortoise_conversation`;
DROP TABLE IF EXISTS `tortoise_user`;
DROP TABLE IF EXISTS `tortoise_sys_user`;
DROP TABLE IF EXISTS `tortoise_chat_profile`;
DROP TABLE IF EXISTS `tortoise_memory_prompt_config`;
DROP TABLE IF EXISTS `tortoise_memory_policy`;
DROP TABLE IF EXISTS `tortoise_llm_config`;
DROP TABLE IF EXISTS `tortoise_import_file_record`;
DROP TABLE IF EXISTS `tortoise_tool`;
DROP TABLE IF EXISTS `tortoise_conversation_tool`;
DROP TABLE IF EXISTS `tortoise_message_extend`;



CREATE TABLE `tortoise_message_extend` (
                                           `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                           `message_id` bigint NOT NULL COMMENT '消息ID，关联tortoise_message.id',
                                           `tool_call_messages` json DEFAULT NULL COMMENT '工具调用消息',
                                           `events` json DEFAULT NULL COMMENT '事件信息',
                                           `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
                                           `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                           `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                           PRIMARY KEY (`id`),
                                           UNIQUE KEY `uk_message_id` (`message_id`),
                                           KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB COMMENT='消息扩展表';


CREATE TABLE `tortoise_conversation_tool` (
                                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                              `tool_id` varchar(64) NOT NULL COMMENT '工具ID',
                                              `conversation_id` varchar(64) NOT NULL COMMENT '会话唯一标识',
                                              `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
                                              `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                              `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                              PRIMARY KEY (`id`),
                                              UNIQUE KEY `uk_conversation_tool` (`conversation_id`, `tool_id`),
                                              KEY `idx_tool_id` (`tool_id`),
                                              KEY `idx_conversation_id` (`conversation_id`),
                                              KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB COMMENT='对话工具关联表';


CREATE TABLE `tortoise_tool` (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 `name` varchar(255) NOT NULL COMMENT '工具名称',
                                 `desc` varchar(1000) DEFAULT NULL COMMENT '工具描述',
                                 `field_config` json DEFAULT NULL COMMENT '字段配置（JSON格式）',
                                 `url` varchar(500) NOT NULL COMMENT '请求URL',
                                 `method` varchar(10) NOT NULL COMMENT '请求方法（GET/POST/PUT/DELETE等）',
                                 `headers` json DEFAULT NULL COMMENT '请求头（JSON格式）',
                                 `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
                                 `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_name` (`name`),
                                 KEY `idx_is_deleted` (`is_deleted`),
                                 KEY `idx_create_time` (`create_time`),
                                 KEY `idx_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工具配置表';

CREATE TABLE `tortoise_import_file_record` (
                                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                               `import_name` varchar(255) NOT NULL COMMENT '导入名称/文件名',
                                               `status` tinyint DEFAULT '0' COMMENT '导入状态：0-处理中，1-成功，2-失败',
                                               `error_info` text COMMENT '错误信息',
                                               `create_user_id` bigint NOT NULL COMMENT '导入的用户ID',
                                               `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
                                               `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                               `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                               PRIMARY KEY (`id`),
                                               KEY `idx_status` (`status`),
                                               KEY `idx_create_user_id` (`create_user_id`),
                                               KEY `idx_is_deleted` (`is_deleted`),
                                               KEY `idx_create_time` (`create_time`),
                                               KEY `idx_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文件导入记录表';


CREATE TABLE `tortoise_chat_profile` (
                                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                         `name` varchar(100) NOT NULL COMMENT '配置名称',
                                         `llm_config_id` bigint NOT NULL COMMENT '聊天模型id',
                                         `memory_policy_id` bigint NOT NULL COMMENT '记忆策略id',
                                         `sk` varchar(255) DEFAULT NULL COMMENT 'sk，提供给客户端调用时传入',
                                         `is_default` tinyint DEFAULT '0' COMMENT '是否默认配置：0-非默认，1-默认配置',
                                         `create_user_id` bigint NOT NULL COMMENT '创建用户ID',
                                         `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
                                         `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
                                         `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                         `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                         `daily_token_limit_conversation` bigint DEFAULT NULL COMMENT '单日单会话token上限',
                                         `daily_token_limit_total` bigint DEFAULT NULL COMMENT '单日总token上限',
                                         PRIMARY KEY (`id`),
                                         KEY `idx_llm_config_id` (`llm_config_id`),
                                         KEY `idx_memory_policy_id` (`memory_policy_id`),
                                         KEY `idx_create_user_id` (`create_user_id`),
                                         KEY `idx_is_default` (`is_default`),
                                         KEY `idx_status` (`status`),
                                         KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB  COMMENT='聊天配置表';

CREATE TABLE `tortoise_conversation` (
                                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                         `conversation_id` varchar(64) DEFAULT NULL COMMENT '会话唯一标识',
                                         `q_and_a_count` int DEFAULT '0' COMMENT '对话轮次(一问一答)',
                                         `chat_profile_id` bigint DEFAULT NULL COMMENT '聊天配置ID',
                                         `create_user_id` bigint DEFAULT NULL COMMENT '用户ID',
                                         `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
                                         `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                         `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                         PRIMARY KEY (`id`),
                                         UNIQUE KEY `uk_conversation_id` (`conversation_id`),
                                         KEY `idx_chat_profile_id` (`chat_profile_id`),
                                         KEY `idx_create_user_id` (`create_user_id`),
                                         KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB  COMMENT='对话表';

CREATE TABLE `tortoise_llm_config` (
                                       `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                       `create_user_id` bigint DEFAULT NULL COMMENT '用户ID',
                                       `config_name` varchar(100) NOT NULL COMMENT '配置名称',
                                       `model_name` varchar(100) NOT NULL COMMENT '大模型名称',
                                       `api_key` varchar(255) NOT NULL COMMENT 'API密钥',
                                       `api_url` varchar(500) NOT NULL COMMENT '完整的API URL',
                                       `input_unit_price` decimal(10,6) NOT NULL COMMENT '输入单元价格(每token)',
                                       `output_unit_price` decimal(10,6) NOT NULL COMMENT '输出单元价格(每token)',
                                       `cache_price` decimal(10,6) DEFAULT '0.000000' COMMENT '缓存价格(每token)',
                                       `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
                                       `description` text NOT NULL COMMENT '配置描述',
                                       `temperature` decimal(3,2) DEFAULT '1.00' COMMENT '温度值：控制随机性，范围0.0-2.0',
                                       `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                       `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
                                       PRIMARY KEY (`id`),
                                       KEY `idx_create_user_id` (`create_user_id`),
                                       KEY `idx_model_name` (`model_name`),
                                       KEY `idx_status` (`status`),
                                       KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB  COMMENT='LLM配置表';

CREATE TABLE `tortoise_llm_usage` (
                                      `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                      `conversation_id` varchar(64) DEFAULT NULL COMMENT '会话ID',
                                      `config_id` bigint DEFAULT NULL COMMENT 'LLM配置ID',
                                      `type` int DEFAULT '0' COMMENT '消费类型',
                                      `input_tokens` int DEFAULT '0' COMMENT '输入token数量',
                                      `output_tokens` int DEFAULT '0' COMMENT '输出token数量',
                                      `total_tokens` int DEFAULT '0' COMMENT '总token数量',
                                      `input_cost` decimal(10,6) DEFAULT '0.000000' COMMENT '输入费用',
                                      `output_cost` decimal(10,6) DEFAULT '0.000000' COMMENT '输出费用',
                                      `total_cost` decimal(10,6) DEFAULT '0.000000' COMMENT '总费用',
                                      `request_content` text COMMENT '请求内容',
                                      `response_content` text COMMENT '响应内容',
                                      `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
                                      `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                      PRIMARY KEY (`id`),
                                      KEY `idx_conversation_id` (`conversation_id`),
                                      KEY `idx_config_id` (`config_id`),
                                      KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB  COMMENT='LLM使用记录表';

CREATE TABLE `tortoise_memory` (
                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                   `conversation_id` varchar(64) DEFAULT NULL COMMENT '会话ID',
                                   `last_message_id` bigint DEFAULT NULL COMMENT '最后消息ID',
                                   `content` text COMMENT '记忆内容',
                                   `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
                                   `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   PRIMARY KEY (`id`),
                                   KEY `idx_conversation_id` (`conversation_id`),
                                   KEY `idx_last_message_id` (`last_message_id`),
                                   KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB  COMMENT='记忆表';

CREATE TABLE `tortoise_memory_policy` (
                                          `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                          `name` varchar(100) DEFAULT NULL COMMENT '策略名称',
                                          `llm_config_id` bigint DEFAULT NULL COMMENT 'LLM配置ID',
                                          `create_user_id` bigint DEFAULT NULL COMMENT '用户ID',
                                          `config` text COMMENT '策略配置',
                                          `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
                                          `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                          `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                          PRIMARY KEY (`id`),
                                          KEY `idx_llm_config_id` (`llm_config_id`),
                                          KEY `idx_create_user_id` (`create_user_id`),
                                          KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB  COMMENT='记忆策略表';

CREATE TABLE `tortoise_memory_prompt_config` (
                                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                                 `type` varchar(50) DEFAULT NULL COMMENT '类型，参考MemoryTypeEnum',
                                                 `base_prompt` text COMMENT '基础提示词',
                                                 `over_length_prompt` text COMMENT '超长提示词',
                                                 `update_prompt` text COMMENT '更新提示词',
                                                 `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
                                                 `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                                 `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                                 PRIMARY KEY (`id`),
                                                 KEY `idx_type` (`type`),
                                                 KEY `idx_is_deleted` (`is_deleted`),
                                                 KEY `idx_create_time` (`create_time`),
                                                 KEY `idx_update_time` (`update_time`)
) ENGINE=InnoDB  COMMENT='记忆提示词配置表';

CREATE TABLE `tortoise_message` (
                                    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                    `conversation_id` varchar(64) DEFAULT NULL COMMENT '会话ID',
                                    `unique_id` varchar(64) NOT NULL COMMENT '消息ID',
                                    `input_content` text COMMENT '输入消息内容',
                                    `input_role` varchar(20) DEFAULT NULL COMMENT '输入角色：user/assistant/system',
                                    `output_content` text COMMENT '输出消息内容',
                                    `output_role` varchar(20) DEFAULT NULL COMMENT '输出角色：user/assistant/system',
                                    `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
                                    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    PRIMARY KEY (`id`),
                                    UNIQUE KEY `uk_unique_id` (`unique_id`),
                                    KEY `idx_conversation_id` (`conversation_id`),
                                    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB COMMENT='消息表';

CREATE TABLE `tortoise_sys_user` (
                                     `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                     `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
                                     `password_hash` varchar(255) DEFAULT NULL COMMENT '密码哈希',
                                     `role_code` bigint DEFAULT NULL COMMENT '角色代码',
                                     `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
                                     `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                     PRIMARY KEY (`id`),
                                     UNIQUE KEY `uk_email` (`email`),
                                     KEY `idx_role_code` (`role_code`),
                                     KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB  COMMENT='系统用户表';

CREATE TABLE `tortoise_token_total_record` (
                                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                               `conversation_id` varchar(64) DEFAULT NULL COMMENT '会话ID',
                                               `chat_profile_id` bigint DEFAULT NULL COMMENT '聊天配置ID',
                                               `total` bigint NOT NULL COMMENT 'token总数',
                                               `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除, 1-已删除',
                                               `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                               `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                               PRIMARY KEY (`id`),
                                               KEY `idx_conversation_id` (`conversation_id`),
                                               KEY `idx_chat_profile_id` (`chat_profile_id`),
                                               KEY `idx_is_deleted` (`is_deleted`),
                                               KEY `idx_create_time` (`create_time`),
                                               KEY `idx_update_time` (`update_time`)
) ENGINE=InnoDB  COMMENT='会话token总数统计表';

CREATE TABLE `tortoise_user` (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 `custom_id` varchar(100) DEFAULT NULL COMMENT '自定义用户id',
                                 `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
                                 `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_custom_id` (`custom_id`),
                                 KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB  COMMENT='用户表';

