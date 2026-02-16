-- ===============================================
-- Seed Cloud Microservices - 开发环境重置脚本
-- ===============================================
-- ⚠️ 警告：此脚本会删除所有数据！仅用于开发/测试环境！
-- ⚠️ 生产环境严禁使用！
-- ===============================================

-- 删除所有表 (按依赖顺序)
DROP TABLE IF EXISTS "sys_login_log" CASCADE;
DROP TABLE IF EXISTS "sys_oper_log" CASCADE;
DROP TABLE IF EXISTS "sys_role_menu" CASCADE;
DROP TABLE IF EXISTS "sys_user_role" CASCADE;
DROP TABLE IF EXISTS "sys_menu" CASCADE;
DROP TABLE IF EXISTS "sys_role" CASCADE;
DROP TABLE IF EXISTS "sys_user" CASCADE;
DROP TABLE IF EXISTS "sys_dept" CASCADE;

-- 重新创建表结构
\i schema.sql

-- 填充初始数据
\i seed.sql

-- 完成提示
DO $$ BEGIN RAISE NOTICE '数据库已重置完成！'; END $$;
