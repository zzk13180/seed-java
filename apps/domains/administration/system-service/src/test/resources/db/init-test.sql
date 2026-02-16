-- 测试数据库初始化脚本
-- 用于 Testcontainers 集成测试 (PostgreSQL)

-- 创建系统用户表
CREATE TABLE IF NOT EXISTS sys_user (
    user_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    avatar VARCHAR(255),
    sex SMALLINT DEFAULT 0,
    status SMALLINT DEFAULT 1,
    dept_id BIGINT,
    tenant_id VARCHAR(64),
    create_by VARCHAR(64),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64),
    update_time TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    remark VARCHAR(500)
);

-- 创建角色表
CREATE TABLE IF NOT EXISTS sys_role (
    role_id BIGSERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL,
    role_key VARCHAR(50) NOT NULL UNIQUE,
    sort INT DEFAULT 0,
    data_scope SMALLINT DEFAULT 1,
    status SMALLINT DEFAULT 1,
    remark VARCHAR(500),
    tenant_id VARCHAR(64),
    create_by VARCHAR(64),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64),
    update_time TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

-- 创建部门表
CREATE TABLE IF NOT EXISTS sys_dept (
    dept_id BIGSERIAL PRIMARY KEY,
    parent_id BIGINT DEFAULT 0,
    ancestors VARCHAR(500) DEFAULT '',
    dept_name VARCHAR(50) NOT NULL,
    sort INT DEFAULT 0,
    leader VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    status SMALLINT DEFAULT 1,
    tenant_id VARCHAR(64),
    create_by VARCHAR(64),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64),
    update_time TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

-- 创建菜单表
CREATE TABLE IF NOT EXISTS sys_menu (
    menu_id BIGSERIAL PRIMARY KEY,
    menu_name VARCHAR(50) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    sort INT DEFAULT 0,
    path VARCHAR(200),
    component VARCHAR(255),
    query VARCHAR(255),
    is_frame SMALLINT DEFAULT 0,
    is_cache SMALLINT DEFAULT 1,
    menu_type CHAR(1),
    visible SMALLINT DEFAULT 1,
    status SMALLINT DEFAULT 1,
    perms VARCHAR(100),
    icon VARCHAR(100),
    remark VARCHAR(500),
    create_by VARCHAR(64),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64),
    update_time TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

-- 角色菜单关联表
CREATE TABLE IF NOT EXISTS sys_role_menu (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id)
);

-- 插入测试数据
INSERT INTO sys_user (user_id, username, password, nickname, email, status) VALUES
(1, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '管理员', 'admin@example.com', 1);

-- 重置序列，确保新插入的记录使用更大的 ID
SELECT setval('sys_user_user_id_seq', 100);
SELECT setval('sys_role_role_id_seq', 100);
SELECT setval('sys_dept_dept_id_seq', 100);
SELECT setval('sys_menu_menu_id_seq', 100);

INSERT INTO sys_role (role_id, role_name, role_key, status) VALUES
(1, '超级管理员', 'admin', 1),
(2, '普通用户', 'user', 1);

INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1);

INSERT INTO sys_dept (dept_id, dept_name, parent_id, status) VALUES
(1, '总公司', 0, 1),
(2, '技术部', 1, 1),
(3, '产品部', 1, 1);

-- 插入菜单权限数据（用于权限测试）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, menu_type, perms, status) VALUES
(1, '系统管理', 0, 'M', '', 1),
(2, '用户管理', 1, 'C', 'system:user:list', 1),
(3, '用户新增', 2, 'F', 'system:user:add', 1),
(4, '用户修改', 2, 'F', 'system:user:edit', 1),
(5, '用户删除', 2, 'F', 'system:user:remove', 1);

-- 角色菜单关联（admin角色拥有所有权限）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5);
