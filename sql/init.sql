-- ============================================================
-- CampusMart · 用户表初始化脚本
--
-- 执行方式（二选一）：
--   A. MySQL Workbench：打开本文件 → 点工具栏的「闪电」图标执行
--   B. 命令行：mysql -u root -p < init.sql
-- ============================================================

-- ---------- 1. 建库 ----------
-- utf8mb4 是 MySQL 里完整的 UTF-8，中文、emoji 都不会乱码
CREATE DATABASE IF NOT EXISTS campusmart
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE campusmart;

-- ---------- 2. 建用户表 ----------
CREATE TABLE IF NOT EXISTS `user`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`    VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`    VARCHAR(100) NOT NULL COMMENT '密码（BCrypt 加密后的）',
    `nickname`    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `avatar`      VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `campus`      VARCHAR(50)  DEFAULT NULL COMMENT '校区',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1正常 0禁用',
    `role`        VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色：USER普通用户 ADMIN管理员',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '用户表';

-- ---------- 3. 确认建好了 ----------
SHOW TABLES;
DESC `user`;
