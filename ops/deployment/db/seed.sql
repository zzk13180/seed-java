-- ===============================================
-- Seed Cloud Microservices - 初始化数据
-- ===============================================
-- 仅包含 INSERT 语句
-- 适用于：首次部署后填充初始数据
-- 前置条件：先执行 schema.sql 创建表结构
-- ===============================================

-- ----------------------------
-- 部门数据
-- ----------------------------
INSERT INTO "sys_dept" ("dept_id", "parent_id", "ancestors", "dept_name", "sort", "leader", "phone", "email", "status", "deleted", "create_by", "create_time", "update_by", "update_time")
VALUES
  (100, 0, '0', '总公司', 0, '管理员', '15888888888', 'admin@seed.com', 1, 0, 'admin', CURRENT_TIMESTAMP, '', NULL),
  (101, 100, '0,100', '深圳分公司', 1, 'user', '15888888888', 'user@seed.com', 1, 0, 'admin', CURRENT_TIMESTAMP, '', NULL),
  (102, 100, '0,100', '长沙分公司', 2, 'user', '15888888888', 'user@seed.com', 1, 0, 'admin', CURRENT_TIMESTAMP, '', NULL),
  (103, 101, '0,100,101', '研发部门', 1, 'user', '15888888888', 'user@seed.com', 1, 0, 'admin', CURRENT_TIMESTAMP, '', NULL),
  (104, 101, '0,100,101', '市场部门', 2, 'user', '15888888888', 'user@seed.com', 1, 0, 'admin', CURRENT_TIMESTAMP, '', NULL),
  (105, 101, '0,100,101', '测试部门', 3, 'user', '15888888888', 'user@seed.com', 1, 0, 'admin', CURRENT_TIMESTAMP, '', NULL),
  (106, 101, '0,100,101', '财务部门', 4, 'user', '15888888888', 'user@seed.com', 1, 0, 'admin', CURRENT_TIMESTAMP, '', NULL),
  (107, 101, '0,100,101', '运维部门', 5, 'user', '15888888888', 'user@seed.com', 1, 0, 'admin', CURRENT_TIMESTAMP, '', NULL),
  (108, 102, '0,100,102', '市场部门', 1, 'user', '15888888888', 'user@seed.com', 1, 0, 'admin', CURRENT_TIMESTAMP, '', NULL),
  (109, 102, '0,100,102', '财务部门', 2, 'user', '15888888888', 'user@seed.com', 1, 0, 'admin', CURRENT_TIMESTAMP, '', NULL)
ON CONFLICT DO NOTHING;

-- 重置部门序列
SELECT setval('sys_dept_dept_id_seq', (SELECT MAX(dept_id) FROM sys_dept));

-- ----------------------------
-- 用户数据 (密码: admin123)
-- ----------------------------
INSERT INTO "sys_user" ("user_id", "dept_id", "username", "password", "nickname", "email", "phone", "sex", "avatar", "status", "deleted", "login_ip", "login_date", "create_by", "create_time", "update_by", "update_time", "remark")
VALUES
  (1, 103, 'admin', '$2b$12$5XuJLpiEjPusp7aSGT1fJOQqsLyUNupeExGgPwBZSuVxErTi43laW', '系统管理员', 'admin@seed.com', '15888888888', 1, '', 1, 0, '127.0.0.1', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, '', NULL, '管理员'),
  (2, 105, 'user', '$2b$12$5XuJLpiEjPusp7aSGT1fJOQqsLyUNupeExGgPwBZSuVxErTi43laW', '普通用户', 'user@seed.com', '15666666666', 1, '', 1, 0, '127.0.0.1', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, '', NULL, '普通用户')
ON CONFLICT (username) DO NOTHING;

-- 重置用户序列
SELECT setval('sys_user_user_id_seq', (SELECT MAX(user_id) FROM sys_user));

-- ----------------------------
-- 角色数据
-- ----------------------------
INSERT INTO "sys_role" ("role_id", "role_name", "role_key", "sort", "data_scope", "status", "deleted", "create_by", "create_time", "update_by", "update_time", "remark")
VALUES
  (1, '超级管理员', 'admin', 1, 1, 1, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, '超级管理员'),
  (2, '普通角色', 'common', 2, 2, 1, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, '普通角色')
ON CONFLICT DO NOTHING;

SELECT setval('sys_role_role_id_seq', (SELECT MAX(role_id) FROM sys_role));

-- ----------------------------
-- 菜单数据
-- ----------------------------
-- 一级菜单
INSERT INTO "sys_menu" ("menu_id", "menu_name", "parent_id", "sort", "path", "component", "query", "menu_type", "visible", "status", "perms", "icon", "is_cache", "is_frame", "create_by", "create_time", "update_by", "update_time", "remark")
VALUES
  (1, '系统管理', 0, 1, 'system', NULL, '', 'M', 1, 1, '', 'system', 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, '系统管理目录'),
  (2, '系统监控', 0, 2, 'monitor', NULL, '', 'M', 1, 1, '', 'monitor', 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, '系统监控目录'),
  (3, '系统工具', 0, 3, 'tool', NULL, '', 'M', 1, 1, '', 'tool', 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, '系统工具目录'),
  -- 二级菜单
  (100, '用户管理', 1, 1, 'user', 'system/user/index', '', 'C', 1, 1, 'system:user:list', 'user', 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, '用户管理菜单'),
  (101, '角色管理', 1, 2, 'role', 'system/role/index', '', 'C', 1, 1, 'system:role:list', 'peoples', 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, '角色管理菜单'),
  (102, '菜单管理', 1, 3, 'menu', 'system/menu/index', '', 'C', 1, 1, 'system:menu:list', 'tree-table', 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, '菜单管理菜单'),
  (103, '部门管理', 1, 4, 'dept', 'system/dept/index', '', 'C', 1, 1, 'system:dept:list', 'tree', 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, '部门管理菜单'),
  -- 按钮权限
  (1000, '用户查询', 100, 1, '', '', '', 'F', 1, 1, 'system:user:query', '#', 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, ''),
  (1001, '用户新增', 100, 2, '', '', '', 'F', 1, 1, 'system:user:add', '#', 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, ''),
  (1002, '用户修改', 100, 3, '', '', '', 'F', 1, 1, 'system:user:edit', '#', 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, ''),
  (1003, '用户删除', 100, 4, '', '', '', 'F', 1, 1, 'system:user:remove', '#', 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, ''),
  (1004, '用户导出', 100, 5, '', '', '', 'F', 1, 1, 'system:user:export', '#', 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, ''),
  (1005, '用户导入', 100, 6, '', '', '', 'F', 1, 1, 'system:user:import', '#', 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, ''),
  (1006, '重置密码', 100, 7, '', '', '', 'F', 1, 1, 'system:user:resetPwd', '#', 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, ''),
  (1007, '角色查询', 101, 1, '', '', '', 'F', 1, 1, 'system:role:query', '#', 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, ''),
  (1008, '角色新增', 101, 2, '', '', '', 'F', 1, 1, 'system:role:add', '#', 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, ''),
  (1009, '角色修改', 101, 3, '', '', '', 'F', 1, 1, 'system:role:edit', '#', 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, ''),
  (1010, '角色删除', 101, 4, '', '', '', 'F', 1, 1, 'system:role:remove', '#', 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, ''),
  (1011, '菜单查询', 102, 1, '', '', '', 'F', 1, 1, 'system:menu:query', '#', 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, ''),
  (1012, '菜单新增', 102, 2, '', '', '', 'F', 1, 1, 'system:menu:add', '#', 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, ''),
  (1013, '菜单修改', 102, 3, '', '', '', 'F', 1, 1, 'system:menu:edit', '#', 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, ''),
  (1014, '菜单删除', 102, 4, '', '', '', 'F', 1, 1, 'system:menu:remove', '#', 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, ''),
  (1015, '部门查询', 103, 1, '', '', '', 'F', 1, 1, 'system:dept:query', '#', 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, ''),
  (1016, '部门新增', 103, 2, '', '', '', 'F', 1, 1, 'system:dept:add', '#', 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, ''),
  (1017, '部门修改', 103, 3, '', '', '', 'F', 1, 1, 'system:dept:edit', '#', 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, ''),
  (1018, '部门删除', 103, 4, '', '', '', 'F', 1, 1, 'system:dept:remove', '#', 0, 0, 'admin', CURRENT_TIMESTAMP, '', NULL, '')
ON CONFLICT DO NOTHING;

SELECT setval('sys_menu_menu_id_seq', (SELECT MAX(menu_id) FROM sys_menu));

-- ----------------------------
-- 用户角色关联数据
-- ----------------------------
INSERT INTO "sys_user_role" ("user_id", "role_id")
VALUES
  (1, 1),
  (2, 2)
ON CONFLICT DO NOTHING;

-- ----------------------------
-- 角色菜单关联数据 (普通角色)
-- ----------------------------
INSERT INTO "sys_role_menu" ("role_id", "menu_id")
VALUES
  (2, 1),
  (2, 100),
  (2, 1000)
ON CONFLICT DO NOTHING;
