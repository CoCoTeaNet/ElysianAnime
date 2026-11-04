# ElysianAnime 播放与媒体模块重做 · 总体计划

> 状态：设计中（未开工）
> 最后更新：2026-10-30
> 本文是本次改造的唯一总计划，设计与实施过程中持续更新，不另建分散文档。

**状态标记**

- ✅ 已确认（用户拍板）
- 🟡 默认执行（按推荐方案实现，未提出异议即生效）
- ⬜ 待定（需要讨论后确定）

---

## 1. 背景与目标

### 1.1 现状问题

| 领域 | 问题概述 |
|---|---|
| 播放进度 | 一部番只有一个进度标量（`ani_user_opus.reading_num/reading_time`），无法按集记忆；状态机只有"未看→在看"，永不自动"已看"；未追番无法保存进度；IDOR |
| 媒体资源 | 首页列表、详情页每次请求都扫磁盘（`AniOpusServiceImpl.listByUser/getOpusMedia`），NAS 上列表页明显变慢；资源以客户端文件名拼路径播放，存在任意文件读取；无 Range，拖动进度条整文件重传 |
| 通知 | 全局通知每 60 秒被复制成每个用户一条（`SysNotifyJobs`），重复执行、重复发邮件；通知内容只有"资源名：xxx.mp4"，信息量低 |
| 前端 | Element Plus 全量引入（index.js 1.5MB）；播放页内嵌播放器受 keep-alive 影响不销毁；进度每 2.5 秒全量写库；首页卡片无懒加载 |
| 工程化 | 无数据库升级机制，老用户升级靠手工执行 SQL |

### 1.2 本次目标

1. 播放进度按"每集"存储，完整兼容历史进度
2. 媒体资源改为定时/事件驱动的扫描索引，请求路径零磁盘 IO（参考 Jellyfin / fnOS TV）
3. 播放卸载到弹窗，详情页常驻不占解码器
4. 删除通知系统，改为「更新动态」信息流（首页 + 仪表盘）
5. 上一轮审计列出的 20 个进度相关问题全部关闭，并有回归用例

---

## 2. 已确认决策

| 日期 | 决策 | 状态 |
|---|---|---|
| 2026-10-27 | 删除通知系统，**邮件通知一并删除** | ✅ |
| 2026-10-27 | `sys_notify` 直接 DROP，不归档 | ✅ |
| 2026-10-27 | 更新动态并入现有「仪表盘」路由，不新增菜单 | ✅ |
| 2026-10-27 | 详情页播放窗口改为弹窗播放 | ✅ |
| 2026-10-27 | 进度模型改为"每集一条"，状态机收敛到服务端 | ✅ |
| 2026-10-27 | 媒体列表/详情全部读 DB，文件系统只由扫描器访问 | ✅ |
| 2026-10-27 | 播放首次上报时自动创建追番记录 | ✅ |
| 2026-10-27 | 旧列 `reading_num/reading_time/total_time/resource_url` 保留，3.2 删除 | ✅ |
| 2026-10-27 | 首页**视觉重构**（不沿用现有暗色卡片），另出 UI 子计划：首页 / 详情页 / 播放层 | ✅ |
| 2026-10-27 | 扫描到的孤儿目录只报告，不自动建条目 | ✅ |
| 2026-10-27 | 扫描间隔可配置（`media.scan.interval`，默认 15 分钟）+ 事件触发 | ✅ |
| 2026-10-27 | 本轮不做剧集缩略图（不引入 ffmpeg） | ✅ |

---

## 3. 总体架构变化

```
【现在】
浏览器 → 列表接口 → 逐行 FileUtil.ls 扫盘 → 响应
浏览器 → 详情接口 → 扫盘 → 响应
播放器 → /media/{opusId}?resName=用户输入的集数 → 拼路径 → 整文件下载

【改造后】
定时/事件/手动 ──→ MediaScanner ──→ ani_media（文件索引）
                                └─→ ani_opus_update（更新动态）
浏览器 → 列表接口 → 纯 SQL（ani_opus + ani_user_opus + progress）
浏览器 → 详情接口 → 纯 SQL（ani_media + progress）
播放器 → /anime/media/{mediaId}/stream → 查索引 → 校验根目录 → Range 分段响应
播放器 → /anime/userOpus/progress → 状态机 → progress 表
```

核心约束：

- 任何 HTTP 请求处理链路中**不允许出现文件系统扫描**
- 播放地址**不接受用户提供的文件路径或文件名**
- 所有写接口的 `user_id` 一律取自登录态
- `<video>` 无法携带自定义请求头，媒体流接口只按 cookie 鉴权，不做 header token

---

## 4. 数据模型

### 4.1 表设计约定

新建表与存量表新增列一律遵循以下约定，与现有 `doc/sql/ddl-sql.sql` 保持一致：

| 项 | 约定 |
|---|---|
| 命名 | 表名、字段名全小写下划线；作品域表以 `ani_` 开头 |
| 主键 | `id bigint NOT NULL`，由 sqltoy 雪花算法生成，**不使用 AUTO_INCREMENT** |
| 注释 | 每个字段必须带中文 `COMMENT`，枚举值在注释里列全（如 `0否 1是`） |
| 审计字段 | 业务表统一 `create_by` / `create_time` / `update_by` / `update_time` / `is_deleted`，顺序固定 |
| 时间 | 统一 `datetime`，字段名以 `_time` 结尾；本模块无毫秒级需求，不用 `DATETIME(3)` |
| 布尔 | 统一 `tinyint`，`0` 否 `1` 是，默认值写 `'0'` |
| 索引 | 普通索引 `{表名}_idx_N`，唯一索引 `{表名}_uk_N`；`N` 从 1 递增 |
| 索引长度 | 单个索引的列总长度不超过 3072 字节（utf8mb4 按 4 字节/字符计）；长字符串列用哈希列参与唯一索引 |
| 表选项 | `ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='...'` |
| 删除 | 一律软删除（`is_deleted`），索引默认带 `is_deleted` 或只查未删行 |
| 兼容 | 新增列必须有默认值或允许 `NULL`，旧代码不感知；不改旧列语义 |
| 幂等 | 扫描、回填、迁移脚本重复执行结果一致 |

存量表保持 `utf8mb3_unicode_ci` 不动；新表用 `utf8mb4` 以容纳文件名中的 emoji，关联键均为 `bigint`，不影响 JOIN 索引。

### 4.2 新表 `ani_media`（媒体文件索引）

```sql
CREATE TABLE `ani_media` (
  `id`             bigint        NOT NULL                COMMENT '媒体文件id',
  `opus_id`        bigint        NOT NULL                COMMENT '作品id',
  `file_path`       varchar(1024) NOT NULL                COMMENT '文件绝对路径（已归一化），仅服务端使用',
  `file_path_hash`  char(32)      NOT NULL                COMMENT '归一化路径的MD5，用于唯一索引',
  `relative_path`  varchar(512)  NOT NULL                COMMENT '相对所属资源根的路径，用于展示',
  `file_name`      varchar(255)  NOT NULL                COMMENT '文件名（含扩展名）',
  `extension`      varchar(16)   NOT NULL                COMMENT '扩展名，小写、不带点',
  `episode_no`     decimal(8,2)  DEFAULT NULL            COMMENT '解析出的集数，SP 与无集数文件为空',
  `episode_key`    varchar(64)   NOT NULL                COMMENT '逻辑集数键：1 / 12.5 / SP1 / X+文件名MD5前8位',
  `display_name`   varchar(255)  NOT NULL                COMMENT '列表展示名：第N集 / SPn / 文件名去扩展名',
  `file_size`      bigint        NOT NULL                COMMENT '文件字节数',
  `file_mtime`     datetime      NOT NULL                COMMENT '文件修改时间，用于增量比对',
  `sort_no`        decimal(10,3) DEFAULT NULL            COMMENT '排序号：正片=集数，SP=10000+序号，无集数为空',
  `is_primary`     tinyint       NOT NULL DEFAULT '1'    COMMENT '同集是否主文件：1是 0否（同集冗余，仅索引不展示）',
  `last_seen_time` datetime      NOT NULL                COMMENT '最近一次扫描确认存在的时间（按部批量刷新）',
  `create_by`      bigint        DEFAULT NULL            COMMENT '创建人',
  `create_time`    datetime      NOT NULL                COMMENT '创建时间',
  `update_by`      bigint        DEFAULT NULL            COMMENT '更新人',
  `update_time`    datetime      NOT NULL                COMMENT '更新时间',
  `is_deleted`     tinyint       NOT NULL DEFAULT '0'    COMMENT '是否删除：0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ani_media_uk_1` (`opus_id`,`file_path_hash`),
  KEY `ani_media_idx_1` (`opus_id`,`is_deleted`,`is_primary`,`sort_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='媒体文件索引表';
```

一行对应磁盘上的一个媒体文件；文件夹形态的剧集（`第01话/xxx.mkv`）索引内部文件，`file_path` 取内部文件、集数从文件夹名解析。

- **唯一键用哈希列**：`file_path` 1024 字符在 utf8mb4 下占 4096 字节，超过 InnoDB 3072 字节的索引键上限，直接建唯一索引会报 1071
- **路径先归一化再哈希**：`toRealPath()` 解析 `..` 与符号链接 → 分隔符统一为 `/` → Windows 下转小写，然后才取 MD5。不做归一化时，大小写、`\`/`/`、相对路径写法不同都会让同一文件产生重复行，唯一键形同虚设
- **同集多文件只改标记，不删除**：命中多个文件（如 `1.mp4` 与 `第01话/a.mkv`）时，保留 `file_size` 最大（相同则 `file_mtime` 较新）的一行 `is_primary = 1`，其余置 `is_primary = 0`。**不动 `is_deleted`**——否则"去重软删"与"文件还在就复活"会每轮扫描互相抵消，`media_count` 与更新动态跟着抖动
- **软删与复活**：文件消失才置 `is_deleted = 1`；同路径再次出现时按唯一键原地复活（`is_deleted = 0` + 刷新 `last_seen_time`），不新增行；复活后重算 `is_primary`
- **软删前提**：只有该部作品本次扫描完整成功才允许软删；中途异常或 NAS 掉线保持原样
- **展示与计数口径**：对外的"集数列表""`media_count`"一律是 `is_deleted = 0 AND is_primary = 1`
- **`last_seen_time` 按部批量刷新**：每部扫描结束执行一次 `UPDATE ... WHERE opus_id = ? AND id IN (...)`，不逐文件写；本次扫描起点之后未被刷新的行才进入软删判定。10k 文件逐行 UPDATE 每 15 分钟一轮，比扫盘本身还贵

### 4.3 新表 `ani_user_opus_progress`（每集播放进度）

```sql
CREATE TABLE `ani_user_opus_progress` (
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
```

`episode_key` 由扫描器生成，规则见 §5.1：数字集数去零（`01` → `1`）、SP 归一为 `SP1`、无集数文件用 `X + 文件名MD5前8位`。

**重命名与进度**：有集数的文件改名（`3.mp4` → `03.mp4`、`SP01.mkv` → `SP 1 特典.mkv`）`episode_key` 不变，进度必然保留；无集数文件的 key 含文件名哈希，**改名后 key 一定变**，只能由扫描器「同体积配对」（§5.1）复用旧行把进度接回去。体积也变了（转码、重新压制、换片源）则视为新片，进度从头开始，这是本方案的已知边界。

- `is_completed`：收到 `ended`，或 `position_ms / duration_ms ≥ 0.95` 时置 1；重新从头上报不主动清零（手动改状态除外）
- `duration_ms`：只信任客户端上报且必须 ≥ 1000，写入取 `GREATEST(COALESCE(duration_ms, 0), :durationMs)`，避免播放器在 metadata 就绪前上报 0 把已有总时长覆盖掉。**不要写 `GREATEST(duration_ms, :durationMs)`**——MySQL 的 `GREATEST` 只要有一个参数是 NULL 就返回 NULL，而该列默认就是 NULL，首条写入会把总时长直接写丢
- 进度唯一键是 `(user_id, opus_id, episode_key)`，与 `ani_media.id` 无关，换路径/换片源都不丢

### 4.4 新表 `ani_opus_update`（更新动态）

```sql
CREATE TABLE `ani_opus_update` (
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
```

不存标题与展示名；唯一键保证重扫/重下不重复播报。查询时 `LEFT JOIN ani_media ON ani_media.id = ani_opus_update.media_id` 取 `display_name` 作为 `displayName`，**不要用 `episode_no` 拼**——SP 与无集数的 `episode_no` 为空，会拼出「第 null 集」；`media_id` 为空（媒体已软删）或 JOIN 未命中时，退回 `episode_key`（正片即集数，SP 是 `SP1`），仍为空才用 `"未知集数"`。

### 4.5 存量表变更

存量表不补齐审计字段，只做本模块必需的改动。

`ani_opus` 新增：

```sql
ALTER TABLE `ani_opus`
  ADD COLUMN `media_dir`        varchar(512)  DEFAULT NULL COMMENT '资源目录绝对路径',
  ADD COLUMN `media_count`      int           NOT NULL DEFAULT '0' COMMENT '已索引媒体数',
  ADD COLUMN `media_latest_key` varchar(64)   DEFAULT NULL COMMENT '最新一集的 episode_key',
  ADD COLUMN `media_scan_time`  datetime      DEFAULT NULL COMMENT '最近一次扫描成功时间';
```

`ani_user_opus` 变更：

```sql
-- 先按 (user_id, opus_id) 去重，合并规则见下，然后才建唯一索引
ALTER TABLE `ani_user_opus`
  ADD COLUMN `last_episode_key` varchar(64) DEFAULT NULL COMMENT '最近观看的 episode_key',
  ADD COLUMN `last_media_id`    bigint      DEFAULT NULL COMMENT '最近观看的媒体文件id',
  ADD COLUMN `last_read_time`   datetime    DEFAULT NULL COMMENT '最近观看时间',
  ADD UNIQUE KEY `ani_user_opus_uk_1` (`user_id`,`opus_id`),
  ADD KEY `ani_user_opus_idx_2` (`user_id`,`last_read_time`),
  DROP INDEX `ani_user_opus_idx_1`;
-- reading_num / reading_time / total_time / resource_url 停止写入，3.2 删除
```

- **去重合并规则**（物理删除，不可逆，必须写死）：`reading_time` 取最大的一条作为基础行；`is_share` 取 `MAX`（任意一条分享过就算分享）；`read_status` 按业务优先级取（已看 1 > 在看 2 > 未看 0，用 `CASE` 判定，**不要直接 `MAX`**，数值大小和优先级不一致）；`create_time` 取最早；`resource_url` 取非空的一条
- **`ani_user_opus_idx_2 (user_id, last_read_time)`**：「继续观看」按 `last_read_time` 倒序取 3 条，只有 `(user_id, opus_id)` 索引的话每个用户都要把全部追番行排序一遍

- `last_media_id` 由上报接口写入（请求体带 `mediaId`），供首页「继续观看」直接跳 `?ep=`；对应媒体被软删时接口返回 `mediaId = null`，前端退化为打开详情页
- 该表没有 `update_time`，排序与"最近观看"一律用 `last_read_time`
- **双写口径**（P2 起至 P5 停写）：`reading_num` = `episode_key` 的可显示形式，**只写数字集数**（`01` 写成 `1`），SP 与无集数一律写空串——旧首页 `AniOpusHomeVO.readingNum` 是 `Integer`（`AniOpusHomeVO.java:65`），写 `SP1` 会让列表映射失败；`reading_time` = `position_ms / 1000`，`total_time` = `duration_ms` 未知时写 `NULL`、否则 `/ 1000`。只写"最近一集"的进度，不聚合

`ani_opus` 新增的 `media_dir` 由扫描器在**首次按 `nameCn` 匹配成功后回写**，之后优先使用；管理员手工改目录后下一轮扫描以新值为准。`ani_opus.media_count` 只统计 `is_deleted = 0 AND is_primary = 1` 的行。

旧列停写时点：P2 起新接口只写新表，但旧列继续双写，直到 P5（首页改读新字段）上线后再停写（落地项见 P5 清单里的 `AnimeUserOpusView` 迁移与「停写旧列」两条）；否则中间版本首页的"已观看至第 N 集"会回退。`follow()` 里写死 `readingNum = "1"` 也要在 P2 一并去掉。

### 4.6 迁移执行器

现状：没有迁移机制，`docker/mysql/init/*.sql` 只在空库执行；`docker/mysql/init/ddl-update-sql.sql` 是历史补丁，老库需手工执行。

新增：

- `db/migration/V3_1_0__media_and_progress.sql`
- **执行时机**：必须在 HTTP 开始服务前完成。Solon 3.10.1 的实际启动顺序（已用 `javap` 核对 `SolonApp`）：`startDo()` 依次调用 `initAwait()`、`init(consumer)`、`cfg.revise()`、`run()`；`run()` 内部依次是插件 init、插件 start（HTTP 端口在此开始监听）、beanScan（`DbConfig.db1` 的 `@Bean` 此时才创建）、`AppLoadEndEvent`。由此：
  - 回调（`init(consumer)`）确实早于端口监听，位置是对的
  - 但回调里**拿不到 `DataSource` bean**：`app.context().getBean(DataSource.class)` 必然失败；换成 `@Configuration` + `@Inject DataSource` 又晚于端口监听
  - 结论：迁移不依赖容器 bean，直接用 `DriverManager` 按配置开一条物理连接（迁移本来就是纯 JDBC，不需要连接池）：

  ```java
  Solon.start(Launcher.class, args, app -> {
      String url = app.cfg().get("myapp.db1.jdbcUrl");
      String user = app.cfg().get("myapp.db1.username");
      String pwd = app.cfg().get("myapp.db1.password");
      new SchemaMigrator(url, user, pwd).migrate();   // 内部 DriverManager 开一条连接走完全程
  });
  ```

  注意数据源配置在 `myapp.db1.*`（不是 `solon.datasource`），由 `DbConfig.db1(@Inject("${myapp.db1}"))` 装配；`app.cfg()` 在回调阶段已加载完成。`AppLoadEndEvent` 不要用——它在端口监听之后才发布，存在"请求先于迁移"的窗口
- **多实例互斥**：执行前在同一**物理连接**（即上面那条迁移连接，全程不换）上取 MySQL 命名锁 `GET_LOCK('ea_schema_migrate', 60)`，取不到即报错退出；`finally` 中 `RELEASE_LOCK` 并 `close()`
- **执行顺序固定为**：① `CREATE TABLE IF NOT EXISTS sys_schema_version`（执行器自己建，不依赖脚本）→ ② 查版本记录，有记录则校验 `checksum` 后跳过 → ③ 无记录时做结构探测（见下表）→ ④ 探测未命中则按文件名顺序执行脚本并写记录
- 按文件名顺序执行未执行脚本，执行记录表沿用同一套约定：

```sql
CREATE TABLE `sys_schema_version` (
  `version`     varchar(64)  NOT NULL COMMENT '脚本版本号，如 3.1.0',
  `script_name` varchar(255) NOT NULL COMMENT '脚本文件名',
  `checksum`    varchar(64)  NOT NULL COMMENT '脚本内容 MD5',
  `apply_time`  datetime     NOT NULL COMMENT '执行时间',
  PRIMARY KEY (`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='数据库版本记录表';
```

- 版本表只负责"跳过已执行脚本"，脚本本身仍要能重跑：`CREATE TABLE IF NOT EXISTS`、`DROP TABLE IF EXISTS`、`ALTER` 前查 `information_schema.COLUMNS`（MySQL 8 的 `ADD COLUMN` 没有 `IF NOT EXISTS`）
- **索引同样要判存在**：`ADD KEY / ADD UNIQUE KEY / DROP INDEX` 在 MySQL 8 都没有 `IF [NOT] EXISTS`，重跑或手工打过补丁的库上会报 1061（重复键名）/ 1091（索引不存在）。执行器按 `information_schema.STATISTICS`（`table_schema`、`table_name`、`index_name`）判断后再拼 DDL，本次涉及的索引有：`ani_user_opus_uk_1`、`ani_user_opus_idx_1`（删）、`ani_user_opus_idx_2`、`ani_media_uk_1`、`ani_media_idx_1`、`ani_opus_update_uk_1`、`ani_opus_update_idx_1`、`ani_user_opus_progress_uk_1`、`ani_user_opus_progress_idx_1`
- `checksum` 与记录不一致 → 终止启动并提示，不静默跳过
- 单个脚本失败 → 启动失败并打印原因（不允许半升级状态）
- **"不允许半升级"只能靠幂等重跑**：MySQL 的 DDL 隐式提交、无法回滚，脚本中途失败会停在半升级状态。执行器不承诺原子性，只保证修好问题后重启能接着跑——每个变更前都查 `information_schema`，已生效的跳过

**与初始化脚本的关系**

| 场景 | 做法 |
|---|---|
| `doc/sql/ddl-sql.sql`、`docker/mysql/init/ddl-sql.sql` | 始终维护为"最新全量"，新表与新列在 P0 一并写入，两份内容除格式外保持一致 |
| 空库 | init 建到最新后，执行器写入 baseline（`version = 3.1.0`、`script_name = baseline`、`checksum = '-'`），不再执行 `V3_1_0` |
| 老库（无 `sys_schema_version`） | 结构探测未命中，执行 `V3_1_0`，成功后写版本记录 |
| 手工打过补丁的库（业务表已齐） | 走"结构探测"：只判 **3 张业务表** `ani_media`、`ani_user_opus_progress`、`ani_opus_update` 全部存在，且 `ani_user_opus.last_episode_key/last_media_id/last_read_time`、`ani_opus.media_dir/media_count/media_latest_key/media_scan_time` 全部存在 → 直接写 baseline，不跑脚本；**任一缺失都不算 baseline**，照常执行脚本（脚本自身幂等）。版本表不在探测条件内——它在第 ① 步已由执行器建好，永远存在，判它等于恒真 |
| `ddl-update-sql.sql` | 内容并入 `V3_1_0` 后清空（保留空文件，`001-init.sql` 仍会 `source` 它） |
| 基线之后的脚本（如 P3 的 `V3_1_1__drop_notify.sql`） | 正常按版本执行；空库上 DROP 不存在表即 no-op，同样写版本记录 |

**P0 必须先修复两份 init 脚本的既有漂移**：`docker/mysql/init/ddl-sql.sql` 目前**没有** `ani_user_opus.total_time`，也**没有** `ani_user_feed` 表，两者只存在于 `ddl-update-sql.sql`。如果直接按上表清空补丁文件，Docker 全新安装会缺列缺表（`ani_user_opus_listByUser` 立刻 SQL 报错）。因此 P0 的顺序是：先把这两块并进 docker 的 `ddl-sql.sql`，确认空库安装可用，再清空 `ddl-update-sql.sql`。两份脚本的排序、collation 写法以 `doc/sql/ddl-sql.sql` 为准。

### 4.7 数据回填

顺序很重要：**先扫描建索引，再回填进度**。回填依赖 `ani_media`，所以不能放进迁移脚本：进度回填由 P2 的启动后任务执行，更新动态回填（第 3 步）随 P3 一起跑。

1. 首次启动异步全量扫描 → 生成 `ani_media`、`ani_opus.media_*`
2. 进度回填：
   - `reading_num` 为数字（含 `01`、`1.5`）→ 规范化为 `episode_key`，`position_ms = reading_time * 1000`
   - `reading_num` 形如 `SP01`/`SP1`/`SP 1` → 统一为 `SP1` 形式作为 key
   - `reading_num` 为其他形式（`第5集`、空串等）→ 先尝试抽数字，抽不到就跳过并记日志
   - `reading_num` 为 `0` 或 `reading_time` 为 `0` → 跳过，不回填
   - `episode_key` 在 `ani_media` 中不存在 → 跳过并记日志（换片源后集数对不上）
   - `total_time` 为空 → `duration_ms` 留空
   - `position > duration` → 截断到 `duration - 5s`
   - `watched_ms` 按 `position_ms` 初始化，`is_completed` 仅在 `position/duration ≥ 0.95` 时置 1
   - **写入用 `INSERT ... ON DUPLICATE KEY UPDATE id = id`**：只补缺失的行，绝不覆盖改造后已经产生的新进度。`id` 无自增，必须显式生成后写入（同 §5.4）
3. 更新动态回填：每部番只回填**最新一集**，`create_time` 取文件 mtime，避免历史刷屏；写入沿用 §5.4 的 upsert（`id` / `create_time` / `update_time` 显式赋值），重复执行不会覆盖 `create_time`，也就不重复播报
4. **执行一次即止，但两步各写一条标记**：P2 的进度回填与 P3 的更新动态回填分属两次发布，共用一个标记会让后上线的一步被跳过
   - 进度回填（P2）：`version = 3.1.0-backfill-progress`、`script_name = progress-backfill`
   - 更新动态回填（P3）：`version = 3.1.0-backfill-update`、`script_name = update-backfill`
   `checksum` 列 NOT NULL，填 `'-'`；启动时见到对应记录就跳过该步。首次进度上报也会自动建档，回填只是把老数据搬过来

### 4.8 回滚

- 旧列全部保留，回滚旧镜像后进度仍可读（丢失改造期间的按集进度）
- 新表可整体 DROP，不影响旧代码
- **`ani_user_opus` 去重不可逆**：重复的追番记录在迁移时被物理删除，回滚不会恢复
- **P5 之后停写旧列**：此时回滚镜像，首页进度会冻结在停写那一刻，直到再次升级
- `sys_notify` 为不可恢复删除（决策已确认），回滚镜像后通知表不存在，旧代码 `save` 会报错 → **回滚步骤必须包含在升级说明中**
- `ani_user_opus` 新加的唯一索引会让旧代码的并发双击追番从"产生重复行"变成抛 DuplicateKey；旧代码是先查后插，正常路径不受影响，回滚说明里注明

---

## 5. 模块设计

### 5.1 媒体扫描器

**目录约定**

- 根：`elysiananime.file.animation-path`（支持逗号分隔多根）
- 作品目录：`{root}/{nameCn}/`
- 集文件：`{opus}/1.mp4` 或 `{opus}/第01话/xxx.mkv`（文件夹形态索引内部全部媒体文件，同集多文件由 `is_primary` 决定展示哪一个）

**集数解析（按序命中）**

1. **先判额外内容**：文件名或所在一级目录名含 `SP / OVA / 特别篇 / 特典 / PV` → 从中取独立数字 `n`，得 `episode_key = SP{n}`（`SP01`、`SP 1` 归一为 `SP1`）、`episode_no = NULL`、`sort_no = 10000 + n`。**这条必须排在所有数字规则之前**：`SP01.mkv` 里也有数字，让数字规则先命中就会当成正片第 1 集
2. 明确集数标记：`第12话 / 第12集 / 第十二话`
3. 方括号与简写：`[12]`、`[12.5]`、`【12】`、`E12`、`EP12`、`- 12`（方括号内允许小数）
4. 小数集数：`12.5.mkv` 这类以小数作集数的文件名，**必须排在"最后一个独立数字"之前**，否则按数字切分会先取到 `12`
5. 文件名中最后一个独立数字（排除 1080/2160/720/264/265/10bit/年份 等）
6. 都没命中，或命中了第 1 条但取不到编号 → `episode_key = 'X' + 文件名去扩展名后的 MD5 前 8 位`，`episode_no = NULL`，`sort_no = NULL`。**不能留空也不能兜底成 `SP1`**——`episode_key` 是 NOT NULL 且参与唯一键，同一部作品下的两个剧场版、或两个没编号的特典都会撞到同一个键，随后被 `is_primary` 去重吃掉一个

目录形态（`第01话/xxx.mkv`）优先用目录名解析，解析不到再退回文件名。

**字段生成规则**

| 字段 | 规则 |
|---|---|
| `episode_key` | 数字去零（`01` → `1`）；SP 归一为 `SP1`；无集数用 `X+hash8` |
| `display_name` | 有集数 → `第 5 集` / `第 12.5 集`；SP → `SP1`；无集数 → 文件名去扩展名 |
| `sort_no` | 正片 = `episode_no`；SP = `10000 + n`；无集数 = `NULL` |
| `media_latest_key` | 取**正片**（`episode_no IS NOT NULL`）里排序最后的一个；没有正片（只有 SP / 剧场版 / PV）时退化为全部媒体里排序最后一个。不能直接取"全部媒体排序最后一个"：SP 的 `sort_no` 是 `10000 + n`，永远压过第 12 集，会让「已看完」/ 续看判定（见 §6 `caughtUp`）永远不成立，并且每来一个 SP 就误报一次 NEW |
| 统一排序 | `ORDER BY sort_no IS NULL, sort_no, id`。MySQL 的 `ASC` 默认把 NULL 排最前，与"SP/无集数排在正片之后"相反，所有列表、上一集/下一集查询都必须显式写上这段 |
| 下一集 / 最后一集 | 只在**同一组内**找后继（`episode_no IS NOT NULL` 为正片组，其余为额外内容组）：组内没有后继就是该组的最后一集，正片播完**不**自动跳到 SP / 剧场版。**禁止**用 `MAX(episode_key)` 或字符串比大小（`"12" < "2"`）；也不要用全局排序找后继，否则第 12 集播完会「下一集」是 `sort_no = 10001` 的 SP1 |
| `hasUpdate` | 见 §6 的 `caughtUp`：**不要**只做 `media_latest_key` 与 `last_episode_key` 的等值比较——看完第 12 集后又看了 SP1，`last_episode_key` 变成 `SP1`，等值永不成立、NEW 永久挂着；也不做大小比较（`"12" < "2"`） |

**扫描流程**

```
scanAll()
 ├─ Redis 锁 media:scan:lock（RedisClient.getLock().tryLock()，TTL 30 分钟，finally unLock）
 │    └─ 现有 RedisServiceImpl 不暴露锁能力，需在扫描器里直接注入 RedisClient
 ├─ 遍历各 root 的一级子目录
 ├─ 匹配 opus：优先 media_dir，其次 nameCn 精确匹配；命中且 media_dir 为空 → 回写 media_dir
 │    └─ 未匹配 → 记入 scanResult.orphans，不写库、不建条目
 ├─ 逐部 scanOpus()（单线程、每部一个事务）
 │    ├─ 扫描起点记 scanStartAt，深度 ≤ 3（`{opus}/Season 1/第01话/a.mkv` 这类三层结构要能扫到），按 media-filetype 过滤
 │    ├─ 跳过 .part / .!qB / .tmp / 隐藏文件 / 60 秒内仍在变动 → 计入"本次已见"，不参与软删
 │    ├─ 路径归一化后取 MD5，与已有行（opus_id + file_path_hash）比对：命中 → 更新，未命中 → 进入"待新增"，本次未命中的行 → 进入"待消失"
 │    ├─ **先配对重命名，再插入**：待消失行 ↔ 待新增文件按 `episode_key + size` 配对，命中者**复用旧行**只改 `file_path / file_path_hash / file_name / relative_path / file_size / file_mtime`，并从"待新增""待消失"双双移除。`episode_key + size` 未命中、且两侧 key 都以 `X` 开头时，再按 `file_size` 兜底配对（同一部作品下同体积的无集数文件视为改名后的同一个）；**同体积候选多于一个时放弃配对**，退回新增 + 软删，避免把两部剧场版错配
 │    │    └─ 顺序不能反：先 insert 新行的话，新路径的 `file_path_hash` 已被占用，旧行再改过去就是 DuplicateKey，整部事务回滚
 │    ├─ 剩余"待新增" → insert ani_media；若同 hash 命中的是已软删行 → 原地复活（is_deleted = 0），不新增行
 │    ├─ 同集多文件 → 保留体积最大（相同取 mtime 新）的一行 is_primary = 1，其余 is_primary = 0
 │    ├─ 批量刷新本次已见行的 last_seen_time（一条 UPDATE ... WHERE id IN (...)）
 │    ├─ 本次扫描完整成功才软删（is_deleted = 1）：last_seen_time < scanStartAt 且文件确已不存在的"待消失"行
 │    ├─ 新增 / 复活的媒体 → upsert `ani_opus_update`（`INSERT ... ON DUPLICATE KEY UPDATE media_id = VALUES(media_id), update_time = VALUES(update_time)`，`source` 取触发方传入的 hint，缺省 1；`id` / `create_time` / `update_time` 手写赋值）；重命名不产生更新动态
 │    └─ 更新 ani_opus.media_count / media_latest_key（按上面的正片优先规则取最后一个）/ has_resource / media_scan_time
 └─ 汇总：新增/更新/删除/跳过/孤儿/耗时
```

**触发方式**

| 触发 | 说明 |
|---|---|
| 定时 | 自管 `ScheduledExecutorService`，每次调度前读取 `elysiananime.media.scan.interval`（默认 15 分钟，支持 `10m`/`1h`）。`@Scheduled` 只有编译期常量，`solon-scheduling-simple` 不解析 `${}`、也没有 `fixedDelayStr`，不能直接表达可配置间隔 |
| 启动 | 应用启动完成后异步执行（迁移除外，见 §4.6），不阻塞启动 |
| 事件 | 上传完成、RSS 下载/重命名完成 → `scanOpusAsync(opusId, source)`，30 秒内按 opusId 合并，`source` 透传给 `ani_opus_update` |
| 手动 | 管理页「重新扫描」 |

- **锁粒度**：只有 `scanAll` 持锁；全量扫描进行中收到的单部触发只进待扫队列（`Set<opusId>`），扫描结束后补跑，不抢锁也不丢任务
- **权限**：`POST /anime/media/scan`、`POST /anime/media/scan/{opusId}` 加 `@SaCheckRole(value = {"role:super:admin","role:simple:admin"}, mode = SaMode.OR)`，与现有管理端点一致；`GET /anime/media/scan/status` 登录即可
- **替换旧的发现任务**：`OpusJobs.autoDiscoverResource()`（每日一次）与新扫描器职责重复，且仍会扫盘、改 `has_resource`、发通知。P1 中改为触发 `scanAll()`（或直接删除该任务与 `AniOpusService.autoDiscoverResource`），避免两套扫盘逻辑并存

**降级**

- 目录不可读 / NAS 掉线 → 本次扫描失败，**保留旧索引**，不做删除
- 多根重复 → 以已记录 `media_dir` 为准，并在扫描结果里记为冲突项（通知系统已删，只落日志与状态对象，不发消息）
- 扫描状态对象 `{running, startedAt, total, done, failed, orphans, lastResult}` 供前端轮询；最近一次结果同时写入 Redis（沿用 `RedisKeyConst`），重启或多实例时 `status` 仍能返回上一次结果，运行中状态以当前实例为准

### 5.2 播放与进度

**上报接口**

```
POST /anime/userOpus/progress
{ opusId, mediaId, episodeKey, positionMs, durationMs, watchedDeltaMs,
  event: tick|pause|seek|ended|close, sessionId, seq }
→ { readStatus, isCompleted, latestEpisodeKey, nextEpisodeKey, nextMediaId, positionMs }
```

**上报节拍**：播放中每 10 秒一次 `tick`（对应验收 #16 ≤ 8 次/分钟）；`pause / seek / ended / close / 切集` 各补一次，不额外起短周期定时器。页面卸载（`pagehide` / 切后台）用 `navigator.sendBeacon` 发最后一次 `close`，不依赖异步请求来得及完成。

**规则**

| 规则 | 说明 |
|---|---|
| 用户身份 | `userId` 取自 `LoginUtils.loginId()`，请求体不接收 |
| 媒体校验 | `mediaId` 必须存在、`is_deleted = 0`、属于该 `opusId`，且其 `episode_key` 与请求一致，否则拒绝；不信任前端传入的集数 |
| 自动建档 | 首次上报时创建 `ani_user_opus`（修未追番无法保存），`read_status` 按首次状态机结果写。该表 `create_time` 是 NOT NULL 且无默认值，插入时必须显式给 `create_time = now()`（没有 `update_time` 列）。走 `lightDao.save()` 由雪花生成 `id`；若为了 `INSERT IGNORE` 改成手写 SQL，`id` 要自己生成后写入 |
| 写入 | 手写 `INSERT ... ON DUPLICATE KEY UPDATE`（顺带置 `is_deleted = 0`）；**三张新表的 `id` 都是无自增的 `bigint`，雪花只在 sqltoy `save()` 路径生效，手写 SQL 必须自己生成 `id` 再写**；同样不经过 `SqlToyUnifyFieldsHandler`，`create_time`/`update_time` 也要显式赋值；lambda 没有 upsert，这项不进 §12 的 Lambda 化 |
| 附带更新 | 同一次请求内更新 `ani_user_opus` 的 `last_episode_key` / `last_media_id` / `last_read_time = now()`，三者只在「该集上报位置大于零或事件为 ended」时更新 |
| 乱序丢弃 | `session_id` 与已存值不同 → 无条件接受并把 `seq` 重置为新值；`session_id` 相同才比较 `seq`，小于已存值则丢弃 |
| 累计观看 | `watchedDeltaMs` 服务端夹到 0–15s，累加进 `watched_ms` |
| 总时长 | `durationMs` 仅在 ≥ 1000 时接受，写 `GREATEST(COALESCE(duration_ms, 0), :durationMs)`；不覆盖为 0 或更小的异常值。别省略 `COALESCE`：`GREATEST(NULL, 12345)` 在 MySQL 里返回 NULL，列默认又是 NULL，首条写入必然写丢 |
| 状态机 | `watched_ms ≥ auto-reading-time × 1000`（配置单位是秒）且 `read_status = 0` → 变 2（在看）；`ended` 时按 §5.1 在**同组内**取下一集，有下一集则只返回 `nextEpisodeKey`；组内没有后继时：正片组 → 置 `read_status = 1`（已看）；额外内容组 → 只有整部作品没有正片（此时 `media_latest_key` 就取自额外内容）才置 1。理由：`last_episode_key` 若停在一个永远不等于 `media_latest_key` 的 SP 上，作品会被反复塞回「继续观看」。**`position/duration ≥ 0.95` 与 `ended` 同等对待**（拖到片尾再直接关页面时浏览器不一定派发 `ended`）：该集标 `is_completed = 1`，并沿用上面同一套「末集」判定置 `read_status = 1`；漏掉这一步会出现「卡片打 ✓、作品还挂在继续观看」 |
| 续看口径 | 「继续观看」排除的是 `read_status = 1` **且** `caughtUp`（§6）的作品。这样新集入库后 `media_latest_key` 变化、`caughtUp` 不再成立，已看完的作品自动回到继续观看，不需要把状态机做成可回退；也不会因为"看完后又看了 SP"把 `last_episode_key` 改写成 `SP1` 就误判 |
| 手动改状态 | 直接写 `ani_user_opus.read_status`，并清零**该作品所有集**的 `watched_ms`；改为「已看完」时**必须同时把 `last_episode_key` 写为当前 `ani_opus.media_latest_key`、`last_media_id` 写为该 key 下 `is_deleted = 0 AND is_primary = 1` 的行（没有就置 NULL）**，否则 `caughtUp`（§6）不成立，作品会立刻弹回「继续观看」。改为「未看/在看」只清 `watched_ms`，不动 `last_episode_key`。用户继续播放时按新累计重新判定 |
| 手动改状态的入口 | 现有 `POST /anime/userOpus/update`（`AniUserOpusUpdateDTO.readStatus`）就是这条路径，P2 起必须走上面的规则，不能只改 `read_status` 一列 |
| 并发 | 状态更新使用条件 SQL（`where read_status = 0`），不读后写 |
| 越权 | 报文里的 `userOpusId`、`userId` 一律忽略；只按登录态写入 |

### 5.3 弹窗播放

**路由**（沿用现有 `/admin` 前缀与 `AdminLayout`，不重构外层框架）

```
/admin/ani-home                   首页
/admin/ani-opus/:id               详情页
/admin/ani-opus/:id?ep=<mediaId>  详情页 + 自动打开弹窗（可带 &t=<秒> 作为初始位置）
旧 /admin/ani-video/:id/:num/:time → /admin/ani-opus/:id?ek=<episodeKey>&t=<秒>
```

- 点集数 `router.push({query:{ep}})`
- 关闭时历史里有 `ep` 项就 `router.back()`，刷新直达的用 `router.replace` 去掉 `ep`
- 刷新带 `ep` 自动打开并续播
- **旧链接重定向要尽力恢复播放**：把 `num` 按 `episode_key` 规则归一（`03` → `3`、`SP01` → `SP1`）后放进 `?ek=`，`time` 放进 `&t=`；重定向发生在拿到 `mediaList` 之前，此时还没有 `mediaId`，所以由详情页加载完成后用 `mediaList` 反查出 `mediaId` 再 `router.replace` 成 `?ep=<mediaId>&t=<time>`。未命中就停在详情页并提示「未找到该集」。直接跳到详情页会丢掉用户点进来的那一集
- **站内调用点必须同批修改**（仍在 `router.push({name:'AnimeVideo'})`，不改会运行时 No match）：`AdminHeader.vue`（通知跳转，随通知删除一并处理）、`AnimeShare.vue:92`、`AnimeFeedList.vue:128`、`AnimeHome.vue:289`、`AnimeVideo.vue:399`
- **keep-alive 要能按路由控制**：`AdminLayout` 现在是无条件 `<keep-alive>`（没有 `:include`），改成 `<keep-alive :include="keepAliveNames">`，`keepAliveNames` 由路由 `meta.keepAlive` 生成；同时每个路由组件显式 `defineOptions({ name })`（异步组件 + 生产构建下组件名不能只靠文件名推断）。详情页与播放层不进缓存
- 详情页与播放层上线后删除 `views/home/AnimeVideo.vue` 及其全局样式引用，不做双份并存
- **自动下一集**：`ended` 且存在下一集时弹 3 秒倒计时条（可取消、可立即播放），倒计时结束才 `player.src` 切源并 `router.replace` 更新 `ep`；最后一集提示「已看完本番」

视觉、交互与组件拆分见子计划 [playback-ui-redesign.md](./playback-ui-redesign.md)。

**组件**

```
views/anime/home/AnimeHome.vue     首页（追番台）
views/anime/detail/AnimeDetail.vue 详情页
  ├─ OpusHero.vue                  封面 hero + 继续观看 + 追番操作
  └─ EpisodeGrid.vue               集数网格（✓ / 进度条 / NEW）
components/player/
  ├─ PlaybackDialog.vue            弹窗壳：标题、选集条、快捷键
  └─ VideoStage.vue                xgplayer 封装
composables/
  ├─ useXgPlayer.ts                实例创建/销毁/切源
  └─ usePlaybackProgress.ts        上报与恢复
```

**交互约定**

| 项 | 策略 |
|---|---|
| 尺寸 | `min(1200px, 92vw)`，16:9 |
| 移动端 | 全屏铺满，横屏自动全屏 |
| 遮罩点击 | 不关闭（防误触） |
| 全屏 ESC | 先退全屏，不关闭弹窗 |
| 切集 | 不销毁实例，`player.src` 切换；切前先 flush 旧集，切后 `router.replace({query:{ep: 新mediaId}})` 同步 URL（用 `replace` 不 `push`，否则连切 5 次要点 5 次后退才关得掉） |
| 自动下一集 | `ended` → 3s 倒计时（可取消、可立即播放）→ 切源 + `router.replace` 更新 `ep`；最后一集不切 |
| 恢复 | 仅 `LOADED_METADATA`；`position / duration ≥ 0.95` 视为看完，从头播且不提示 |
| 打开与关闭 | 打开一律 `push({query:{ep}})`；关闭时历史里有 `ep` 项则 `router.back()`，刷新直达的则 `replace` 去掉 `ep` |
| 关闭 | flush → `destroy()` → 置空实例；`onDeactivated` 同样处理 |
| 错误 | 文件不存在/编码不支持在弹窗内展示并可触发重扫 |

### 5.4 通知删除与更新动态

**删除清单**

后端：

- `api/system/controller/SysNotifyController.java`
- `api/system/service/SysNotifyService.java` + `impl/SysNotifyServiceImpl.java`
- `api/system/model/po/SysNotify.java`、`dto/SysNotifyAddDTO.java`、`vo/SysNotifyVO.java`
- `jobs/SysNotifyJobs.java`
- `elysiananime-common` 模块的 `common/constant/NotifyConst.java`
- sqltoy 中 `sys_notify_*` SQL
- `MiKanRss.doNotify / doNotifyCatchEx`、`MailUtil/MailAccount` import、`EmailSenderProp` 注入
- `AniOpusServiceImpl.autoDiscoverResource / uploadRes` 中的通知
- `AniUserOpusService.findFollowsEmail` 及对应 SQL
- `properties/EmailSenderProp.java`
- `DefaultProp.mailNotifyFlag`（**`webUrl` 保留**，登录返回 `origin` 在用）
- `elysiananime-server/pom.xml` 的 `dependencyManagement` 中 `mail.version` 与 `javax.mail` 条目（没有模块直接声明该依赖，删管理条目即可；`hutool-extra` 保留）

前端：

- `AdminHeader.vue` 铃铛 popover、`notifyList`、`getNotifyList`、`onReadMore`
- `api/anime/ani-notify-api.ts`

配置：

- 三份 `app.yml` 中的 `mail-notify-flag` 与 `emailsender` 整块
  - `elysiananime-server/elysiananime-api/src/main/resources/app.yml`
  - `docker/app/conf/app.yml`
  - `release/config/app.yml`

数据库：

- `DROP TABLE IF EXISTS sys_notify`（同时从 `doc/sql/ddl-sql.sql` 与 `docker/mysql/init/ddl-sql.sql` 移除建表语句）；脚本是 P3 的 `V3_1_1__drop_notify.sql`，**不要并进 P0 的 `V3_1_0`**——P0 部署时 `SysNotifyJobs` 还在每 60 秒查这张表
- 修正死链菜单：`doc/sql/data-sql.sql` 与 `docker/mysql/init/data-sql.sql` 里那条 `番剧首页 → /#/anime/home`（id `1278669765661822976`）路由不存在，且带 `/#/` 前缀——菜单组件是 `router.push({path: item.routerPath})`（`ChildMenu.vue:47`），带 `#` 必然匹配不到，其它菜单行也都是 `/admin/...`。迁移脚本（P0 的 `V3_1_0`）里 `UPDATE sys_menu SET router_path = '/admin/ani-home' WHERE router_path = '/#/anime/home'`，两份种子数据把该行**原地改成** `/admin/ani-home`。**不要删行**：`sys_role_menu` 有 `menu_id = 1278669765661822976` 的绑定，删了全新安装会既没有首页菜单、又留下悬空角色菜单关联
- `OpusJobs.autoDiscoverResource` 与 `AniOpusService.autoDiscoverResource` 由扫描器取代（见 §5.1），不保留两套扫盘逻辑

**更新动态写入**

- 扫描器新增媒体行时 upsert `ani_opus_update`：`INSERT ... ON DUPLICATE KEY UPDATE media_id = VALUES(media_id), update_time = VALUES(update_time)`。**不能用 `insert ignore`**——同集换了主文件时 `media_id` 必须跟着改（见本节最后一条），`IGNORE` 不会更新已有行
- 手写 SQL 不经过 `SqlToyUnifyFieldsHandler`，三张新表的 `id` 都是无自增的 `bigint`、雪花只在 sqltoy `save()` 里生成，所以 `id` 与 `create_time` / `update_time` 都必须显式赋值（`id` 用同一个 `SnowflakeIdGenerator` 实例生成）
- `create_time` 取文件 `file_mtime`：首次扫描把历史文件一次性入库时，用当前时间会让「最近更新」被历史刷屏；新下载文件的 mtime 本来就接近当下
- `source` 由触发方传入：扫描 1 / RSS 2 / 手动上传 3；纯定时扫描一律为 1
- 唯一键 `(opus_id, episode_key)` 去重，重扫与重下不重复播报；`media_id` 由上面的 upsert 在去重时更新为当前主文件

**展示**

- 首页分区：继续观看（1 大 + 2 小）、最近更新（横滑，最近 12 条）、我的追番（网格），布局以子计划 §5 为准
- 仪表盘特指 `views/system/dashboard/Dashboard.vue`（路由 `/admin/dashboard`，统计与 RSS 情况在这里）；`/admin/home` 是 `Home.vue` 的 README iframe，不要改错文件。新增「更新动态」区块 + 「扫描状态」区块，保留现有统计与 RSS 情况
- 未读提示：`localStorage` 记录 `lastSeenUpdateId`，不新增服务端状态

**接口**

```
GET /anime/update/latest?limit=12
GET /anime/update/page?pageNo&pageSize&opusId&start&end
→ { id, opusId, opusName, coverUrl, episodeKey, episodeNo, isExtra, displayName, mediaId, source, createTime }
```

- 两个接口都要求登录（走全局 Sa-Token 拦截器，不需要额外注解），更新动态是全站共享数据
- `latest` 一次返回近 12 条，按 `create_time desc`；`page` 按 `opusId` 过滤走唯一键 `(opus_id, episode_key)` 的前缀，不需要额外索引
- `displayName` 与 `isExtra` 都在查询里现算：展示名 JOIN `ani_media` 取 `display_name`（缺失退回 `episode_key`，见 §4.4），`isExtra` 取 `episode_no IS NULL`，不要为它们再加列
- 表只按"每部番每集一条"增长，不做清理任务；分页接口承担历史翻阅

### 5.5 首页与仪表盘

首页、详情页、播放层的视觉与组件设计见子计划 [playback-ui-redesign.md](./playback-ui-redesign.md)，本节只保留与主计划相关的约束：

- 首页与详情页的数据全部来自 §6 的接口，卡片进度用服务端数字，杜绝 NaN
- 首页「继续观看」用 `GET /anime/userOpus/continue`：按 `last_read_time` 倒序，排除「`read_status = 1` 且 `caughtUp`」（定义见 §6，不要自己写等值比较），跨作品取 3 条；返回 `opusId / nameCn / coverUrl / episodeKey / displayName / mediaId / positionMs / durationMs`，`durationMs` 出参为 `0` 表示未知时长（§6 序列化约定），按未知降级、不显示百分比
- 「我的追番」用 `POST /anime/opus/listByUser`，但**必须只返回已追番的作品**：该 SQL 是 `ani_opus left join ani_user_opus`，不筛会把全库作品都列出来，空态也永远不触发。接口加请求参数 `isFollow`，但**库里没有 `is_follow` 列**——「追番」的语义就是 `ani_user_opus` 有没有对应行，所以过滤条件是 `auo.id is not null`（`isFollow = 0` 则用 `auo.id is null`），不要去找一个不存在的列
- 同一 SQL 的默认排序从现在的 `str_to_date(ao.launch_start, '%Y年%m月%d日') desc, auo.create_time desc` 改为 `ORDER BY auo.last_read_time DESC, ao.id DESC`：MySQL 的 `DESC` 本身把 NULL 排在最后，不要额外套 `COALESCE` 或 `IS NULL`；追番过滤后 `ani_user_opus` 必然有行，`last_read_time` 只在从未播放时为空。若以后要改成升序，必须显式写 `ORDER BY auo.last_read_time IS NULL, auo.last_read_time`
- 卡片「已看 5/12」「NEW」分别来自 `completedCount` / `mediaCount` 与 `hasUpdate`，✓ 角标取 `isCompleted`，字段定义见 §6
- **「我的追番」管理页不能只做视觉对齐**：`views/system/manager/anime/AnimeUserOpusView.vue` 走 `POST /anime/userOpus/listByUser`（返回 `AniUserOpusVO`），列表现有列是 `readingNum` / `readingTime` / `resourceUrl`，编辑弹窗还能改 `readingNum` / `readingTime` / `readStatus`。P5 停写旧列后这一页会冻结在停写那一刻，编辑也写不进任何新逻辑。P5 必须同批把它迁到新字段（`display_name`、`positionMs`、`durationMs`、`readStatus`），并去掉 `resourceUrl` 列与输入项；这也是"停写旧列"的前置条件
- 封面接口已支持 `?w=&h=`（`/anime/opus/cover`），本轮不新增图片接口
- 首页拆组件：`AnimeCard`、`ContinueWatching`、`RecentUpdateStrip`、`AnimeFilterBar`、`FilterDrawer`、`AddOpusDialog`、`FeedDialog`（与子计划 §8 同名）
- 封面：`<img loading="lazy" decoding="async">` + 后端 `cover?w=&h=` + `aspect-ratio: 3/4` + 骨架屏，首屏无布局抖动
- 仪表盘改「运行监控」：统计卡片 / 扫描状态 / RSS 情况 / 更新动态表格

### 5.6 同批修复的安全项

| 问题 | 处理 |
|---|---|
| 未授权任意文件读取（cover/background/avatar） | `Path.normalize()` + `startsWith(基准目录)`，拒绝分隔符 |
| 媒体路径穿越 | 改 `/anime/media/{mediaId}/stream`，不再接受文件名。前端地址**不要在新组件里再写一份 `/api`**：现在 `axios-util.ts:19/69` 把 `/api/${url}` 写死，并不存在可复用的基址常量，P1 把它提成导出常量 `API_BASE`（默认 `/api`，可用 `VITE_API_BASE` 覆盖），axios 与 `<video src>` 共用。dev / docker 的 `server.contextPath` 是 `/api`；`release/config/app.yml` 是 `/`，线上靠前置代理把 `/api` 转发（现状即如此，本轮不改变这个约定） |
| 进度 IDOR | 所有写入强制 `user_id = 登录用户` |
| 通知越权 | 随通知系统删除 |
| 大视频拖动 | 流式接口支持 `Range` / `206` / `Content-Range`，并补齐 `Accept-Ranges`、`HEAD`、非法 Range 返回 `416`（带 `Content-Range: bytes */total`）、`If-Range` 可忽略但不得报错；文件名用 `Content-Disposition` 的 RFC 5987 编码，避免中文名乱码 |
| 媒体流鉴权 | `<video src>` 发不出自定义 header，流接口一律靠 Sa-Token cookie（`token-name = Authorization`，`is-read-cookie` 默认 true）。**现状不是"没有 cookie"，而是"有 cookie 但用不了"**：sa-token 1.44.0 的 `StpLogic.setTokenValue` 在 `isReadCookie` 为 true 时会写 `Set-Cookie`，但 `SaCookieConfig.path` 默认是 null（`httpOnly=false`、`secure=false`、`sameSite=null`），响应头里没有 `Path=`，浏览器按默认路径把作用域算成登录接口所在的 `/api/system`，`<video src="/api/anime/media/...">` 带不上；axios 那边发的又是 `sa-token` 头（`token-name` 是 `Authorization`）——两条凭证通路都不通。修法放配置层、不写 Java 代码：`app.yml` 的 `sa-token.cookie` 下补 `path: /`、`http-only: true`、`same-site: Lax`（1.44.0 的 `SaCookie.setSameSite` 仍收 `String`，没有 `SaSameSite` 枚举），登出 `StpUtil.logout()` 会按同一份配置清 cookie；`secure` 保持 false，等前置 HTTPS 代理稳定后再开。三个 profile（`elysiananime-api/src/main/resources/app.yml`、`docker/app/conf/app.yml`、`release/config/app.yml`）一起改，改完打印 `SaManager.getConfig().getCookie().getPath()` 确认绑定生效。嵌套绑定由 `solon-config-snack4` 的 `PropsConverterExt` 完成（`BOOT-INF/lib` 里有），kebab / camel 两种写法都能绑；但用最小 classpath 写单测验证时会看到"绑不上"，那是缺这个扩展包，不是配置错 | 
| 前端请求头 | `sa-token.token-name` 是 `Authorization`，而 `axios-util.ts:42/81` 现在发的是 `sa-token` 头，两处名称不一致。P1 把前端请求头统一成 `Authorization`，与 cookie 同名，不要出现"header 一个名、cookie 另一个名"。**改之前先确认旧头在当前环境能否登录**：若线上一直可用，说明线上 `app.yml` 与仓库不一致，先对齐配置再改前端。`sra-admin.excludes` 已放行 `/anime/opus/cover` 等静态资源；`/anime/media/**` 也要加进去，但目的是让 401/416 走原生状态码，鉴权必须在 handler 里自己做（见下一行），**不是**把它当公开资源 |
| 流接口的状态码必须自己写 | `AppFilter` 把所有异常统一渲染成 **HTTP 200 + `ApiResult` JSON**（`AppFilter.java:63`，未登录业务码 `4001` 在 body 里）。所以流接口不能靠拦截器抛 `NotLoginException`：`<video>` 会收到 200 和一段 JSON，表现成"解码失败"而不是"登录失效"，非法 `Range` 的 `416` 同样会被吞成 200。做法：`/anime/media/**` 从 `SaTokenInterceptor` 排除（走 `sra-admin.excludes`），handler 第一行 `StpUtil.checkLogin()`，不通过 `ctx.status(401)` 直接结束；非法 Range 写 `416`；读取失败写 `500`——全部直接操作 `ctx`，不抛异常。这是唯一一处"放行了但必须自校验"的路径，代码里要写注释说明 |
| 流接口归属 | 沿用现状：只校验登录，不按追番限制播放；如需收紧在此处补归属校验 |
| `follow` 语义 | 唯一索引保证并发只落一条；**最终形态**是 `follow` = "确认为已追" + `POST /anime/userOpus/unfollow` = 取消，不要让用户无法取消追番。但 P2–P4 过渡期旧页面仍在用，`follow` 暂时保持"再点一次取消"的旧行为，P4 新前端上线后再切（见 §6 过渡期表） |
| 扫描接口 | `POST /anime/media/scan`、`POST /anime/media/scan/{opusId}` 加 `@SaCheckRole(value = {"role:super:admin","role:simple:admin"}, mode = SaMode.OR)`；`GET /anime/media/scan/status` 登录即可 |
| 路径归一化 | 扫描入库前 `toRealPath` + 统一分隔符 + Windows 小写再取哈希，防止同一文件因写法不同产生重复行绕过唯一键 |

---

## 6. 接口变更总表

| 旧 | 新 | 说明 |
|---|---|---|
| `POST /anime/userOpus/updateProgress`（用 `userOpusId`） | `POST /anime/userOpus/progress` | 不再接收 `userOpusId`；增加 `mediaId` / `episodeKey` / `watchedDeltaMs` / `sessionId` / `seq`；返回状态机结果（见 §5.2） |
| `GET /anime/opus/media/{opusId}?resName=` | `GET /anime/media/{mediaId}/stream` | 支持 Range/206/HEAD/416；按 mediaId 取路径；靠 cookie 鉴权。前端地址 = `API_BASE` + 该路径（见 §5.6，基址常量在 P1 建立；dev / docker 是 `/api`，release 是 `/` 靠前置代理），**不要在组件里写死 `/api`** |
| `POST /anime/opus/listByUser` | 路径与方法不变，去掉扫盘 | 新增请求参数 `isFollow`（实现为 `auo.id is not null`，见 §5.5）；新增返回字段 `completedCount`、`mediaCount`、`lastEpisodeKey`、`lastMediaId`、`positionMs`、`durationMs`、`hasUpdate`、`isCompleted`（作品级是否看完，卡片 ✓ 用，定义见字段表）；`readStatus` 沿用已有列 |
| `GET /anime/opus/getOpusMedia/{id}` | 路径不变，**返回结构变更** | `mediaList` 增加 `mediaId`、`episodeKey`、`episodeNo`、`isExtra`、`sortNo`、`displayName`；新增 `progressMap`（key = `episodeKey`，value = `{positionMs, durationMs, isCompleted}`）；**旧字段必须保留到 P4**：`episodes`（取 `file_name` 去扩展名）、`mediaType`（取 `extension`）、`isFolder`（恒为 0）、`readStatus`、`isFollow`、`isShare`、`readingNum`、`readingTime`、`userOpusId`。`userOpusId` 在旧页面 `AnimeVideo.vue:283/288/408/439` 仍在用，P1 删掉会让 P1–P4 的旧页面进度与状态静默写不进去，**P4 删页面时一起删** |
| — | `GET /anime/userOpus/continue` | 继续观看，返回结构见下 |
| — | `POST /anime/userOpus/unfollow` | 取消追番（`follow` 改为幂等"确认已追"） |
| `GET /anime/opus/cover` | 不变 | 已支持 `w/h`，本轮不动 |
| `GET /anime/notify/*` | 删除 | 由 `/anime/update/*` 取代 |
| — | `POST /anime/media/scan`、`GET /anime/media/scan/status`、`POST /anime/media/scan/{opusId}` | 扫描；两个 POST 仅管理员 |
| — | `GET /anime/update/latest`、`GET /anime/update/page` | 更新动态，登录可见 |

**过渡期（P2 → P4）兼容**：后端接口先于新前端上线，旧页面在这段时间仍会被访问，不能直接删旧接口：

| 旧接口 | 过渡做法 | 何时删 |
|---|---|---|
| `POST /anime/userOpus/updateProgress` | 保留，内部转调新服务（写新表 + 双写旧列），`userOpusId` 仍可用。转换链：`userOpusId → (user_id, opus_id)` → `readingNum` 按 §4.7 规则归一成 `episode_key` → 查 `ani_media`（`is_deleted = 0`，优先 `is_primary = 1`）拿 `mediaId`。查不到 media（首次扫描未跑完、文件已换）时**只写旧列、不报错**，记一条 debug 日志 | P4 新前端上线后删 |
| `GET /anime/opus/media/{opusId}?resName=` | 保留，内部**按 `opus_id + file_name = resName` 查 `ani_media`**（`resName` 是「文件名.扩展名」，不是 `episode_key`；`AnimeVideo.vue:412` 拼的就是 `item.episodes + '.' + item.mediaType`），命中多行时 `ORDER BY is_primary DESC, id` 取第一条，再复用新流式响应（含 Range），不再拼路径 | 同上 |
| `POST /anime/opus/listByUser` 的 `isFollow` | 默认不传 = 不过滤，保持旧行为 | 永不删，作为可选过滤 |
| `POST /anime/userOpus/follow` | P2–P4 期间仍保持"再点一次取消"的旧行为；新前端改用 `follow` + `unfollow` 后再改为幂等确认 | P4 |

**过渡期实现陷阱**：旧 `getOpusMedia` 用 `Double.parseDouble(i.getEpisodes())` 给 `mediaList` 排序（`AniOpusServiceImpl.java:226-229`）。P1 把数据源换成 `ani_media` 后，无集数文件的 `episodes` 是 `movie` 这种非数字串，会直接抛 `NumberFormatException`、整页 500。P1 必须把它改成按 `sortNo` 排序（`sort_no IS NULL` 沉底，见 §5.1），并保持「数字集数在前、其他在后」的旧观感。

**序列化约定（先看这一条，再读下面的字段定义）**

`WebMvcConfig.jsonInit` 给 fastjson2 开了 `WriteNullNumberAsZero`、`WriteNullStringAsEmpty`、`BrowserCompatible`，所以**接口里根本没有 `null` 数字，也没有 `null` 字符串**：

- 数字为 `null` → 前端收到 `0`；字符串为 `null` → 收到 `""`；超出 JS 安全整数的 `long` / `BigInteger`（所有雪花 id）→ 收到**字符串**
- 因此「为空」一律按 `0` / `""` / 假值判断，不要写 `=== null`：`durationMs <= 0` 表示未知时长，`lastMediaId <= 0` 表示没有可跳转的媒体，`displayName` 空串时退回 `episodeKey`
- `sortNo` 为 `null` 时同样出参 `0`：排序一律由服务端 SQL 完成（`ORDER BY sort_no IS NULL, sort_no, id`），前端按接口顺序渲染即可，**不要按 `sortNo` 重排**，否则无集数的剧场版/PV 会被排到最前
- 不能靠 `episodeNo` 判「是不是正片」——SP 与无集数文件的 `episodeNo` 都会变成 `0`。正片/额外内容由 `isExtra` 显式区分（见字段定义），前端不要自己推
- 如果新增字段确实需要 `null`（例如 `episodeNo`），只能在该字段上单独关掉这几个 Feature，不允许为此改全局序列化配置——会波及全站接口

**字段定义**（首页/详情页/继续观看共用，避免各页面各算一套）

| 字段 | 定义 |
|---|---|
| `completedCount` | 该用户在该作品下 `is_completed = 1 AND is_deleted = 0` **且 `episode_key` 仍在当前剧集集合内**（`ani_media` 中 `is_deleted = 0 AND is_primary = 1` 的 key）的行数。必须与 `mediaCount` 同口径取交集——否则文件被软删/换片源后进度行还在，卡片会出现「已看 13/12」 |
| `mediaCount` | 取自 `ani_opus.media_count`（只统计 `is_deleted = 0 AND is_primary = 1`），卡片「已看 5/12」的分母；无资源为 0 |
| `lastEpisodeKey` / `lastMediaId` | 取自 `ani_user_opus`，未追番或未播放时为 `null`；**出参时 `lastMediaId` 为 `null` 会被序列化成 `0`**，前端按 `> 0` 判断 |
| `positionMs` / `durationMs` | 该用户"最近观看的那一集"（`lastEpisodeKey`）的进度；`durationMs` 未知时为 `null` → 出参 `0`，前端按 `<= 0` 判未知 |
| `caughtUp`（内部判定，不出参） | 用户是否已看完当前的**最新正片集**：`ani_opus.media_latest_key IS NOT NULL AND (ani_user_opus.last_episode_key = media_latest_key OR EXISTS (进度行：`user_id` = 当前用户、`opus_id` = 该作品、`episode_key = ani_opus.media_latest_key`、`is_completed = 1`、`is_deleted = 0`))`，走 `(user_id, opus_id, episode_key)` 唯一键，成本可忽略。**`hasUpdate` 与「继续观看」的排除都用它**：`last_episode_key` 会被"看完正片后又看 SP"改写成 `SP1`，只比等值会让 NEW 常挂、作品反复回到继续观看 |
| `hasUpdate` | `NOT caughtUp` 且 `ani_user_opus.last_episode_key IS NOT NULL`；未开始看的作品不算更新，否则刚追番的每部都会挂着 NEW，角标失去信息量 |
| `isCompleted`（作品级） | `readStatus != 0 AND (caughtUp OR (mediaCount > 0 AND completedCount >= mediaCount))`；卡片 ✓ 角标用，含义是"这部现在看完了"。`readStatus = 0`（用户手动标了「未看」）一律不出 ✓，否则「标记未看」等于没生效；`caughtUp` 兜住"手动标已看完/播完最后一集"，`completedCount >= mediaCount` 兜住"删掉最后一集文件后变成 11/11"与"乱序补完所有集"。新集上线后 `caughtUp` 不成立、`completedCount < mediaCount`，✓ 自动消失，与 NEW 不打架。**不要写成"`lastEpisodeKey` 那一集 `is_completed = 1` 就算作品看完"**：看到第 5 集、第 5 集播完、后面还有 7 集时会凭空打 ✓ |
| `displayName` | 该集展示名，取自 `ani_media.display_name`（`第 5 集` / `SP1` / 文件名去扩展名）；前端不拼集数，避免 `第 null 集` |
| `isExtra`（集级） | `episode_no IS NULL` 的媒体为 `1`（SP / OVA / 剧场版 / PV / 无集数），否则 `0`。**前端分组只能看这个字段**——`episodeNo` 的 `null` 在出参时会变成 `0` |
| `isCompleted`（progress 表） | 该集是否看完，用于集数网格的 ✓ |
| `readStatus`（作品级） | 0 未看 / 1 已看 / 2 在看；"已看"只表示看完了当时的最新一集 |

`GET /anime/userOpus/continue` 返回（最多 3 条，按 `last_read_time desc`，过滤规则见 §5.2）：

```
{ opusId, nameCn, coverUrl, episodeKey, displayName, mediaId, positionMs, durationMs, lastReadTime }
```

`listByUser` 实现保持 XML（JOIN 查询），只把扫盘逻辑替换成对 `ani_media` 的聚合；`completedCount`、`positionMs`、`durationMs` 用按 `(user_id, opus_id)` 关联的子查询或两次 LEFT JOIN 实现，200 条列表下需 `EXPLAIN` 验证走唯一键。

---

## 7. 实施阶段

> 每个阶段独立可验证，完成后提交一次，提交信息引用阶段号（如 `feat(P1): media scanner`）。

### P0 数据层（1 天）

- [x] 迁移执行器（`sys_schema_version`；用 `Solon.start(Launcher.class, args, app -> migrate(...))` 在 HTTP 启动前执行，失败终止启动；**连接用 `DriverManager` + `myapp.db1.*`，不要注入 `DataSource`——回调阶段 bean 还没创建**，见 §4.6；`GET_LOCK` 与后续 DDL 共用这一条连接，`finally` 释放）
- [x] `V3_1_0__media_and_progress.sql`：三张新表（含 `ani_media.is_primary`）、`ani_opus` 加列、`ani_user_opus` 去重 + 唯一索引 + `last_episode_key/last_media_id/last_read_time` + `(user_id, last_read_time)` 索引、把 `sys_menu` 里 `/#/anime/home` 改到 `/admin/ani-home`（原地 `UPDATE`，两份种子数据同批改）
- [x] **`sys_notify` 的 DROP 不在 P0**：`SysNotifyJobs` 每 60 秒仍在跑（`@Scheduled(initialDelay=1, fixedDelay=60000)`），P0 删表会让 P0–P3 之间每 60 秒抛一次 SQL 异常、通知接口 500。删表脚本 `V3_1_1__drop_notify.sql` 放到 P3，与删代码同一次发布
- [x] 先修 init 漂移：把 `total_time`、`ani_user_feed` 并进 `docker/mysql/init/ddl-sql.sql`，验证 Docker 空库安装可用，再清空 `ddl-update-sql.sql`
- [x] `doc/sql/ddl-sql.sql` 与 `docker/mysql/init/ddl-sql.sql` 同步到最新全量；空库与"手工打过补丁的库"都写 baseline
- [x] 回滚说明写入 `doc/sql` 升级文档（含去重不可逆、P5 停写旧列两条）
- [x] 老库升级演练：从当前 DDL 起一份库，执行迁移，无报错、可重复执行
- [x] 三张新表 PO 带 `@Entity/@Id/@Column`（实体映射必需，后续 lambda 化也依赖 `EntityMeta`）
- [x] `AniOpus`、`AniUserOpus` PO 补新增列注解并重新生成

**出口标准**：空库、Docker 空库、老库三种场景都能通过迁移，重复执行无副作用。

### P1 媒体扫描器（2.5 天）

- [x] `MediaScanner`：全量/单部、增量对比、重命名识别（**先配对再插入**；`episode_key + size` 未命中时对 `X` 型按同体积兜底配对，见 §5.1）、软删除与复活、同集 `is_primary` 标记、深度 ≤ 3
- [x] 集数解析器 + 单元测试（数字 / SP / 12.5 / `SP01`≡`SP1` / `SP01.mkv` 不得解析成正片第 1 集 / 无编号特典不共用 `SP1` / 无集数 fallback `X+hash8`）
- [x] 路径归一化（`toRealPath` + 分隔符统一 + Windows 小写）后再取 MD5，补"同一文件不同写法"用例
- [x] Redis 锁（TTL + finally 释放）、待扫队列、自管调度器（读 `media.scan.interval`）、单线程执行、状态对象 + 最近结果落 Redis
- [x] 批量刷新 `last_seen_time`，不逐文件写
- [x] 定时 + 启动 + 事件触发（带 `source`）+ 手动接口（管理员权限）
- [x] 删除/替换 `OpusJobs.autoDiscoverResource` 与 `AniOpusService.autoDiscoverResource`
- [x] 流式播放接口（Range/206/HEAD/416、`Accept-Ranges`、路径根校验、cookie 鉴权）：`/anime/media/**` 从拦截器排除、401/416/500 直接写 `ctx` 不抛异常（`AppFilter` 会把异常改成 200，见 §5.6）
- [x] **补齐 Sa-Token cookie 配置**：三个 profile 的 `sa-token.cookie` 下加 `path: /`、`http-only: true`、`same-site: Lax`；登出由 `StpUtil.logout()` 按同一配置清 cookie。不是"从零写 cookie"——登录已经会写 `Set-Cookie`，缺的是 `Path`（作用域落到 `/api/system`），见 §5.6
- [x] 前端基址与请求头收敛：`axios-util.ts` 里写死的 `/api/${url}` 提成导出常量 `API_BASE`（默认 `/api`，`VITE_API_BASE` 可覆盖），`<video src>` 复用它；请求头 `sa-token` 改成 `Authorization`（与 `token-name` 对齐，先按 §5.6 核对线上配置）
- [x] `listByUser` / `getOpusMedia` 改为读索引（保留 XML），并返回 §6 定义的新字段（`mediaId` / `episodeKey` / `isExtra` / `sortNo` / `displayName` 等）；`hasUpdate` / `isCompleted` 按 §6 的 `caughtUp` 算（关联 progress 判最新正片集是否已看完），**不要只比 `last_episode_key = media_latest_key`**；**`episodes` / `mediaType` / `userOpusId` 等旧字段保留到 P4**
- [x] 首页 SQL 索引验证（`EXPLAIN`）
- [x] 新增单表查询沿用现有写法（XML `<sql id>`）：lambda 只有 sqltoy 6.x 才有，本轮不引入，待 §11 升级后由 §12 统一收敛

**出口标准**：请求链路日志中无 `FileUtil.ls`；10k 文件本地扫描 < 10s；连续两轮扫描结果稳定（无 `is_primary` / `is_deleted` 抖动）。

> 实测（2026-10-30，1 万个文件 + 1 个作品目录，MySQL 8.0.45 走 Docker）：
> 冷扫（索引从零建）7.2s、热扫 3.7~3.8s，连续三轮 `added/updated/removed/renamed/revived` 全为 0（`is_primary`/`is_deleted` 无抖动）。
> 为达标做了两处修正：①`file_mtime` 落库前截断到秒（列是无小数位的 `datetime`，不截断会让每轮扫描都判"已变更"，1 万行被反复重写）；②JDBC URL 加 `rewriteBatchedStatements=true`（sqltoy 的 `saveAll` 逐条发 SQL，不开这个开关 1 万次插入就是 1 万次数据库往返，冷扫 14.5s → 7.2s）。

### P2 播放进度（2.5 天）

- [x] `AniPlaybackService` + 状态机（含"下一集"按 §5.1 **同组内**取后继、"最后一集"只认正片组）
- [x] `POST /anime/userOpus/update` 的手动改状态按 §5.2 规则执行：改「已看完」时同步 `last_episode_key / last_media_id`，并清 `watched_ms`
- [x] 新上报接口：`mediaId` 归属校验、自动建档、按 `(session_id, seq)` 丢弃乱序、`duration_ms` 取大值、同请求更新 `last_episode_key/last_media_id/last_read_time`
- [x] `getOpusMedia` 补 `progressMap`；新增 `/anime/userOpus/continue`
- [x] 历史进度回填（`INSERT ... ON DUPLICATE KEY UPDATE id = id`，`id` 显式生成；完成写 `sys_schema_version` 的 `3.1.0-backfill-progress` 标记，只跑一次）
- [x] 新增 `POST /anime/userOpus/unfollow`；`follow` 的"再点一次取消"语义**保持到 P4** 再改成幂等确认（见 §6 过渡期表）；同时去掉 `follow()` 里写死的 `readingNum = "1"`
- [x] 手写 SQL（进度 upsert、更新动态 upsert、回填）统一补 `id` 生成——三张新表无自增，雪花只在 `lightDao.save()` 里生效
- [x] 单元测试：状态机、越权、幂等、乱序、新集入库后回到继续观看
- [x] 进度表查询写 XML `<sql id>`；upsert 与状态机条件更新保留手写 SQL 并显式写审计时间（见 §5.2）。lambda 化留给 §12
- [x] 旧列过渡期双写（口径见 §4.5；SP 与无集数写空串，避免 `AniOpusHomeVO.readingNum: Integer` 映射失败）
- [x] 过渡期兼容：旧 `updateProgress`、旧媒体接口保留并转调新服务，`follow` 暂维持旧切换语义（见 §6），P4 后再删

**出口标准**：§8.1 里与进度相关的条目全部有对应测试。

### P3 删除通知 + 更新动态（1.5 天）

- [x] 按 §5.4 清单删除后端/前端/配置/依赖
- [x] `V3_1_1__drop_notify.sql`：`DROP TABLE IF EXISTS sys_notify`，与删代码同一次发布（放 P0 会让线上每 60 秒报错）
- [x] 扫描器写入 `ani_opus_update`（`create_time = file_mtime`，`source` 透传，显式写审计字段）
- [x] 更新动态两个查询接口
- [x] 迁移回填每部番最新一集（写 `3.1.0-backfill-update` 标记，与 P2 的进度回填标记分开，见 §4.7）
- [x] grep 验收：无 `SysNotify` / `sys_notify` / `MailUtil` / `findFollowsEmail`

**出口标准**：编译通过，登录与菜单无异常，RSS 下载后能查到更新记录。

### P4 详情页与弹窗播放器（2.5 天）

- [x] `useXgPlayer` / `usePlaybackProgress`
- [x] `PlaybackDialog` + `VideoStage` + 选集条
- [x] 路由改造、旧链接映射（含 `?ep=&t=` 恢复）、5 处 `AnimeVideo` 调用点同批修改、删除 `AnimeVideo.vue`
- [x] keep-alive 改 `:include` + 各路由组件 `defineOptions({name})`
- [x] 关闭/后退语义、`pagehide` 用 `sendBeacon` 补报
- [x] 自动下一集（3 秒可取消倒计时）、续播、错误态、快捷键、无集数文件的展示
- [x] 上报节拍：10 秒 tick + pause/seek/ended/close/切集 补报
- [x] 集数网格与详情页 hero
- [x] 删除过渡期旧接口（`updateProgress`、旧媒体接口）并把 `follow` 改为幂等确认
- [x] `getOpusMedia` 删除只为旧页面保留的字段：`userOpusId`、`episodes`、`mediaType`、`isFolder`（`readingNum` / `readingTime` 随 P5 停写旧列一起删）

**出口标准**：§8.3 全部验收项通过。

### P5 首页与仪表盘（2.5 天）

- [x] 按子计划完成首页视觉重构（`AnimeCard` / `ContinueWatching` / `RecentUpdateStrip` / 筛选抽屉）
- [x] 「我的追番」走 `listByUser` 且只返回已追番（`isFollow = 1`，实现为 `auo.id is not null`）
- [x] 详情页视觉与子计划对齐
- [x] `views/system/dashboard/Dashboard.vue` 增加「更新动态」「扫描状态」
- [x] 我的追番列表与首页卡片视觉对齐
- [x] `AnimeUserOpusView` 数据源迁到新字段（`display_name` / `positionMs` / `durationMs` / `readStatus`），去掉 `readingNum` / `readingTime` / `resourceUrl` 的展示与编辑（停写旧列的前置条件，见 §5.5）
- [x] 停写旧列（§4.5 的落地项）：删掉 `reading_num` / `reading_time` / `total_time` 的双写代码，`resource_url` 一并停写；列本身留到 3.2 再 DROP，回滚旧镜像仍能读到停写前的值
- [x] 清理非 scoped 全局样式（`AdminHome.css` 已随旧首页删除）

**出口标准**：200 部番首屏 P95 < 300ms（本地数据集）；子计划验收清单全部通过。

### P6 回归与发布（0.5 天）

- [x] 第 8 节验收清单逐条执行（8.1/8.2 主要由 P1–P5 各阶段的单元测试与浏览器走查覆盖：状态机 12 例、扫描解析 21 例、P4 弹窗全流程、P5 首页/我的追番/仪表盘；P6 复测安全关键项——未登录流接口 401、报文塞他人 userId 被忽略、pause 落位、播完置已看——均通过）
- [x] 升级文档（含 `sys_notify` 删除不可回滚、去重不可逆、旧列停写说明）：`doc/sql/upgrade-3.1.0.md`
- [x] 后端测试接入 CI（当前测试从未执行）：`.github/workflows/backend-test.yml`，JDK 21 + Maven test；依赖 MySQL/Redis 的集成测试在无环境时自动跳过（`SolonTestSupport` assumeTrue）

**合计：约 13 人日**

---

## 8. 验收清单

### 8.1 进度问题（对应审计 20 项）

| # | 场景 | 期望 |
|---|---|---|
| 1 | 看 A 集 20 分钟 → 切 B 集 → 回 A 集 | A 集位置仍保留 |
| 2 | 存 `SP1`、`12.5` | 不报错，排序正确 |
| 3 | 并发双击追番 | 只产生 1 条关联 |
| 4 | 老数据 `total_time = NULL` | 回填后 `durationMs` 出参为 `0`（未知），首页不出现 NaN 与百分比 |
| 5 | `resource_url` | 代码中无引用 |
| 6 | 播完最后一集 | 状态自动变「已看」 |
| 7 | 直接拖到 10 分钟 | 仍为「未看」（累计观看不足） |
| 8 | 两端同时上报同一集 | `read_status` 不回退（条件更新），位置按 `(session_id, seq)` 取较新的一条；同一集只有一行，位置本身就是后写覆盖，不做多端合并 |
| 9 | 手动改「未看」后静置 | 不自动变回「在看」 |
| 10 | 从首页进入播放 | `last_episode_key` 立即更新 |
| 11 | 播完非最后一集 | 自动进入下一集 |
| 12 | 剧集从第 5 集开始 | 首页显示正确，无下标错位 |
| 13 | 刚追番未播放 | 不显示「已观看至第 01 集」，也不挂 NEW 角标 |
| 14 | 未追番直接播放 | 进度可保存 |
| 15 | 切集后立刻刷新 | 新集位置为 0，不带旧集位置 |
| 16 | 正常播放 1 分钟 | 6 次 tick + 事件补报，总数 ≤ 8 次 |
| 17 | 离开页面 | 无定时请求、无残留播放器 |
| 18 | 换片源后进入 | 不跳到片尾 |
| 19 | 新番无进度 | 不弹续播提示 |
| 20 | 报文里塞他人 `userOpusId` / `userId` | 字段被忽略，只写当前登录用户的进度 |
| 21 | 已看完最后一集，之后又更新一集 | 重新出现在「继续观看」，卡片 ✓ 消失、改出 NEW，无需手工改状态 |
| 22 | 无集数文件（剧场版、PV、`movie.mkv`） | 各自独立成集，`episode_key` 不冲突，可播放可记进度 |
| 23 | 未追番的作品 | 不出现在「我的追番」，但进度仍可保存 |
| 24 | 播放中直接关标签页 | `pagehide` 用 `sendBeacon` 落库，重新进入位置正确 |
| 25 | 打开旧链接 `/admin/ani-video/:id/:num/:time` | 重定向到详情页并定位到对应集与位置 |
| 26 | 两集之间来回切 5 次 | 每集位置各自独立，互不覆盖 |
| 27 | 上报乱序（旧 `seq` 晚到） | 丢弃，位置与状态不回退 |
| 28 | 手动标记「已看完」（当前只看到第 5 集、最新第 12 集） | 不进入「继续观看」；第 13 集入库后自动回到「继续观看」 |
| 29 | 过渡期旧页面调用 `updateProgress` | 仍能落库（查不到 media 时只写旧列），不报错 |
| 30 | 已看完 12 集后删掉第 12 集的文件 | 卡片显示「已看 11/11」，不出现「已看 12/11」（`completedCount` 与当前剧集集合取交集） |
| 31 | 播放 `SP1` 后打开旧首页 | 不报映射异常（双写时 SP 的 `reading_num` 为空串），新首页显示 `SP1` |
| 32 | 卡片 ✓ 的判定 | 手动标记「已看完」出 ✓；手动标记「未看」不出 ✓，且集数网格里每集的 ✓ 保留；12 集里只播完第 5 集不出 ✓ |
| 33 | 拖到 96% 直接关页面（未触发 `ended`），且该集就是正片最后一集 | 卡片出 ✓，且不再出现在「继续观看」：状态机把 `read_status` 一并置 1，只置 `is_completed` 会让作品一边打 ✓ 一边挂在首页 |
| 34 | 看完第 12 集后再看 SP1 | 卡片不出 NEW（`last_episode_key` 变成 `SP1`，只靠等值比较会永久误报），作品也不回到「继续观看」 |

### 8.2 媒体扫描

| 场景 | 期望 |
|---|---|
| 新增文件 | 一个扫描周期内出现在列表 |
| 删除文件 | 软删除，列表集数减一 |
| 重命名 `3.mp4` → `03.mp4` | 集数不变、该集进度保留 |
| 无集数文件改名（`movie.mkv` → `movie.1080p.mkv`，体积不变） | 按同体积配对复用旧行，进度保留 |
| 无集数文件转码后体积变化 | 视为新片，旧行软删、进度从头开始（已知边界，不报错） |
| 放入 `SP1.mkv`、`12.5.mkv` | 正确排序、可播放、可记进度；`SP01.mkv` 必须归为 `SP1`，不能占正片第 1 集 |
| 放两个没编号的特典（`特典A.mkv`、`OVA-B.mkv`） | 都走 `X+hash8`，是两个独立条目，不会互相覆盖 |
| `{opus}/Season 1/第01话/a.mkv`（三层） | 能扫到，集数从最内层目录解析 |
| NAS 断开 | 本次扫描失败，`/anime/media/scan/status` 返回失败原因，列表数据不变 |
| 大文件拖动 | 206 分段响应 |
| 同一集两个文件（`1.mp4` + `第01话/a.mkv`） | 详情页只出现一个「第 1 集」 |
| 删除文件后同路径重新拷入 | 原地复活，不产生重复行 |
| 未登录 / cookie 失效请求流接口 | HTTP 401（不是 200 + JSON），不返回文件内容 |
| 只保留 cookie、清空 localStorage 后播放 | 正常播放（`<video>` 只带得上 cookie） |
| 扫描期间浏览/播放 | 不受影响 |
| 连续两轮扫描 | `is_primary` / `is_deleted` 不抖动，集数与文件数稳定 |
| NAS 路径大小写变化（`A.mkv` ↔ `a.mkv`） | 视为同一文件，不产生重复行 |
| 非管理员调用扫描接口 | 403 |
| 扫描进程被 kill | 重启后数据一致，无半行、无重复行 |
| 全新 Docker 初始化（`001-init.sql`） | 结构与运行时迁移后的结构一致 |
| 手工打过补丁的老库启动 | 结构探测写 baseline，不重复执行已生效的变更 |

### 8.3 弹窗播放

| 场景 | 期望 |
|---|---|
| 点击集数 | 300ms 内弹窗播放，URL 带 `ep` |
| 刷新带 `ep` | 同集同位置继续 |
| 关闭 / ESC / 后退 | 进度落库、实例销毁、无残留声音 |
| 弹窗内切集 | 不重建实例、不黑屏 |
| 全屏中 ESC | 只退全屏 |
| 移动端 | 全屏播放、横屏自动全屏 |
| 离开详情页 | 弹窗自动关闭 |
| 播完非最后一集 | 3s 倒计时，可「立即播放」也可取消；取消后停在当前集 |
| 播完最后一集 | 不弹下一集，提示「已看完本番」 |
| 有 SP 时播完第 12 集（正片最后一集） | 不把 SP1 当成「下一集」自动播放；作品变「已看」，SP 仍需手点 |
| 播完最后一个 SP（作品有正片） | 不自动跳到正片第一集，也不改变作品的「已看」状态 |
| 作品只有剧场版 / SP（没有正片） | 播完最后一个同样置「已看」，不留在「继续观看」 |

### 8.4 更新动态

| 场景 | 期望 |
|---|---|
| RSS 下载完成 | 首页「最近更新」出现「《xxx》{displayName} 更新了」 |
| 重复扫描 / 重下同一集 | 只出现一次 |
| 手动上传 / 外部拷入 | 同样产生记录 |
| 仪表盘筛选分页 | 正常 |
| 全局搜索 | 无 `sys_notify` / `ani-notify-api` 残留 |
| 旧定时扫描任务 | 代码与菜单中均已移除，不会与扫描器重复写 |
| 更新动态 `create_time` | 等于文件 mtime，不是扫描时间 |

---

## 9. 风险与应对

| 风险 | 影响 | 应对 |
|---|---|---|
| `episode_key` 变化导致进度丢失 | 高 | 统一定义为"数字去零字符串 + SP 归一为 `SP1`"，扫描器与迁移共用同一函数；先灰度验证再删旧列 |
| NAS 扫描慢/掉线 | 中 | 单线程 + 每部事务 + 失败保留旧索引；状态可观测 |
| 文件重命名识别失败 | 中 | 先按 `episode_key + size` 配对，再对两侧都是 `X` 型（无集数）的按同体积兜底配对；体积也变（转码 / 换源）时进度必然丢，属已知边界，验收里单列 |
| JSON 序列化把 null 写成 0/空串 | 中 | 前端一律按 `0` / 空串判空；正片与 SP 的分组改看 `isExtra`，不再依赖 `episodeNo` 是否为空（见 §6 序列化约定） |
| `<video>` 无凭证导致流接口 401 | 高 | 登录本来就会写 `Authorization` cookie，但没配 `Path`（浏览器把作用域算成 `/api/system`）、也没有 `HttpOnly`/`SameSite`；P1 按 §5.6 在三个 profile 的 `sa-token.cookie` 下补 `path: /`，验收里用"只留 cookie、清掉 localStorage"验证 |
| 弹窗与全屏/PiP 冲突 | 中 | 全屏用播放器自身能力；ESC 优先级判断；关闭前 flush |
| 老用户升级后通知消失 | 低 | 升级说明明确告知；更新动态替代 |
| 回滚后 `sys_notify` 不存在 | 中 | 升级说明写明回滚步骤；如需可提供重建 SQL |
| 前后端同包发布 | 低 | 前端静态资源打包进 jar，无版本错位 |
| 路径大小写/分隔符差异产生重复行 | 中 | 入库前统一归一化（`toRealPath` → `/` → Windows 转小写）再算 `file_path_hash`；唯一键建在哈希上 |
| 结构探测误判基线 | 中 | 只读 `information_schema`；探测异常即中止启动并打印差异，不猜结构 |
| Docker 初始化脚本与运行时迁移漂移 | 中 | P0 先把 `total_time`、`ani_user_feed` 合入 docker `ddl-sql.sql`；init 脚本只承载 baseline，变更仍由 `SchemaMigrator` 执行 |
| 去重软删不可逆 | 中 | 不用软删做去重，改 `is_primary` 标记；同集冗余文件仍在，可随时改回主文件 |
| 扫描器与旧 `OpusJobs` 双写 | 低 | 旧定时任务与扫描器在同一提交内下线，不留灰度期 |
| 迁移失败导致启动不可用 | 中 | 迁移失败即 fail-fast 并输出可执行的修复 SQL；提供 `--skip-migrate` 启动开关应急 |
| 删表脚本与删代码跨发布错位 | 中 | `V3_1_1__drop_notify.sql` 与 P3 代码删除必须同一次发布；提前删表会让 `SysNotifyJobs` 每 60 秒报错 |
| 过渡期旧接口依赖扫描索引 | 中 | 旧 `updateProgress` 转换需要 `ani_media` 已有数据；查不到时只写旧列不报错，扫描/回填完成后新进度自然补齐 |
| 异常被 `AppFilter` 改写成 HTTP 200 | 中 | 流接口自己写 401/416/500、不抛异常（§5.6）；排查"视频打不开"时先看响应状态与 body 是不是 `ApiResult`，别只看后端日志 |

---

## 10. 待确认

无。原 6 项已全部确认，见 §2；UI 细节的开放问题记录在子计划 §11。

---

## 11. 工程待办（TODO）

不属于 P0–P6 的交付内容，单独排期、单独提交，不和播放改造混在一起。

- [ ] **升级 sqltoy 到最新版（实际是 sqltoy 6 + Solon 4 一起升）**
  - 现状：根 `pom.xml` `sqltoy.version = 5.6.46`（`com.sagframe:sagacity-sqltoy-solon-plugin`），Solon 3.10.1，redisx 1.4.7
  - 依赖证据（各版本插件 POM 里的 `solon.version`）：`5.6.46` → Solon 3.3.0；`5.6.96` → **Solon 4.1.0**；`6.0.9` → **Solon 4.1.1**，后两者同时要求 redisx 1.8.6
  - 结论：不存在"只升 sqltoy、不动 Solon"的中间版本，`5.6.96` 与 `6.0.9` 都会跨 Solon 大版本，不能按"小步升级"排期
  - 路径 A（🟡 默认）：作为独立迭代，`sqltoy 6.0.9` + `Solon 4.1.x` + `redisx 1.8.x` + 对应 `sa-token-solon-plugin` 一起升级，全量回归后再做 §12
  - 路径 B（仅当 A 受阻）：用 `exclusions` 挡掉插件传递的 solon/redisx 保持 3.10.1，需先验证 lambda 未依赖 Solon 4 的 ABI；验证不通过则 §12 不做
  - 重点回归：Solon 生命周期事件、`@Scheduled`、Sa-Token 拦截器、`LightDao` 的 `find / findOne / findPage / update / updateByQuery / save`、`EntityUpdate`、`@Tran`、雪花 ID、动态表名、`sql.xml` 加载告警
  - 覆盖接口：`ani_user_opus_listByUser`（分页 + count）、`getOpusMedia`、`listFromShares`、系统模块的 `findPage`
  - 验收：编译通过、启动无告警、上述接口返回结构与升级前一致、`showSql` 生成的分页语句无变化
  - 风险：Solon 4 的生命周期/事件/插件坐标变化面远大于 sqltoy 本身；`Page`/`ApiPage` 转换行为、6.x 方言与分页语法调整

- [ ] **单表查询 SQL 改 Lambda**（**阻塞于上一项**，设计见 §12）
  - 当前 5.6.46 没有 lambda API（jar 内无 `org.sagacity.sqltoy.lambda`），P0–P6 期间新增查询仍写 XML
  - 9 个可直接改、3 个需先统一返回类型、13 个保留 XML（含 2 个随通知系统删除）
  - 升级完成后起，新增单表查询不再写 `<sql id>`

---

## 12. 单表查询 Lambda 化

前提：先完成 §11 的第一项（sqltoy 6.0.9）。该版本自带 `org.sagacity.sqltoy.lambda`（5.6.46 的 jar 里没有这个包），无需引入新依赖；但插件 6.0.9 依赖 Solon 4.1.1，所以这项改造实际排在 Solon 4 升级之后，不随 P0–P6 发布。

### 12.1 能力边界

| 项 | 说明 |
|---|---|
| 入口 | `lightDao.lambdaQuery(Entity.class)`（内部自动 bind）、`lightDao.lambdaUpdate(...)`、`lightDao.lambdaDelete(...)` |
| 条件 | 方法引用 + `eq/ne/gt/ge/lt/le/like/likeLeft/likeRight/in/isNull/between/...`，首参 `boolean` 控制该条件是否生效 |
| 查询 | `find()`、`getOne()`、`getFirst()`、`findPage(Page)`、`count()`、`exists()`、`findColumns(fn)`、`sum/avg/max/min`、`findTop/findRandom` |
| 结果类型 | **只能是实体类**；VO 投影、字段别名不支持的查询仍留在 XML |
| 关联 | 6.x 提供 `leftJoin/innerJoin`、`exists`、`inSql`、CTE，但本计划不改 JOIN 查询 |
| 运行环境 | 6.x 编译目标为 Java 17（项目 JDK 21 ✅） |

### 12.2 判定标准

能改（全部满足）：

1. 单表，无 JOIN
2. 返回实体，或返回列是实体列的子集（列名一致）
3. 不依赖 XML 专属能力：`<translate>`、`<shuffle>`、`<secure>`、`cache`、`page-optimize`
4. 条件能表达为方法引用（`order by RAND(now())`、`limit :参数`、窗口函数等除外）

不能改：JOIN 查询、返回 VO/DTO 且字段名与实体不一致、依赖数据库函数或方言的排序/截断。

### 12.3 现状清单（25 个 `<sql id>`）

| 分类 | 数量 | 明细 |
|---|---|---|
| 可直接改 | 9 | `ani_opus_findList`、`ani_opus_count`（→ `count()`）、`ani_opus_tag_findList`、`ani_tag_findList`、`ani_user_opus_findList`、`sys_dictionary_findList`、`sys_user_getOne`、`sys_user_role_findList`、`sys_role_menu_findList` |
| 需先统一返回类型 | 3 | `sys_user_findList`、`sys_role_findList`（同一 SQL 同时映射实体与 VO）、`sys_menu_findList`（只映射 `SysMenuVO`） |
| 保留 XML | 13 | 8 个 JOIN（`ani_opus_JOIN_findList`、`ani_opus_listByUser`、`ani_opus_tag_findListByOpusIds`、`ani_user_opus_listByUser`、`ani_user_opus_findByShares`、`ani_user_feed_*` ×2、`sys_log_JOIN_findList`）、`sys_menu_IN_findList`（`IN (子查询)`，条件无法用方法引用表达）、`sys_user_listActive`、`sys_user_search`（VO + 三列投影）、`sys_notify_findList` 与 `ani_user_opus_findFollowsEmail`（随通知删除） |

「需先统一返回类型」的三个：先在 Service 层拆成「实体查询 + 转换」，再改 lambda；如果拆完调用方更绕，就保留 XML，不为改造而改造。

### 12.4 改造写法

```java
// 改前
Map<String, Object> map = MapUtil.newHashMap(2);
map.put("opusId", opusId);
map.put("userId", loginId);
AniUserOpus exist = lightDao.findOne("ani_user_opus_findList", map, AniUserOpus.class);

// 改后
AniUserOpus exist = lightDao.lambdaQuery(AniUserOpus.class)
        .eq(AniUserOpus::getOpusId, opusId)
        .eq(AniUserOpus::getUserId, loginId)
        .getOne();
```

```java
// 分页 + 排序
Page<AniOpus> page = lightDao.lambdaQuery(AniOpus.class)
        .eq(AniOpus::getIsDeleted, 0)
        .eq(StrUtil.isNotBlank(nameCn), AniOpus::getNameCn, nameCn)
        .orderByDesc(AniOpus::getId)
        .findPage(ApiPage.create(pageDTO));

// 列表
List<AniTag> tags = lightDao.lambdaQuery(AniTag.class)
        .in(CollectionUtil.isNotEmpty(tagIds), AniTag::getId, tagIds)
        .find();
```

`SqlToyLazyDao` 的调用点用 `LambdaQuery.create(X.class).bind(sqlToyLazyDao)`，或统一改成注入 `LightDao`（同一类里通常已有）。

### 12.5 语义对齐（改造前逐条核对）

| 项 | XML 现状 | Lambda 行为 | 处理 |
|---|---|---|---|
| 空值 | `#[and x = :x]` 参数为 null 时整段消失 | 默认 `ValueFilter.NON_NULL`，null 跳过 | 一致，勿开 `strict()` |
| 空串 | 空串会参与条件 | 空串参与条件（NON_NULL 只过滤 null） | 需要"空串也跳过"时用 `.eq(StrUtil.isNotBlank(v), ...)` 或 `.valueFilter(ValueFilter.ignore(""))` |
| 模糊查询 | 调用方传 `%关键字%` | `like()` 自动包 `%v%`；`likeExact()` 不包 | **去掉调用方手写的 `%`**，否则变成 `%%v%%` |
| 前后模糊 | 手写 `%kw%` 由 SQL 决定 | `likeLeft` = `%v`、`likeRight` = `v%` | 按原意选对应方法 |
| `in` 空集合 | `#[and id in (:ids)]` 空集合时整段消失 | `in(...)` 空集合默认跳过（同 NON_NULL 语义需验证） | 改造时补一条空集合用例 |
| 排序 | `order by id desc` | `orderByDesc(Entity::getId)` | 一致 |
| 计数 | 手写 `select count(1)` | `count()` | 确认 `is_deleted = 0` 等固定条件仍带上 |
| 更新 | `EntityUpdate` + `updateByQuery` | `LambdaUpdate` 内部同样走 `updateByQuery` | `updateTime/updateBy` 自动填充仍生效，`create_time` 不会被改 |

lambda 没有 upsert / `ON DUPLICATE KEY UPDATE`，进度表的写入（§5.2）与状态机条件更新不在此范围内，保留手写 SQL。

### 12.6 实施顺序

1. `ani_*` 小表（4 个）：改动面小，先验证写法与 unify-fields 行为
2. 系统表只读查询（`sys_user_getOne`、`sys_user_role_findList`、`sys_role_menu_findList`、`sys_dictionary_findList`）
3. 三个「需先统一返回类型」的，逐个评估拆或留
4. 清理：删除已无引用的 `<sql id>`，`grep -r "xxx_findList"` 确认零引用
5. 不保留双份实现（不搞 XML + lambda 并行）

### 12.7 新代码规范

- 前提：lambda 只有 sqltoy 6.x 才有（5.6.46 的 jar 里没有 `org.sagacity.sqltoy.lambda`），所以**P1/P2 期间新增的单表查询仍写 XML `<sql id>`**
- sqltoy 6 升级完成后：新增单表查询一律用 lambda、不再新增 `<sql id>`，并回头把 P1/P2 期间写下的 XML 一并收敛
- 只有 JOIN、聚合报表、VO 投影才写 XML，且写上注释说明为何不能用 lambda
- 新表 PO 必须带 `@Entity(tableName=...)`、`@Id`、`@Column`（lambda 依赖 `EntityMeta`）

### 12.8 验收与风险

验收：

- [ ] 每个改动的查询都有等价用例：结果集、排序、分页 total 与改造前一致
- [ ] 用 `.getSql()` 在单测里断言关键查询生成的 SQL 片段（表名、条件、order by）
- [ ] 开启 `debug` 抽查 `showSql` 输出，确认无 `%%` 双通配、无漏条件
- [ ] 改造后 `sqltoy/*.sql.xml` 行数与 id 数下降，且无悬空 id

风险：

| 风险 | 应对 |
|---|---|
| `like` 双通配导致查不到数据 | 12.5 明确去掉调用方 `%`，并加用例 |
| 空集合 `in` 变成无条件查询（全表返回） | 补空集合用例；必要时显式 `CollectionUtil.isNotEmpty` 守卫 |
| `updateByQuery` 自动填充差异 | 对比改造前后 `update_time/update_by` 落库值 |
| 混合返回类型的 SQL 拆出两份逻辑，维护成本上升 | 评估收益，收益低就直接保留 XML |
| 6.x lambda API 在后续小版本变动 | 版本固定在 `sqltoy.version`，升级需回归 12.8 全部用例 |

---

## 13. 变更记录

| 日期 | 内容 |
|---|---|
| 2026-10-27 | 初版：进度模型重做、媒体扫描、通知删除与更新动态、弹窗播放、首页/仪表盘改版 |
| 2026-10-27 | 表结构统一：新增 4.1 表设计约定，三张新表改为存量 DDL 风格（反引号、全字段注释、统一审计字段、`_time` 后缀、`{表}_idx/uk_N` 索引） |
| 2026-10-27 | 6 项待确认全部拍板；首页改为视觉重构，新增子计划 `playback-ui-redesign.md`；路由改为沿用 `/admin` 前缀 |
| 2026-10-27 | 新增 §11 工程待办：sqltoy 5.6.46 → 6.0.9 |
| 2026-10-27 | 新增 §12 单表查询 Lambda 化：能力边界、判定标准、25 个 SQL 分类、语义对齐、实施顺序与验收 |
| 2026-10-29 | 审计修正：`ani_media` 唯一键改 `(opus_id, file_path_hash)`（utf8mb4 索引 3072 字节上限）；`getOpusMedia` 改为"必须变更返回结构"；乱序判定改按 `(session_id, seq)`；状态机与秒级配置对齐；upsert 明确用手写 SQL；迁移改为 HTTP 启动前执行并补齐与 init 脚本的关系；补继续观看接口、首页字段、流接口鉴权、并发追番幂等、存量 PO、上报节拍、软删复活与同集去重；§11 升级口径改为「sqltoy 6 + Solon 4」 |
| 2026-10-29 | 第二轮审计：lambda 只有 sqltoy 6.x 才有（5.6.46 无 `org.sagacity.sqltoy.lambda`），撤回 P1/P2/§12.7 中"本轮用 lambda"的要求，改为本轮写 XML、升级后统一收敛；`sys_menu_IN_findList` 因 `IN (子查询)` 改归保留 XML，分类数改为 9/3/13 |
| 2026-10-29 | 第三轮审计并直接修正：迁移钩子改为 `Solon.start(class, args, consumer)` 回调内执行；docker 初始化脚本先合并 `total_time`/`ani_user_feed`，init 只写 baseline；同集去重改 `is_primary` 不软删；非数字集数 `episode_key` 兜底 `X+hash8`；排序统一 `ORDER BY sort_no IS NULL, sort_no, id`；新增 `last_media_id` 并明确来源；继续观看判据改等值比较（新集自动回来）；`listByUser` 增 `isFollow` 过滤，追番拆 `follow`/`unfollow`；下线 `OpusJobs.autoDiscoverResource`；路径归一化后再哈希；`last_seen_time` 批量刷新；补字段定义表、旧链接映射（改 `?ek=` 由详情页反查 `mediaId`）、`sendBeacon`、3s 倒计时、pom 路径、死菜单清理；新增 `mediaCount` 字段、`hasUpdate` 排除未开始看；补 P2–P4 过渡期接口兼容表；§8 补 19 条验收、§9 补 6 条风险 |
| 2026-10-29 | 第四轮审计修正：`sys_notify` 删表移到 P3 的 `V3_1_1__drop_notify.sql`（P0 删表会让 60 秒定时任务持续报错）；流媒体路径统一 `/anime/media/{mediaId}/stream`，注明浏览器侧带 `/api` context path；扫描流程改为「先配对重命名再插入」避免唯一键冲突，深度放宽到 3 层；手动「已看完」同步 `last_episode_key/last_media_id`（否则立刻弹回继续观看）并绑定 `POST /userOpus/update`；补 `(user_id,last_read_time)` 索引与索引存在性判断、结构探测覆盖全部对象、去重合并规则、回填 `checksum`、自动建档 `create_time`；`AnimeUserOpusView` 必须迁新字段（停写旧列前置条件）；旧 `updateProgress` 补转换链；`listByUser` 默认排序改 `last_read_time`；验收 #8 措辞修正，新增手动已看完、三层目录、过渡期旧接口 3 条用例 |
| 2026-10-30 | 第五轮审计修正：`<video>` 鉴权补「登录写 token cookie」（sa-token 只读不写、登录只回 body，不补这步流接口必 401）；新增 §6 序列化约定（`WriteNullNumberAsZero`/`WriteNullStringAsEmpty`/`BrowserCompatible`）并把正片/SP 分组改成 `isExtra`；`getOpusMedia` 的 `userOpusId`/`episodes`/`mediaType` 保留到 P4；过渡期旧媒体接口改按 `opus_id + file_name` 查（旧前端传的是 `resName`）；三张新表手写 SQL 补 `id` 生成；结构探测不再判版本表；更新动态改 upsert 让 `media_id` 跟随主文件；`completedCount` 与当前剧集集合取交集；无集数文件改名按同体积配对并写明已知边界；关闭弹窗、`/api` 基址口径与子计划对齐；双写时 SP 写空串（`AniOpusHomeVO.readingNum` 是 `Integer`）；`listByUser` 增 `isCompleted`；§8 补 5 条验收、§9 补 3 条风险 |
| 2026-10-30 | 第六轮自查修正：更新动态的 `displayName` 改为 JOIN `ani_media` 取 `display_name`（原写法用 `episode_no` 拼，SP/无集数会出「第 null 集」）；死链菜单改**原地 `UPDATE router_path = '/admin/ani-home'`**（菜单是 `router.push({path})`，`/#/` 前缀匹配不到；`sys_role_menu` 有引用，删行会让全新安装丢菜单），种子数据同批改而不删；P5 补「停写旧列」任务（§4.5 只写了时点，清单里没有落地项）；`isCompleted`（作品级）改为「`readStatus != 0` 且（`caughtUp` 或 `completedCount >= mediaCount`）」（手动标已看完、拖到 96% 关页面都要出 ✓，手动标未看不出 ✓，只播完第 5 集不会凭空打 ✓，新集上线后 ✓ 自动消失；集数网格的每集 ✓ 不受影响）；§5.5 卡片字段补 ✓ 来源、§8.1 补 1 条验收；`duration_ms` 取大值补 `COALESCE`（`GREATEST(NULL, x)` 返回 NULL，首条写入会把总时长写丢）；`API_BASE` 口径改为「现在没有这个常量、P1 建立」（原文说的"axios 基址常量"并不存在，`axios-util.ts` 是写死的 `/api`），请求头 `sa-token` 与 `token-name` 不一致改为「先核对线上配置再改」；补 `sortNo` 出参 `0` 时前端不得重排；流接口鉴权口径修正——`AppFilter` 把所有异常渲染成 HTTP 200 + `ApiResult`（`AppFilter.java:63`），拦到未登录只会让 `<video>` 收到 200 + JSON，因此 `/anime/media/**` 要从拦截器排除、handler 内自校验并直接写 401/416/500，§5.6 与 P1 清单同步、§8.2 验收改为「HTTP 401」、§9 补 1 条风险；回填标记拆成 `3.1.0-backfill-progress`（P2）与 `3.1.0-backfill-update`（P3），避免 P2 先写标记把 P3 的回填一起跳过；§4.6 补「DDL 隐式提交，半升级只能靠幂等重跑」；集数解析把 `SP/OVA/特典` 判定提到所有数字规则之前（否则 `SP01.mkv` 会按数字命成正片第 1 集），无编号特典归 `X+hash8`（避免多个特典共用 `SP1` 被 `is_primary` 去重），§8.2 补 2 条扫描验收；状态机补「`position/duration ≥ 0.95` 与 `ended` 同等对待」（拖到片尾直接关页面不一定派发 `ended`），且最后一集走到 0.95 时 `read_status` 一并置 1，否则会出现「卡片打 ✓、作品还挂在继续观看」，§8.1 补 #33；`media_latest_key` 改为**正片优先**（SP 的 `sort_no = 10000 + n` 永远压过第 12 集，会让已看完的作品永远回到「继续观看」、每来一个 SP 误报一次 NEW），「下一集」改成**同组内找后继**（否则第 12 集播完会「自动下一集」到 SP1，且 `read_status` 永远等不到 1），只有额外内容、没有正片的作品例外（在额外内容组内播完仍置「已看」，否则永远留在继续观看），§8.3 补 3 条验收，子计划 §5 的「最新一集」同步；把「已看完最新正片」收敛成 §6 的 `caughtUp`，`hasUpdate` 与「继续观看」排除都改用它（原来只比 `last_episode_key = media_latest_key`，看完正片再看 SP 后 `last_episode_key` 变 `SP1`，会导致 NEW 常挂、作品反复回到继续观看），§8.1 补 #34 |
| 2026-10-30 | 第七轮可执行性验证（对代码与依赖实测）：`mvn -DskipTests package` 通过（JDK 21），解析版本 `sa-token-core 1.44.0`、`sqltoy 5.6.46`、MySQL 8.0.45。修正 4 处与实测不符的结论：①**迁移钩子拿不到 `DataSource`**——`javap` 核对 `SolonApp` 实际顺序是 `startDo = initAwait → init(consumer) → cfg.revise → run`，而 `run` 里依次是插件 init、插件 start（端口开始监听）、beanScan（`DbConfig.db1` 此时才创建），回调早于端口监听但也早于 bean 创建，`getBean(DataSource.class)` 必失败，改为用 `DriverManager` 按 `myapp.db1.*` 开一条连接（§4.6）；②**「系统里没有 cookie」不成立**——sa-token 1.44.0 的 `StpLogic.setTokenValue` 在 `isReadCookie`（默认 true）下会写 `Set-Cookie`，只是 `SaCookieConfig.path` 默认 null，浏览器把作用域算成 `/api/system`，`<video>` 仍带不上；修法改为配置 `sa-token.cookie.path/http-only/same-site`，不再手写 `addCookie`（§5.6、P1、§9）；③`listByUser` 现有排序是 `str_to_date(ao.launch_start, '%Y年%m月%d日') desc, auo.create_time desc`，不是 `launch_start desc`；④`ani_user_opus` 没有 `is_follow` 列，`isFollow = 1` 必须实现为 `auo.id is not null`（§5.5、§6、P4 验收） |
| 2026-10-30 | P1 落地与实测：`MediaScanner`/`MediaScanExecutor`/`EpisodeParser`/`MediaPathUtils`/`MediaStreamer` 五个新类 + `AniMediaController`（扫描/状态/流）；旧 `OpusJobs.autoDiscoverResource` 与 `AniOpusService.autoDiscoverResource` 下线；`listByUser`/`getOpusMedia` 改读 `ani_media` 索引（新增 `ani_media.sql.xml` 两个 `<sql id>`）；三 profile 补 `sa-token.cookie`；前端抽 `API_BASE`、请求头改 `Authorization`。实测修正三处：①`file_mtime` 截断到秒（否则每轮扫描重写全部行）；②`relative_path` 改用真实路径计算（归一化在 Windows 下会转小写，展示字段不该丢大小写）；③JDBC URL 加 `rewriteBatchedStatements=true`（sqltoy `saveAll` 逐条发 SQL，1 万次往返把冷扫拖到 14.5s）。1 万文件冷扫 7.2s / 热扫 3.7s，连续三轮零变更；流接口 200/206/416/HEAD/401 全部实测通过 |
