-- elysiananime.ani_opus definition

CREATE TABLE `ani_opus` (
                            `id` bigint NOT NULL COMMENT '作品id',
                            `name_original` varchar(100) COLLATE utf8mb3_unicode_ci DEFAULT NULL COMMENT '原名',
                            `name_cn` varchar(100) COLLATE utf8mb3_unicode_ci DEFAULT NULL COMMENT '中文名',
                            `cover_url` varchar(200) COLLATE utf8mb3_unicode_ci DEFAULT NULL COMMENT '封面地址',
                            `detail_info_url` varchar(200) COLLATE utf8mb3_unicode_ci DEFAULT NULL COMMENT '详细链接',
                            `episodes` varchar(100) COLLATE utf8mb3_unicode_ci DEFAULT NULL COMMENT '话数',
                            `score` float DEFAULT NULL COMMENT '评分',
                            `launch_start` varchar(100) COLLATE utf8mb3_unicode_ci DEFAULT NULL COMMENT '放送开始',
                            `delivery_week` varchar(100) COLLATE utf8mb3_unicode_ci DEFAULT NULL COMMENT '放送星期',
                            `ani_summary` text COLLATE utf8mb3_unicode_ci COMMENT '作品简介',
                            `has_resource` tinyint DEFAULT '0' COMMENT '是否有资源：0否 1是',
                            `rss_url` varchar(900) COLLATE utf8mb3_unicode_ci DEFAULT NULL COMMENT 'rss订阅链接',
                            `rss_status` tinyint DEFAULT '0' COMMENT '订阅状态: 0未订阅 1订阅中 2订阅完成',
                            `rss_level_index` int DEFAULT '0' COMMENT '资源标题集数出现的位置（数字匹配）',
                            `rss_file_type` varchar(10) COLLATE utf8mb3_unicode_ci DEFAULT NULL COMMENT '要订阅的资源格式',
                            `rss_only_mark` varchar(50) COLLATE utf8mb3_unicode_ci DEFAULT NULL COMMENT '匹配的唯一标识',
                            `rss_exclude_res` varchar(50) COLLATE utf8mb3_unicode_ci DEFAULT NULL COMMENT '排除的资源',
                            `media_dir` varchar(512) COLLATE utf8mb3_unicode_ci DEFAULT NULL COMMENT '资源目录绝对路径',
                            `media_count` int NOT NULL DEFAULT '0' COMMENT '已索引媒体数',
                            `media_latest_key` varchar(64) COLLATE utf8mb3_unicode_ci DEFAULT NULL COMMENT '最新一集的 episode_key',
                            `media_scan_time` datetime DEFAULT NULL COMMENT '最近一次扫描成功时间',
                            `create_time` datetime NOT NULL COMMENT '创建时间',
                            `create_by` bigint DEFAULT NULL COMMENT '创建人',
                            `update_time` datetime NOT NULL COMMENT '更新时间',
                            `update_by` bigint DEFAULT NULL COMMENT '更新人',
                            `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
                            PRIMARY KEY (`id`),
                            KEY `ani_opus_idx_1` (`name_cn`,`name_original`),
                            KEY `ani_opus_idx_2` (`launch_start`,`delivery_week`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='作品主表';


-- elysiananime.ani_opus_group definition

CREATE TABLE `ani_opus_group` (
                                  `id` bigint NOT NULL COMMENT '作品分组id',
                                  `group_name` varchar(100) COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '组名称',
                                  `group_remark` varchar(200) COLLATE utf8mb3_unicode_ci DEFAULT NULL COMMENT '组备注',
                                  `create_by` bigint NOT NULL COMMENT '创建人',
                                  `create_time` datetime NOT NULL COMMENT '创建时间',
                                  `update_by` bigint NOT NULL COMMENT '更新人',
                                  `update_time` datetime NOT NULL COMMENT '更新时间',
                                  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='作品分组表';


-- elysiananime.ani_opus_tag definition

CREATE TABLE `ani_opus_tag` (
                                `id` bigint NOT NULL COMMENT '作品标签关联id',
                                `tag_id` bigint NOT NULL COMMENT '标签id',
                                `opus_id` bigint NOT NULL COMMENT '作品id',
                                `create_time` datetime DEFAULT NULL COMMENT '关联时间',
                                PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='作品标签关联表';


-- elysiananime.ani_tag definition

CREATE TABLE `ani_tag` (
                           `id` bigint NOT NULL COMMENT '标签id',
                           `tag_name` varchar(100) COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '标签名称',
                           `create_time` datetime NOT NULL COMMENT '创建时间',
                           `create_by` bigint NOT NULL COMMENT '创建人',
                           `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                           `update_by` bigint DEFAULT NULL COMMENT '更新人',
                           `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='acg标签表';


-- elysiananime.ani_user_opus definition

CREATE TABLE `ani_user_opus` (
                                 `id` bigint NOT NULL COMMENT '用户作品关联id',
                                 `user_id` bigint NOT NULL COMMENT '用户id',
                                 `opus_id` bigint NOT NULL COMMENT '作品id',
                                 `resource_url` varchar(200) COLLATE utf8mb3_unicode_ci DEFAULT NULL COMMENT '资源地址',
                                 `reading_num` varchar(20) COLLATE utf8mb3_unicode_ci NOT NULL DEFAULT '0' COMMENT '正在的播放集数',
                                 `reading_time` bigint NOT NULL DEFAULT '0' COMMENT '正在播放的时长',
                                 `total_time` bigint DEFAULT NULL COMMENT '视频总时长',
                                 `read_status` tinyint NOT NULL DEFAULT '0' COMMENT '观看状态：0未看 1已看 2在看',
                                 `create_time` datetime NOT NULL COMMENT '关联时间',
                                 `is_share` tinyint NOT NULL DEFAULT '0' COMMENT '是否分享：0否 1是',
                                 `last_episode_key` varchar(64) COLLATE utf8mb3_unicode_ci DEFAULT NULL COMMENT '最近观看的 episode_key',
                                 `last_media_id` bigint DEFAULT NULL COMMENT '最近观看的媒体文件id',
                                 `last_read_time` datetime DEFAULT NULL COMMENT '最近观看时间',
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `ani_user_opus_uk_1` (`user_id`,`opus_id`),
                                 KEY `ani_user_opus_idx_2` (`user_id`,`last_read_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='用户作品关联表';


-- elysiananime.ani_media definition

CREATE TABLE `ani_media` (
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


-- elysiananime.ani_user_opus_progress definition

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


-- elysiananime.ani_opus_update definition

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


-- elysiananime.sys_dictionary definition

CREATE TABLE `sys_dictionary` (
                                  `id` bigint NOT NULL COMMENT '字典id',
                                  `parent_id` bigint DEFAULT NULL COMMENT '父级id',
                                  `dictionary_name` varchar(30) COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '字典名称',
                                  `remark` varchar(255) COLLATE utf8mb3_unicode_ci DEFAULT NULL COMMENT '备注',
                                  `sort` int NOT NULL COMMENT '排序号',
                                  `enable_status` tinyint DEFAULT '1' COMMENT '启用状态;0关闭 1启用',
                                  `create_by` bigint NOT NULL COMMENT '创建人',
                                  `create_time` datetime NOT NULL COMMENT '创建时间',
                                  `update_by` bigint DEFAULT NULL COMMENT '更新人',
                                  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
                                  `revision` int DEFAULT NULL COMMENT '乐观锁',
                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='系统字典表';


-- elysiananime.sys_log definition

CREATE TABLE `sys_log` (
                           `id` bigint NOT NULL COMMENT '日志编号',
                           `ip_address` varchar(128) COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '请求ip地址',
                           `operator` bigint DEFAULT NULL COMMENT '操作人员',
                           `request_way` varchar(10) COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '请求方式',
                           `log_status` tinyint DEFAULT NULL COMMENT '日志状态;0异常 1成功',
                           `log_type` tinyint DEFAULT NULL COMMENT '日志类型：1登录 2操作 ',
                           `api_path` varchar(256) COLLATE utf8mb3_unicode_ci DEFAULT NULL COMMENT '接口请求路径',
                           `create_time` datetime NOT NULL COMMENT '创建时间',
                           PRIMARY KEY (`id`),
                           KEY `sys_log_request_way_index` (`request_way`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='系统操作日志表';


-- elysiananime.sys_menu definition

CREATE TABLE `sys_menu` (
                            `id` bigint NOT NULL COMMENT '菜单id',
                            `menu_name` varchar(30) COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '菜单名称',
                            `permission_code` varchar(64) COLLATE utf8mb3_unicode_ci DEFAULT NULL COMMENT '权限编号',
                            `router_path` varchar(255) COLLATE utf8mb3_unicode_ci DEFAULT NULL COMMENT '路由地址',
                            `parent_id` bigint DEFAULT NULL COMMENT '父级id',
                            `menu_type` tinyint DEFAULT NULL COMMENT '按钮类型;0目录 1菜单 2按钮',
                            `is_menu` tinyint DEFAULT NULL COMMENT '是否菜单',
                            `menu_status` tinyint DEFAULT '0' COMMENT '菜单状态：0显示 1隐藏',
                            `component_path` varchar(64) COLLATE utf8mb3_unicode_ci DEFAULT NULL COMMENT '组件路径',
                            `is_external_link` tinyint DEFAULT '0' COMMENT '是否外链',
                            `icon_path` varchar(255) COLLATE utf8mb3_unicode_ci DEFAULT NULL COMMENT '菜单图标',
                            `sort` int DEFAULT NULL COMMENT '显示顺序',
                            `create_by` bigint NOT NULL COMMENT '创建人',
                            `create_time` datetime NOT NULL COMMENT '创建时间',
                            `update_by` bigint DEFAULT NULL COMMENT '更新人',
                            `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                            `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
                            `revision` int DEFAULT NULL COMMENT '乐观锁',
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='系统菜单表';


-- elysiananime.sys_notify definition

CREATE TABLE `sys_notify` (
                              `id` bigint NOT NULL COMMENT '通知ID',
                              `title` varchar(200) COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '通知标题',
                              `memo` varchar(900) COLLATE utf8mb3_unicode_ci DEFAULT NULL COMMENT '消息内容',
                              `jump_url` varchar(200) COLLATE utf8mb3_unicode_ci DEFAULT NULL COMMENT '跳转链接、ID、等等',
                              `notify_type` varchar(20) COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '通知类型',
                              `is_global` tinyint NOT NULL DEFAULT '0' COMMENT '是否全局',
                              `receiver` bigint DEFAULT NULL COMMENT '接收人',
                              `level` tinyint NOT NULL DEFAULT '1' COMMENT '通知等级',
                              `notify_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '通知时间',
                              `create_by` bigint DEFAULT NULL COMMENT '创建人',
                              `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
                              PRIMARY KEY (`id`),
                              KEY `sys_notify_idx1` (`notify_type`,`is_deleted`,`create_time`),
                              KEY `sys_notify_idx2` (`receiver`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_unicode_ci COMMENT='系统通知表';


-- elysiananime.sys_role definition

CREATE TABLE `sys_role` (
                            `id` bigint NOT NULL COMMENT '角色id',
                            `role_name` varchar(30) COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '角色名称',
                            `role_key` varchar(255) COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '角色标识',
                            `remark` varchar(255) COLLATE utf8mb3_unicode_ci DEFAULT NULL COMMENT '备注',
                            `sort` int DEFAULT NULL COMMENT '显示排序',
                            `create_by` bigint NOT NULL COMMENT '创建人',
                            `create_time` datetime NOT NULL COMMENT '创建时间',
                            `update_by` bigint DEFAULT NULL COMMENT '更新人',
                            `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                            `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
                            `revision` int DEFAULT NULL COMMENT '乐观锁',
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='系统角色表';


-- elysiananime.sys_role_menu definition

CREATE TABLE `sys_role_menu` (
                                 `id` bigint NOT NULL COMMENT '角色菜单关联id',
                                 `role_id` bigint NOT NULL COMMENT '角色id',
                                 `menu_id` bigint NOT NULL COMMENT '菜单id',
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='角色菜单关联表';


-- elysiananime.sys_theme definition

CREATE TABLE `sys_theme` (
                             `id` bigint NOT NULL COMMENT '系统主题id',
                             `user_id` bigint DEFAULT NULL COMMENT '用户id',
                             `layout_mode` tinyint NOT NULL DEFAULT '0' COMMENT '布局模式：0默认',
                             `primary_color` varchar(8) COLLATE utf8mb3_unicode_ci DEFAULT NULL COMMENT '主题颜色',
                             `color_2` varchar(8) COLLATE utf8mb3_unicode_ci DEFAULT NULL COMMENT '颜色2',
                             `color_3` varchar(8) COLLATE utf8mb3_unicode_ci DEFAULT NULL COMMENT '颜色3',
                             `color_4` varchar(8) COLLATE utf8mb3_unicode_ci DEFAULT NULL COMMENT '颜色4',
                             `is_dark` tinyint DEFAULT '0' COMMENT '是否暗黑模式',
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='系统主题表';


-- elysiananime.sys_user definition

CREATE TABLE `sys_user` (
                            `id` bigint NOT NULL COMMENT '用户id',
                            `username` varchar(30) COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '登录账号',
                            `nickname` varchar(30) COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '用户昵称',
                            `password` varchar(64) COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '密码',
                            `sex` tinyint NOT NULL COMMENT '用户性别;0未知 1男 2女',
                            `email` varchar(128) COLLATE utf8mb3_unicode_ci DEFAULT NULL COMMENT '用户邮箱',
                            `mobile_phone` varchar(20) COLLATE utf8mb3_unicode_ci DEFAULT NULL COMMENT '手机号',
                            `account_status` tinyint NOT NULL COMMENT '账号状态;0停用 1正常 2冻结 3封禁',
                            `avatar` varchar(255) COLLATE utf8mb3_unicode_ci DEFAULT NULL COMMENT '头像地址',
                            `last_login_ip` varchar(64) COLLATE utf8mb3_unicode_ci DEFAULT NULL COMMENT '最后登录ip',
                            `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
                            `create_by` bigint NOT NULL COMMENT '创建人',
                            `create_time` datetime NOT NULL COMMENT '创建时间',
                            `update_by` bigint DEFAULT NULL COMMENT '更新人',
                            `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                            `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
                            `revision` int DEFAULT NULL COMMENT '乐观锁',
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='系统用户表';


-- elysiananime.sys_user_role definition

CREATE TABLE `sys_user_role` (
                                 `id` bigint NOT NULL COMMENT '用户角色关联id',
                                 `user_id` bigint NOT NULL COMMENT '用户id',
                                 `role_id` bigint NOT NULL COMMENT '角色id',
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='用户角色关联表';

create table if not exists ani_user_feed
(
    id            bigint            not null comment '投喂 id'
    primary key,
    from_user_id  bigint            not null comment '投喂人 id',
    to_user_id    bigint            not null comment '被投喂人 id',
    opus_id       bigint            not null comment '作品 id',
    create_time   datetime          not null comment '投喂时间',
    is_deleted    tinyint default 0 not null comment '是否删除'
)
    comment '用户投喂关联表' collate = utf8_unicode_ci
    row_format = DYNAMIC;

create index ani_user_feed_idx_1
    on ani_user_feed (to_user_id, is_deleted);
