# 升级到 3.1.0

本版本引入媒体文件索引、按集播放进度与更新动态，删除系统通知模块。数据库结构由启动期迁移执行器自动升级，**不需要手工执行 SQL**。

## 1. 升级过程

1. 备份数据库（**必须**，详见第 3 节「不可逆变更」）
2. 停旧实例 → 替换镜像 / 部署包 → 启动
3. 启动日志中出现以下内容即代表迁移成功：
   - `数据库迁移 >>> 开始执行 V3_1_0__media_and_progress.sql`
   - `数据库迁移 >>> 执行完成 V3_1_0__media_and_progress.sql`
   - 或者 `数据库迁移 >>> 检测到已是最新全量结构，写入基线 3.1.0`（全新建库）
4. 迁移失败时进程直接退出（不会带病启动）。修复原因后重启即可，脚本本身幂等，已生效的列 / 索引会跳过

迁移执行器行为：

- 脚本位置 `elysiananime-api/src/main/resources/db/migration/`，按文件名顺序执行
- 执行记录写入 `sys_schema_version`（`version` / `script_name` / `checksum` / `apply_time`）
- 多实例同时启动时用 MySQL 命名锁 `ea_schema_migrate` 互斥，抢不到锁的实例直接退出
- 脚本内容被改动过（checksum 不一致）会终止启动并报错，不允许静默跳过

## 2. 结构变更

| 对象 | 变更 |
|---|---|
| `ani_media` | 新建，媒体文件索引；唯一键 `(opus_id, file_path_hash)` |
| `ani_user_opus_progress` | 新建，每集播放进度；唯一键 `(user_id, opus_id, episode_key)` |
| `ani_opus_update` | 新建，更新动态；唯一键 `(opus_id, episode_key)` |
| `sys_schema_version` | 新建，迁移版本记录（执行器自动创建） |
| `ani_opus` | 新增 `media_dir` / `media_count` / `media_latest_key` / `media_scan_time` |
| `ani_user_opus` | 新增 `last_episode_key` / `last_media_id` / `last_read_time` / `total_time`；按 `(user_id, opus_id)` 去重；新增唯一键 `ani_user_opus_uk_1`、索引 `ani_user_opus_idx_2`，删除索引 `ani_user_opus_idx_1` |
| `ani_user_feed` | 补齐到标准初始化脚本（老库若已手工建过则跳过） |
| `sys_menu` | `/#/anime/home` 改为 `/admin/ani-home` |

`ani_user_opus` 的 `reading_num` / `reading_time` / `total_time` / `resource_url` 在 3.1.0 期间继续双写，**不删除**，3.2 再清理。

## 3. 回滚

回滚到旧镜像前确认以下事项：

1. **`ani_user_opus` 去重不可逆**：同一 `(user_id, opus_id)` 的重复记录已在迁移中物理删除，回滚不会恢复。升级前务必留库备份
2. **停写旧列的时点**：3.1.0 的新接口只写新表，同时双写旧列；如果之后升级到停写旧列的版本，回滚旧镜像后首页进度会冻结在停写那一刻
3. **`sys_notify` 删除不可回滚**（P3 起）：通知表被 DROP，回滚旧镜像后通知相关接口 / 定时任务会报表不存在，需要按旧版本的 DDL 重建
4. **唯一索引带来的行为变化**：`ani_user_opus_uk_1` 生效后，旧代码并发双击追番会从"产生重复行"变成抛 DuplicateKey。旧代码是先查后插，正常路径不受影响

需要彻底回退结构时，反向操作如下（确认数据已备份）：

```sql
-- 恢复旧索引
ALTER TABLE `ani_user_opus` ADD KEY `ani_user_opus_idx_1` (`user_id`,`opus_id`);
ALTER TABLE `ani_user_opus` DROP INDEX `ani_user_opus_uk_1`;
ALTER TABLE `ani_user_opus` DROP INDEX `ani_user_opus_idx_2`;

-- 删除新增列（旧代码不感知这些列，也可以保留不删）
ALTER TABLE `ani_user_opus` DROP COLUMN `last_episode_key`;
ALTER TABLE `ani_user_opus` DROP COLUMN `last_media_id`;
ALTER TABLE `ani_user_opus` DROP COLUMN `last_read_time`;
ALTER TABLE `ani_opus` DROP COLUMN `media_dir`;
ALTER TABLE `ani_opus` DROP COLUMN `media_count`;
ALTER TABLE `ani_opus` DROP COLUMN `media_latest_key`;
ALTER TABLE `ani_opus` DROP COLUMN `media_scan_time`;

-- 新表可整体删除，旧代码不使用
DROP TABLE IF EXISTS `ani_media`;
DROP TABLE IF EXISTS `ani_user_opus_progress`;
DROP TABLE IF EXISTS `ani_opus_update`;

-- 菜单改回 hash 路由（旧前端）
UPDATE `sys_menu` SET `router_path` = '/#/anime/home' WHERE `router_path` = '/admin/ani-home';

-- 迁移记录：删除后重启会重新执行 V3_1_0（脚本幂等）
DELETE FROM `sys_schema_version` WHERE `version` = '3.1.0';
```

## 4. 升级后仍需执行的一次性任务

| 任务 | 触发方式 | 说明 |
|---|---|---|
| 媒体全量扫描 | 首次启动自动触发，也可 `POST /anime/media/scan`（管理员） | 生成 `ani_media` 与 `ani_opus.media_*` |
| 旧进度回填 | 扫描完成后自动执行，写入标记 `3.1.0-backfill-progress` | 只补缺失行，不覆盖改造后的新进度 |
| 更新动态回填 | 随 3.1.0 首次扫描执行，写入标记 `3.1.0-backfill-update` | 每部番只回填最新一集 |

回填标记同样记录在 `sys_schema_version`，重复启动不会重复回填。
