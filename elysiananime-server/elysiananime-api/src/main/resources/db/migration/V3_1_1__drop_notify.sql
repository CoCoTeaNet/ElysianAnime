-- 3.1.1 通知功能下线：删除 sys_notify 表（不可回滚，回滚旧版本需按旧 DDL 重建）
DROP TABLE IF EXISTS `sys_notify`;
