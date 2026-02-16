-- ===============================================
-- Seed Cloud Microservices - 数据库初始化脚本
-- ===============================================
-- 此文件已拆分为以下脚本，请按需使用：
--
-- 1. schema.sql  - DDL 定义（仅 CREATE TABLE，无 DROP）
--    用途：首次部署、增量迁移
--    命令：psql -d seed_cloud -f schema.sql
--
-- 2. seed.sql    - 初始数据（INSERT 语句）
--    用途：首次部署后填充数据
--    命令：psql -d seed_cloud -f seed.sql
--
-- 3. reset.sql   - 开发环境重置（DROP + CREATE + INSERT）
--    用途：开发/测试环境重置数据
--    命令：psql -d seed_cloud -f reset.sql
--    ⚠️ 警告：会删除所有数据，生产环境严禁使用！
--
-- 完整初始化流程（新环境）：
--   psql -d seed_cloud -f schema.sql
--   psql -d seed_cloud -f seed.sql
--
-- ===============================================

-- 以下保留原始脚本供参考（建议使用上述拆分后的脚本）

-- ----------------------------
-- 部门表
-- ----------------------------
DROP TABLE IF EXISTS "sys_dept";
CREATE TABLE "sys_dept" (
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
-- 部门数据
-- ----------------------------
INSERT INTO "sys_dept" VALUES (100, 0, '0', '总公司', 0, '管理员', '15888888888', 'admin@seed.com', 1, 0, 'admin', CURRENT_TIMESTAMP, '', NULL);
INSERT INTO "sys_dept" VALUES (101, 100, '0,100', '深圳分公司', 1, 'user', '15888888888', 'user@seed.com', 1, 0, 'admin', CURRENT_TIMESTAMP, '', NULL);
INSERT INTO "sys_dept" VALUES (102, 100, '0,100', '长沙分公司', 2, 'user', '15888888888', 'user@seed.com', 1, 0, 'admin', CURRENT_TIMESTAMP, '', NULL);
INSERT INTO "sys_dept" VALUES (103, 101, '0,100,101', '研发部门', 1, 'user', '15888888888', 'user@seed.com', 1, 0, 'admin', CURRENT_TIMESTAMP, '', NULL);
INSERT INTO "sys_dept" VALUES (104, 101, '0,100,101', '市场部门', 2, 'user', '15888888888', 'user@seed.com', 1, 0, 'admin', CURRENT_TIMESTAMP, '', NULL);
INSERT INTO "sys_dept" VALUES (105, 101, '0,100,101', '测试部门', 3, 'user', '15888888888', 'user@seed.com', 1, 0, 'admin', CURRENT_TIMESTAMP, '', NULL);
INSERT INTO "sys_dept" VALUES (106, 101, '0,100,101', '财务部门', 4, 'user', '15888888888', 'user@seed.com', 1, 0, 'admin', CURRENT_TIMESTAMP, '', NULL);
INSERT INTO "sys_dept" VALUES (107, 101, '0,100,101', '运维部门', 5, 'user', '15888888888', 'user@seed.com', 1, 0, 'admin', CURRENT_TIMESTAMP, '', NULL);
INSERT INTO "sys_dept" VALUES (108, 102, '0,100,102', '市场部门', 1, 'user', '15888888888', 'user@seed.com', 1, 0, 'admin', CURRENT_TIMESTAMP, '', NULL);
INSERT INTO "sys_dept" VALUES (109, 102, '0,100,102', '财务部门', 2, 'user', '15888888888', 'user@seed.com', 1, 0, 'admin', CURRENT_TIMESTAMP, '', NULL);

-- 重置部门序列（避免后续 INSERT 无显式 ID 时主键冲突）
SELECT setval('sys_dept_dept_id_seq', (SELECT MAX(dept_id) FROM sys_dept));

-- ----------------------------
-- 用户表
-- ----------------------------
DROP TABLE IF EXISTS "sys_user";
CREATE TABLE "sys_user" (
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
-- 用户数据 (密码: admin123)
-- ----------------------------
INSERT INTO "sys_user" VALUES (1, 103, 'admin', '$2b$12$5XuJLpiEjPusp7aSGT1fJOQqsLyUNupeExGgPwBZSuVxErTi43laW', '系统管理员', 'admin@seed.com', '15888888888', 1, '', 1, 0, '127.0.0.1', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, '', NULL, '管理员');
INSERT INTO "sys_user" VALUES (2, 105, 'user', '$2b$12$5XuJLpiEjPusp7aSGT1fJOQqsLyUNupeExGgPwBZSuVxErTi43laW', '普通用户', 'user@seed.com', '15666666666', 1, '', 1, 0, '127.0.0.1', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, '', NULL, '普通用户');

-- 重置用户序列
SELECT setval('sys_user_user_id_seq', (SELECT MAX(user_id) FROM sys_user));

-- ----------------------------
-- 角色表
-- ----------------------------
DROP TABLE IF EXISTS "sys_role";
CREATE TABLE "sys_role" (
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
-- 角色数据
-- ----------------------------
INSERT INTO "sys_role" VALUES (1, '超级管理员', 'admin', 1, 1, 1, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, '超级管理员');
INSERT INTO "sys_role" VALUES (2, '普通角色', 'common', 2, 2, 1, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, '普通角色');

-- 重置角色序列
SELECT setval('sys_role_role_id_seq', (SELECT MAX(role_id) FROM sys_role));

-- ----------------------------
-- 菜单表
-- ----------------------------
DROP TABLE IF EXISTS "sys_menu";
CREATE TABLE "sys_menu" (
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
-- 菜单数据
-- ----------------------------
-- 一级菜单
INSERT INTO "sys_menu" VALUES (1, '系统管理', 0, 1, 'system', NULL, '', 'M', 1, 1, '', 'system', 0, 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, '系统管理目录');
INSERT INTO "sys_menu" VALUES (2, '系统监控', 0, 2, 'monitor', NULL, '', 'M', 1, 1, '', 'monitor', 0, 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, '系统监控目录');
INSERT INTO "sys_menu" VALUES (3, '系统工具', 0, 3, 'tool', NULL, '', 'M', 1, 1, '', 'tool', 0, 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, '系统工具目录');

-- 二级菜单
INSERT INTO "sys_menu" VALUES (100, '用户管理', 1, 1, 'user', 'system/user/index', '', 'C', 1, 1, 'system:user:list', 'user', 0, 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, '用户管理菜单');
INSERT INTO "sys_menu" VALUES (101, '角色管理', 1, 2, 'role', 'system/role/index', '', 'C', 1, 1, 'system:role:list', 'peoples', 0, 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, '角色管理菜单');
INSERT INTO "sys_menu" VALUES (102, '菜单管理', 1, 3, 'menu', 'system/menu/index', '', 'C', 1, 1, 'system:menu:list', 'tree-table', 0, 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, '菜单管理菜单');
INSERT INTO "sys_menu" VALUES (103, '部门管理', 1, 4, 'dept', 'system/dept/index', '', 'C', 1, 1, 'system:dept:list', 'tree', 0, 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, '部门管理菜单');

-- 按钮权限
INSERT INTO "sys_menu" VALUES (1000, '用户查询', 100, 1, '', '', '', 'F', 1, 1, 'system:user:query', '#', 0, 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, '');
INSERT INTO "sys_menu" VALUES (1001, '用户新增', 100, 2, '', '', '', 'F', 1, 1, 'system:user:add', '#', 0, 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, '');
INSERT INTO "sys_menu" VALUES (1002, '用户修改', 100, 3, '', '', '', 'F', 1, 1, 'system:user:edit', '#', 0, 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, '');
INSERT INTO "sys_menu" VALUES (1003, '用户删除', 100, 4, '', '', '', 'F', 1, 1, 'system:user:remove', '#', 0, 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, '');
INSERT INTO "sys_menu" VALUES (1004, '用户导出', 100, 5, '', '', '', 'F', 1, 1, 'system:user:export', '#', 0, 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, '');
INSERT INTO "sys_menu" VALUES (1005, '用户导入', 100, 6, '', '', '', 'F', 1, 1, 'system:user:import', '#', 0, 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, '');
INSERT INTO "sys_menu" VALUES (1006, '重置密码', 100, 7, '', '', '', 'F', 1, 1, 'system:user:resetPwd', '#', 0, 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, '');

INSERT INTO "sys_menu" VALUES (1007, '角色查询', 101, 1, '', '', '', 'F', 1, 1, 'system:role:query', '#', 0, 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, '');
INSERT INTO "sys_menu" VALUES (1008, '角色新增', 101, 2, '', '', '', 'F', 1, 1, 'system:role:add', '#', 0, 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, '');
INSERT INTO "sys_menu" VALUES (1009, '角色修改', 101, 3, '', '', '', 'F', 1, 1, 'system:role:edit', '#', 0, 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, '');
INSERT INTO "sys_menu" VALUES (1010, '角色删除', 101, 4, '', '', '', 'F', 1, 1, 'system:role:remove', '#', 0, 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, '');

INSERT INTO "sys_menu" VALUES (1011, '菜单查询', 102, 1, '', '', '', 'F', 1, 1, 'system:menu:query', '#', 0, 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, '');
INSERT INTO "sys_menu" VALUES (1012, '菜单新增', 102, 2, '', '', '', 'F', 1, 1, 'system:menu:add', '#', 0, 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, '');
INSERT INTO "sys_menu" VALUES (1013, '菜单修改', 102, 3, '', '', '', 'F', 1, 1, 'system:menu:edit', '#', 0, 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, '');
INSERT INTO "sys_menu" VALUES (1014, '菜单删除', 102, 4, '', '', '', 'F', 1, 1, 'system:menu:remove', '#', 0, 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, '');

INSERT INTO "sys_menu" VALUES (1015, '部门查询', 103, 1, '', '', '', 'F', 1, 1, 'system:dept:query', '#', 0, 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, '');
INSERT INTO "sys_menu" VALUES (1016, '部门新增', 103, 2, '', '', '', 'F', 1, 1, 'system:dept:add', '#', 0, 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, '');
INSERT INTO "sys_menu" VALUES (1017, '部门修改', 103, 3, '', '', '', 'F', 1, 1, 'system:dept:edit', '#', 0, 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, '');
INSERT INTO "sys_menu" VALUES (1018, '部门删除', 103, 4, '', '', '', 'F', 1, 1, 'system:dept:remove', '#', 0, 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, '');

-- 重置菜单序列
SELECT setval('sys_menu_menu_id_seq', (SELECT MAX(menu_id) FROM sys_menu));

-- ----------------------------
-- 用户角色关联表
-- ----------------------------
DROP TABLE IF EXISTS "sys_user_role";
CREATE TABLE "sys_user_role" (
  "user_id" bigint NOT NULL,
  "role_id" bigint NOT NULL,
  PRIMARY KEY ("user_id","role_id")
);

-- ----------------------------
-- 用户角色数据
-- ----------------------------
INSERT INTO "sys_user_role" VALUES (1, 1);
INSERT INTO "sys_user_role" VALUES (2, 2);

-- ----------------------------
-- 角色菜单关联表
-- ----------------------------
DROP TABLE IF EXISTS "sys_role_menu";
CREATE TABLE "sys_role_menu" (
  "role_id" bigint NOT NULL,
  "menu_id" bigint NOT NULL,
  PRIMARY KEY ("role_id","menu_id")
);

-- ----------------------------
-- 角色菜单数据 (普通角色)
-- ----------------------------
INSERT INTO "sys_role_menu" VALUES (2, 1);
INSERT INTO "sys_role_menu" VALUES (2, 100);
INSERT INTO "sys_role_menu" VALUES (2, 1000);

-- ----------------------------
-- 操作日志表
-- ----------------------------
DROP TABLE IF EXISTS "sys_oper_log";
CREATE TABLE "sys_oper_log" (
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
CREATE INDEX "idx_oper_time" ON "sys_oper_log" ("oper_time");

-- ----------------------------
-- 登录日志表
-- ----------------------------
DROP TABLE IF EXISTS "sys_login_log";
CREATE TABLE "sys_login_log" (
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
CREATE INDEX "idx_login_time" ON "sys_login_log" ("login_time");
