-- =====================================================================
-- V3.1.0 媒体索引 / 每集播放进度 / 更新动态
--
-- 执行器约定（见 SchemaMigrator）：
--   1. 语句按分号切分，脚本里不要写 DELIMITER / 存储过程，不要依赖注释里的分号
--   2. CREATE TABLE 必须带 IF NOT EXISTS
--   3. ALTER 每条语句只带一个子句（ADD COLUMN / ADD KEY / DROP INDEX），
--      执行器会先查 information_schema 再决定执行或跳过，因此脚本可重复执行
--   4. 本脚本只做结构变更与存量数据整理，业务数据回填在 P2 / P3 的启动任务里
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. 媒体文件索引
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `ani_media` (
  `id`              bigint        NOT NULL                COMMENT '媒体文件id',
  `opus_id`         bigint        NOT NULL                COMMENT '作品id',
  `file_path`       varchar(1024) NOT NULL                COMMENT '文件绝对路径（已归一化），仅服务端使用',
  `file_path_hash`  char(32)      NOT NULL                COMMENT '归一化路径的MD5，用于唯一索引',
  `relative_path`   varchar(512)  NOT NULL                COMMENT '相对所属资源根的路径，用于展示',
  `file_name`       varchar(255)  NOT NULL                COMMENT '文件名（含扩展名）',
  `extension`       varchar(16)   NOT NULL                COMMENT '扩展名，小写、不带点',
  `episode_no`      decimal(8,2)  DEFAULT NULL            COMMENT '解析出的集数，SP 与无集数文件为空',
  `episode_key`     varchar(64)   NOT NULL                COMMENT '逻辑集数键：1 / 12.5 / SP1 / X+文件名MD5前8位',
  `display_name`    varchar(255)  NOT NULL                COMMENT '列表展示名：第N集 / SPn / 文件名去扩展名',
  `file_size`       bigint        NOT NULL                COMMENT '文件字节数',
  `file_mtime`      datetime      NOT NULL                COMMENT '文件修改时间，用于增量比对',
  `sort_no`         decimal(10,3) DEFAULT NULL            COMMENT '排序号：正片=集数，SP=10000+序号，无集数为空',
  `is_primary`      tinyint       NOT NULL DEFAULT '1'    COMMENT '同集是否主文件：1是 0否（同集冗余，仅索引不展示）',
  `last_seen_time`  datetime      NOT NULL                COMMENT '最近一次扫描确认存在的时间（按部批量刷新）',
  `create_by`       bigint        DEFAULT NULL            COMMENT '创建人',
  `create_time`     datetime      NOT NULL                COMMENT '创建时间',
  `update_by`       bigint        DEFAULT NULL            COMMENT '更新人',
  `update_time`     datetime      NOT NULL                COMMENT '更新时间',
  `is_deleted`      tinyint       NOT NULL DEFAULT '0'    COMMENT '是否删除：0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ani_media_uk_1` (`opus_id`,`file_path_hash`),
  KEY `ani_media_idx_1` (`opus_id`,`is_deleted`,`is_primary`,`sort_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='媒体文件索引表';

-- ---------------------------------------------------------------------
-- 2. 用户每集播放进度
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `ani_user_opus_progress` (
  `id`           bigint      NOT NULL                COMMENT '进度id',
  `user_id`      bigint      NOT NULL                COMMENT '用户id',
  `opus_id`      bigint      NOT NULL                COMMENT '作品id',
  `episode_key`  varchar(64) NOT NULL                COMMENT '逻辑集数键：1 / 12.5 / SP1 / X+文件名MD5前8位',
  `position_ms`  bigint      NOT NULL DEFAULT '0'    COMMENT '播放位置（毫秒）',
  `duration_ms`  bigint      DEFAULT NULL            COMMENT '视频总时长（毫秒）',
  `watched_ms`   bigint      NOT NULL DEFAULT '0'    COMMENT '累计观看时长（毫秒），用于状态判定',
  `is_completed` tinyint     NOT NULL DEFAULT '0'    COMMENT '是否看完：0否 1是',
  `session_id`   varchar(36) DEFAULT NULL            COMMENT '播放会话id，用于识别跨端会话',
  `seq`          int         NOT NULL DEFAULT '0'    COMMENT '会话内上报序号，同会话内小于已存值则丢弃',
  `create_by`    bigint      DEFAULT NULL            COMMENT '创建人',
  `create_time`  datetime    NOT NULL                COMMENT '创建时间',
  `update_by`    bigint      DEFAULT NULL            COMMENT '更新人',
  `update_time`  datetime    NOT NULL                COMMENT '更新时间',
  `is_deleted`   tinyint     NOT NULL DEFAULT '0'    COMMENT '是否删除：0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ani_user_opus_progress_uk_1` (`user_id`,`opus_id`,`episode_key`),
  KEY `ani_user_opus_progress_idx_1` (`opus_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='用户每集播放进度表';

-- ---------------------------------------------------------------------
-- 3. 作品更新动态
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `ani_opus_update` (
  `id`          bigint       NOT NULL                COMMENT '更新动态id',
  `opus_id`     bigint       NOT NULL                COMMENT '作品id',
  `media_id`    bigint       DEFAULT NULL            COMMENT '媒体文件id',
  `episode_key` varchar(64)  NOT NULL                COMMENT '逻辑集数键',
  `episode_no`  decimal(8,2) DEFAULT NULL            COMMENT '集数，SP/OVA 与无集数文件为空',
  `source`      tinyint      NOT NULL DEFAULT '1'    COMMENT '来源：1扫描 2RSS 3手动上传',
  `create_by`   bigint       DEFAULT NULL            COMMENT '创建人',
  `create_time` datetime     NOT NULL                COMMENT '创建时间，回填时取文件 mtime',
  `update_by`   bigint       DEFAULT NULL            COMMENT '更新人',
  `update_time` datetime     NOT NULL                COMMENT '更新时间',
  `is_deleted`  tinyint      NOT NULL DEFAULT '0'    COMMENT '是否删除：0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ani_opus_update_uk_1` (`opus_id`,`episode_key`),
  KEY `ani_opus_update_idx_1` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='作品更新动态表';

-- ---------------------------------------------------------------------
-- 4. 作品主表新增媒体索引字段
-- ---------------------------------------------------------------------
ALTER TABLE `ani_opus` ADD COLUMN `media_dir` varchar(512) DEFAULT NULL COMMENT '资源目录绝对路径';
ALTER TABLE `ani_opus` ADD COLUMN `media_count` int NOT NULL DEFAULT '0' COMMENT '已索引媒体数';
ALTER TABLE `ani_opus` ADD COLUMN `media_latest_key` varchar(64) DEFAULT NULL COMMENT '最新一集的 episode_key';
ALTER TABLE `ani_opus` ADD COLUMN `media_scan_time` datetime DEFAULT NULL COMMENT '最近一次扫描成功时间';

-- ---------------------------------------------------------------------
-- 5. 存量 init 脚本漂移补齐：total_time 与 ani_user_feed
--    这两个变更以前只写在 ddl-update-sql.sql 里，老库可能没执行过
-- ---------------------------------------------------------------------
ALTER TABLE `ani_user_opus` ADD COLUMN `total_time` bigint DEFAULT NULL COMMENT '视频总时长';

CREATE TABLE IF NOT EXISTS `ani_user_feed` (
  `id`           bigint   NOT NULL COMMENT '投喂id',
  `from_user_id` bigint   NOT NULL COMMENT '投喂人id',
  `to_user_id`   bigint   NOT NULL COMMENT '被投喂人id',
  `opus_id`      bigint   NOT NULL COMMENT '作品id',
  `create_time`  datetime NOT NULL COMMENT '投喂时间',
  `is_deleted`   tinyint  NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='用户投喂关联表';

ALTER TABLE `ani_user_feed` ADD KEY `ani_user_feed_idx_1` (`to_user_id`,`is_deleted`);

-- ---------------------------------------------------------------------
-- 6. 用户作品关联表：新增"最近观看"字段
-- ---------------------------------------------------------------------
ALTER TABLE `ani_user_opus` ADD COLUMN `last_episode_key` varchar(64) DEFAULT NULL COMMENT '最近观看的 episode_key';
ALTER TABLE `ani_user_opus` ADD COLUMN `last_media_id` bigint DEFAULT NULL COMMENT '最近观看的媒体文件id';
ALTER TABLE `ani_user_opus` ADD COLUMN `last_read_time` datetime DEFAULT NULL COMMENT '最近观看时间';

-- ---------------------------------------------------------------------
-- 7. 去重：同一 (user_id, opus_id) 只保留一行，否则无法建唯一索引
--    合并规则：reading_time 最大的一行为基础行；is_share 取 MAX；
--    read_status 按 1 > 2 > 0 取优先级；create_time 取最早；resource_url 取非空的一条
--    物理删除不可逆，回滚不会恢复（见 doc/sql/upgrade-3.1.0.md）
-- ---------------------------------------------------------------------
UPDATE `ani_user_opus` t
JOIN (
  SELECT `user_id`,
         `opus_id`,
         MAX(`is_share`) AS is_share,
         CASE
           WHEN SUM(`read_status` = 1) > 0 THEN 1
           WHEN SUM(`read_status` = 2) > 0 THEN 2
           ELSE MIN(`read_status`)
         END AS read_status,
         MIN(`create_time`) AS create_time,
         MIN(`resource_url`) AS resource_url
  FROM `ani_user_opus`
  GROUP BY `user_id`, `opus_id`
  HAVING COUNT(*) > 1
) g ON g.`user_id` = t.`user_id` AND g.`opus_id` = t.`opus_id`
SET t.`is_share` = g.is_share,
    t.`read_status` = g.read_status,
    t.`create_time` = g.create_time,
    t.`resource_url` = COALESCE(g.resource_url, t.`resource_url`);

DELETE t
FROM `ani_user_opus` t
JOIN `ani_user_opus` k
  ON k.`user_id` = t.`user_id`
 AND k.`opus_id` = t.`opus_id`
 AND k.`id` <> t.`id`
 AND (k.`reading_time` > t.`reading_time`
      OR (k.`reading_time` = t.`reading_time` AND k.`id` > t.`id`));

ALTER TABLE `ani_user_opus` ADD UNIQUE KEY `ani_user_opus_uk_1` (`user_id`,`opus_id`);
ALTER TABLE `ani_user_opus` ADD KEY `ani_user_opus_idx_2` (`user_id`,`last_read_time`);
ALTER TABLE `ani_user_opus` DROP INDEX `ani_user_opus_idx_1`;

-- ---------------------------------------------------------------------
-- 8. 番剧首页从 hash 路由改到后台布局内的独立路由
-- ---------------------------------------------------------------------
UPDATE `sys_menu` SET `router_path` = '/admin/ani-home' WHERE `router_path` = '/#/anime/home';
