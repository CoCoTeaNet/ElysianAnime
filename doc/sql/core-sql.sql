create table ani_opus
(
    id              bigint            not null comment '作品id'
        primary key,
    name_original   varchar(100)      null comment '原名',
    name_cn         varchar(100)      null comment '中文名',
    cover_url       varchar(200)      null comment '封面地址',
    detail_info_url varchar(200)      null comment '详细链接',
    episodes        varchar(100)      null comment '话数',
    launch_start    varchar(100)      null comment '放送开始',
    delivery_week   varchar(100)      null comment '放送星期',
    ani_summary     text              null comment '作品简介',
    has_resource    tinyint default 0 null comment '是否有资源：0否 1是',
    rss_url         varchar(900)      null comment 'rss订阅链接',
    rss_status      tinyint default 0 null comment '订阅状态: 0未订阅 1订阅中 2订阅完成',
    rss_level_index int     default 0 null comment '资源标题集数出现的位置（数字匹配）',
    rss_file_type   varchar(10)       null comment '要订阅的资源格式',
    rss_only_mark   varchar(50)       null comment '匹配的唯一标识',
    rss_exclude_res varchar(50)       null comment '排除的资源',
    create_time     datetime          not null comment '创建时间',
    create_by       bigint            null comment '创建人',
    update_time     datetime          not null comment '更新时间',
    update_by       bigint            null comment '更新人',
    is_deleted      tinyint default 0 not null comment '是否删除'
)
    comment '作品主表'
    collate = utf8_unicode_ci
    row_format = DYNAMIC;

create index ani_opus_idx_1
    on ani_opus (name_cn, name_original);

create index ani_opus_idx_2
    on ani_opus (launch_start, delivery_week);

create table ani_opus_group
(
    id           bigint            not null comment '作品分组id'
        primary key,
    group_name   varchar(100)      not null comment '组名称',
    group_remark varchar(200)      null comment '组备注',
    create_by    bigint            not null comment '创建人',
    create_time  datetime          not null comment '创建时间',
    update_by    bigint            not null comment '更新人',
    update_time  datetime          not null comment '更新时间',
    is_deleted   tinyint default 0 not null comment '是否删除'
)
    comment '作品分组表'
    collate = utf8_unicode_ci
    row_format = DYNAMIC;

create table ani_opus_tag
(
    id          bigint   not null comment '作品标签关联id'
        primary key,
    tag_id      bigint   not null comment '标签id',
    opus_id     bigint   not null comment '作品id',
    create_time datetime null comment '关联时间'
)
    comment '作品标签关联表'
    collate = utf8_unicode_ci
    row_format = DYNAMIC;

create table ani_tag
(
    id          bigint            not null comment '标签id'
        primary key,
    tag_name    varchar(100)      not null comment '标签名称',
    create_time datetime          not null comment '创建时间',
    create_by   bigint            not null comment '创建人',
    update_time datetime          null comment '更新时间',
    update_by   bigint            null comment '更新人',
    is_deleted  tinyint default 0 not null comment '是否删除'
)
    comment 'acg标签表'
    collate = utf8_unicode_ci
    row_format = DYNAMIC;

create table ani_user_opus
(
    id           bigint            not null comment '用户作品关联id'
        primary key,
    user_id      bigint            not null comment '用户id',
    opus_id      bigint            not null comment '作品id',
    resource_url varchar(200)      null comment '资源地址',
    reading_num  int     default 0 not null comment '正在的播放集数',
    reading_time bigint  default 0 not null comment '正在播放的时长',
    read_status  tinyint default 0 not null comment '观看状态：0未看 1已看 2在看',
    create_time  datetime          not null comment '关联时间',
    is_share     tinyint default 0 not null comment '是否分享：0否 1是'
)
    comment '用户作品关联表'
    collate = utf8_unicode_ci
    row_format = DYNAMIC;

create index ani_user_opus_idx_1
    on ani_user_opus (user_id, opus_id);

create table sche_job
(
    id              bigint            not null comment '计划任务id'
        primary key,
    name            varchar(90)       not null comment '任务名称',
    type            tinyint default 0 not null comment '配置类型;0类模式 1函数模式',
    class_name      varchar(500)      null comment '任务对应类名',
    method_name     varchar(200)      null comment '方法名',
    parameters      varchar(900)      null comment '参数json对象',
    corn_expression varchar(64)       not null comment 'cron表达式',
    description     varchar(900)      null comment '任务描述',
    active          tinyint default 0 not null comment '是否启用;0未启用 1启用',
    next_exe_time   datetime          null comment '下一次执行时间',
    sort            int               null comment '排序',
    create_time     datetime          not null comment '创建时间',
    create_by       bigint            not null comment '创建人',
    update_time     datetime          null comment '更新时间',
    update_by       bigint            null comment '更新人',
    is_deleted      tinyint default 0 not null comment '是否删除',
    revision        int               null comment '乐观锁'
)
    comment '计划任务表'
    collate = utf8_unicode_ci
    row_format = DYNAMIC;

create table sche_job_log
(
    id                bigint            not null comment '计划任务执行日志id'
        primary key,
    job_id            bigint            not null comment '计划任务id',
    trigger_time      datetime          null comment '任务触发时间',
    trigger_by        bigint            null comment '任务触发人',
    exe_result        tinyint           null comment '任务执行结果;0失败 1成功',
    spend_time_millis bigint            null comment '任务执行耗时ms',
    finish_time       datetime          null comment '任务完成时间',
    sort              int               null comment '排序',
    create_time       datetime          null comment '创建时间',
    create_by         bigint            null comment '创建人',
    update_time       datetime          null comment '更新时间',
    update_by         bigint            null comment '更新人',
    is_deleted        tinyint default 0 not null comment '是否删除',
    revision          int               null comment '乐观锁'
)
    comment '计划任务执行日志表'
    collate = utf8_unicode_ci
    row_format = DYNAMIC;

create table sys_dictionary
(
    id              bigint            not null comment '字典id'
        primary key,
    parent_id       bigint            null comment '父级id',
    dictionary_name varchar(30)       not null comment '字典名称',
    remark          varchar(255)      null comment '备注',
    sort            int               not null comment '排序号',
    enable_status   tinyint default 1 null comment '启用状态;0关闭 1启用',
    create_by       bigint            not null comment '创建人',
    create_time     datetime          not null comment '创建时间',
    update_by       bigint            null comment '更新人',
    update_time     datetime          null comment '更新时间',
    is_deleted      tinyint default 0 not null comment '是否删除',
    revision        int               null comment '乐观锁'
)
    comment '系统字典表'
    collate = utf8_unicode_ci
    row_format = DYNAMIC;

create table sys_file
(
    id          bigint            not null comment '文件id'
        primary key,
    file_name   varchar(200)      not null comment '文件名称',
    file_suffix varchar(20)       null comment '文件后缀',
    real_path   varchar(500)      not null comment '文件真实路径',
    file_size   bigint  default 0 not null comment '文件大小（单位：字节）',
    create_by   bigint            null comment '创建人',
    create_time datetime          not null comment '创建时间',
    update_by   bigint            null comment '更新人',
    update_time datetime          not null comment '更新时间',
    is_share    tinyint default 0 null comment '是否共享',
    is_deleted  tinyint default 0 not null comment '是否删除'
)
    comment '系统文件表'
    collate = utf8_unicode_ci
    row_format = DYNAMIC;

create table sys_log
(
    id          bigint       not null comment '日志编号'
        primary key,
    ip_address  varchar(128) not null comment '请求ip地址',
    operator    bigint       null comment '操作人员',
    request_way varchar(10)  not null comment '请求方式',
    log_status  tinyint      null comment '日志状态;0异常 1成功',
    log_type    tinyint      null comment '日志类型：1登录 2操作 ',
    create_time datetime     not null comment '创建时间'
)
    comment '系统操作日志表'
    collate = utf8_unicode_ci
    row_format = DYNAMIC;

create index sys_log_request_way_index
    on sys_log (request_way);

create table sys_menu
(
    id               bigint            not null comment '菜单id'
        primary key,
    menu_name        varchar(30)       not null comment '菜单名称',
    permission_code  varchar(64)       null comment '权限编号',
    router_path      varchar(255)      null comment '路由地址',
    parent_id        bigint            null comment '父级id',
    menu_type        tinyint           null comment '按钮类型;0目录 1菜单 2按钮',
    is_menu          tinyint           null comment '是否菜单',
    menu_status      tinyint default 0 null comment '菜单状态：0显示 1隐藏',
    component_path   varchar(64)       null comment '组件路径',
    is_external_link tinyint default 0 null comment '是否外链',
    icon_path        varchar(255)      null comment '菜单图标',
    sort             int               null comment '显示顺序',
    create_by        bigint            not null comment '创建人',
    create_time      datetime          not null comment '创建时间',
    update_by        bigint            null comment '更新人',
    update_time      datetime          null comment '更新时间',
    is_deleted       tinyint default 0 not null comment '是否删除',
    revision         int               null comment '乐观锁'
)
    comment '系统菜单表'
    collate = utf8_unicode_ci
    row_format = DYNAMIC;

create table sys_notify
(
    id          bigint            not null comment '通知ID'
        primary key,
    title       varchar(200)      not null comment '通知标题',
    memo        varchar(900)      null comment '消息内容',
    jump_url    varchar(200)      null comment '跳转链接、ID、等等',
    notify_type varchar(20)       not null comment '通知类型',
    is_global   tinyint default 0 not null comment '是否全局',
    receiver    bigint            null comment '接收人',
    level       tinyint default 1 not null comment '通知等级',
    notify_time timestamp         not null comment '通知时间',
    create_by   bigint            null comment '创建人',
    create_time timestamp         not null comment '创建时间',
    is_deleted  tinyint default 0 not null comment '是否删除'
)
    comment '系统通知表' collate = utf8_unicode_ci;

create index sys_notify_idx1
    on sys_notify (notify_type, is_deleted, create_time);

create index sys_notify_idx2
    on sys_notify (receiver);

create table sys_role
(
    id          bigint            not null comment '角色id'
        primary key,
    role_name   varchar(30)       not null comment '角色名称',
    role_key    varchar(255)      not null comment '角色标识',
    remark      varchar(255)      null comment '备注',
    sort        int               null comment '显示排序',
    create_by   bigint            not null comment '创建人',
    create_time datetime          not null comment '创建时间',
    update_by   bigint            null comment '更新人',
    update_time datetime          null comment '更新时间',
    is_deleted  tinyint default 0 not null comment '是否删除',
    revision    int               null comment '乐观锁'
)
    comment '系统角色表'
    collate = utf8_unicode_ci
    row_format = DYNAMIC;

create table sys_role_menu
(
    id      bigint not null comment '角色菜单关联id'
        primary key,
    role_id bigint not null comment '角色id',
    menu_id bigint not null comment '菜单id'
)
    comment '角色菜单关联表'
    collate = utf8_unicode_ci
    row_format = DYNAMIC;

create table sys_theme
(
    id            bigint            not null comment '系统主题id'
        primary key,
    user_id       bigint            null comment '用户id',
    layout_mode   tinyint default 0 not null comment '布局模式：0默认',
    primary_color varchar(8)        null comment '主题颜色',
    color_2       varchar(8)        null comment '颜色2',
    color_3       varchar(8)        null comment '颜色3',
    color_4       varchar(8)        null comment '颜色4',
    is_dark       tinyint default 0 null comment '是否暗黑模式'
)
    comment '系统主题表'
    collate = utf8_unicode_ci
    row_format = DYNAMIC;

create table sys_user
(
    id              bigint            not null comment '用户id'
        primary key,
    username        varchar(30)       not null comment '登录账号',
    nickname        varchar(30)       not null comment '用户昵称',
    password        varchar(64)       not null comment '密码',
    sex             tinyint           not null comment '用户性别;0未知 1男 2女',
    email           varchar(128)      null comment '用户邮箱',
    mobile_phone    varchar(20)       null comment '手机号',
    account_status  tinyint           not null comment '账号状态;0停用 1正常 2冻结 3封禁',
    avatar          varchar(255)      null comment '头像地址',
    last_login_ip   varchar(64)       null comment '最后登录ip',
    last_login_time datetime          null comment '最后登录时间',
    create_by       bigint            not null comment '创建人',
    create_time     datetime          not null comment '创建时间',
    update_by       bigint            null comment '更新人',
    update_time     datetime          null comment '更新时间',
    is_deleted      tinyint default 0 not null comment '是否删除',
    revision        int               null comment '乐观锁'
)
    comment '系统用户表'
    collate = utf8_unicode_ci
    row_format = DYNAMIC;

create table sys_user_role
(
    id      bigint not null comment '用户角色关联id'
        primary key,
    user_id bigint not null comment '用户id',
    role_id bigint not null comment '角色id'
)
    comment '用户角色关联表'
    collate = utf8_unicode_ci
    row_format = DYNAMIC;

create table sys_version
(
    id            bigint       not null comment '版本id'
        primary key,
    update_no     varchar(20)  null comment '更新版本号',
    update_desc   varchar(900) null comment '版本更新描述',
    platform_name varchar(10)  null comment '系统平台名称',
    download_url  varchar(200) null comment '下载地址',
    create_by     bigint       not null comment '创建人',
    create_time   datetime     not null comment '创建时间',
    update_by     bigint       null comment '更新人',
    update_time   datetime     null comment '更新时间'
)
    comment '系统版本更新记录表'
    collate = utf8_unicode_ci
    row_format = DYNAMIC;

