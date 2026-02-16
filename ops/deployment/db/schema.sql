-- ===============================================
-- Seed Cloud Microservices - 数据库 Schema 定义
-- ===============================================
-- 仅包含 DDL (CREATE TABLE)
-- 适用于：首次部署、增量迁移
-- 注意：不包含 DROP TABLE，不会删除已有数据
-- ===============================================

-- ----------------------------
-- 部门表
-- ----------------------------
CREATE TABLE IF NOT EXISTS "sys_dept" (
  "dept_id" bigserial NOT NULL PRIMARY KEY,
  "parent_id" bigint DEFAULT 0,
  "ancestors" varchar(500) DEFAULT '',
  "dept_name" varchar(50) DEFAULT '',
  "sort" integer DEFAULT 0,
  "leader" varchar(20),
  "phone" varchar(20),
  "email" varchar(50),
  "status" smallint DEFAULT 1,
  "deleted" smallint DEFAULT 0,
  "create_by" varchar(64) DEFAULT '',
  "create_time" timestamp DEFAULT CURRENT_TIMESTAMP,
  "update_by" varchar(64) DEFAULT '',
  "update_time" timestamp,
  "remark" varchar(500),
  "tenant_id" varchar(64)
);

-- ----------------------------
-- 用户表
-- ----------------------------
CREATE TABLE IF NOT EXISTS "sys_user" (
  "user_id" bigserial NOT NULL PRIMARY KEY,
  "dept_id" bigint,
  "username" varchar(30) NOT NULL,
  "password" varchar(100) DEFAULT '',
  "nickname" varchar(30) DEFAULT '',
  "email" varchar(50) DEFAULT '',
  "phone" varchar(11) DEFAULT '',
  "sex" smallint DEFAULT 0,
  "avatar" varchar(200) DEFAULT '',
  "status" smallint DEFAULT 1,
  "deleted" smallint DEFAULT 0,
  "login_ip" varchar(128) DEFAULT '',
  "login_date" timestamp,
  "create_by" varchar(64) DEFAULT '',
  "create_time" timestamp DEFAULT CURRENT_TIMESTAMP,
  "update_by" varchar(64) DEFAULT '',
  "update_time" timestamp,
  "remark" varchar(500),
  "tenant_id" varchar(64),
  UNIQUE ("username")
);

-- ----------------------------
-- 角色表
-- ----------------------------
CREATE TABLE IF NOT EXISTS "sys_role" (
  "role_id" bigserial NOT NULL PRIMARY KEY,
  "role_name" varchar(30) NOT NULL,
  "role_key" varchar(100) NOT NULL,
  "sort" integer NOT NULL,
  "data_scope" smallint DEFAULT 1,
  "status" smallint DEFAULT 1,
  "deleted" smallint DEFAULT 0,
  "create_by" varchar(64) DEFAULT '',
  "create_time" timestamp DEFAULT CURRENT_TIMESTAMP,
  "update_by" varchar(64) DEFAULT '',
  "update_time" timestamp,
  "remark" varchar(500),
  "tenant_id" varchar(64)
);

-- ----------------------------
-- 菜单表
-- ----------------------------
CREATE TABLE IF NOT EXISTS "sys_menu" (
  "menu_id" bigserial NOT NULL PRIMARY KEY,
  "menu_name" varchar(50) NOT NULL,
  "parent_id" bigint DEFAULT 0,
  "sort" integer DEFAULT 0,
  "path" varchar(200) DEFAULT '',
  "component" varchar(255),
  "query" varchar(255),
  "menu_type" char(1) DEFAULT '',
  "visible" smallint DEFAULT 1,
  "status" smallint DEFAULT 1,
  "perms" varchar(100),
  "icon" varchar(100) DEFAULT '#',
  "is_cache" smallint DEFAULT 0,
  "is_frame" smallint DEFAULT 0,
  "deleted" smallint DEFAULT 0,
  "create_by" varchar(64) DEFAULT '',
  "create_time" timestamp DEFAULT CURRENT_TIMESTAMP,
  "update_by" varchar(64) DEFAULT '',
  "update_time" timestamp,
  "remark" varchar(500) DEFAULT '',
  "tenant_id" varchar(64)
);

-- ----------------------------
-- 用户角色关联表
-- ----------------------------
CREATE TABLE IF NOT EXISTS "sys_user_role" (
  "user_id" bigint NOT NULL,
  "role_id" bigint NOT NULL,
  PRIMARY KEY ("user_id","role_id")
);

-- ----------------------------
-- 角色菜单关联表
-- ----------------------------
CREATE TABLE IF NOT EXISTS "sys_role_menu" (
  "role_id" bigint NOT NULL,
  "menu_id" bigint NOT NULL,
  PRIMARY KEY ("role_id","menu_id")
);

-- ----------------------------
-- 操作日志表
-- ----------------------------
CREATE TABLE IF NOT EXISTS "sys_oper_log" (
  "oper_id" bigserial NOT NULL PRIMARY KEY,
  "title" varchar(50) DEFAULT '',
  "business_type" integer DEFAULT 0,
  "method" varchar(100) DEFAULT '',
  "request_method" varchar(10) DEFAULT '',
  "operator_type" integer DEFAULT 0,
  "oper_name" varchar(50) DEFAULT '',
  "dept_name" varchar(50) DEFAULT '',
  "oper_url" varchar(255) DEFAULT '',
  "oper_ip" varchar(128) DEFAULT '',
  "oper_location" varchar(255) DEFAULT '',
  "oper_param" varchar(2000) DEFAULT '',
  "json_result" varchar(2000) DEFAULT '',
  "status" integer DEFAULT 0,
  "error_msg" varchar(2000) DEFAULT '',
  "oper_time" timestamp,
  "cost_time" bigint DEFAULT 0
);
CREATE INDEX IF NOT EXISTS "idx_oper_time" ON "sys_oper_log" ("oper_time");

-- ----------------------------
-- 登录日志表
-- ----------------------------
CREATE TABLE IF NOT EXISTS "sys_login_log" (
  "info_id" bigserial NOT NULL PRIMARY KEY,
  "username" varchar(50) DEFAULT '',
  "ipaddr" varchar(128) DEFAULT '',
  "login_location" varchar(255) DEFAULT '',
  "browser" varchar(50) DEFAULT '',
  "os" varchar(50) DEFAULT '',
  "status" smallint DEFAULT 0,
  "msg" varchar(255) DEFAULT '',
  "login_time" timestamp
);
CREATE INDEX IF NOT EXISTS "idx_login_time" ON "sys_login_log" ("login_time");
