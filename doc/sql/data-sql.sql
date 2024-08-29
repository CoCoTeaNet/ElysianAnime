insert into sys_user (id, username, nickname, password, sex, email, mobile_phone, account_status, avatar, last_login_ip, last_login_time, create_by, create_time, update_by, update_time, is_deleted, revision)
values  (2233, 'admin', 'admin', 'EDE36EFF1BC0B5C00BCD78F64A9AD643C594CF2A68C36C91AC67EB24B5BA12D9', 1, null, null, 1, null, '0:0:0:0:0:0:0:1', '2024-08-29 10:32:49', 2233, '2024-08-29 10:17:26', 2233, '2024-08-29 10:32:49', 0, null);


insert into sys_user_role (id, user_id, role_id)
values  (1122, 2233, 2211231231);


insert into sys_role (id, role_name, role_key, remark, sort, create_by, create_time, update_by, update_time, is_deleted, revision)
values  (2211231231, '超级管理员', 'role:super:admin', null, 1, 2233, '2024-08-29 10:21:31', 2233, '2024-08-29 10:21:34', 0, null),
        (1278670599241994240, '订阅者', 'bangumi:rss:subscriber', null, 1, 2233, '2024-08-29 11:00:27', 2233, '2024-08-29 11:00:27', 0, null);


insert into sys_role_menu (id, role_id, menu_id)
values  (1278670365086584832, 2211231231, 1278666070165426176),
        (1278670365086584833, 2211231231, 1278664659524521984),
        (1278670365086584834, 2211231231, 1278669765661822976),
        (1278670365086584835, 2211231231, 1278669471007772672),
        (1278670365086584836, 2211231231, 1278669198487064576),
        (1278670365086584837, 2211231231, 1278666636891394048),
        (1278670365086584838, 2211231231, 1278665169853878272),
        (1278670365086584839, 2211231231, 1278667378054270976),
        (1278670365086584840, 2211231231, 1278668202272755712),
        (1278670365086584841, 2211231231, 1278667087892320256),
        (1278670365086584842, 2211231231, 1278666780940570624),
        (1278670365086584843, 2211231231, 1278666452165857280),
        (1278670365086584844, 2211231231, 1278666313250508800),
        (1278670365086584845, 2211231231, 1111),
        (1278670365086584846, 2211231231, 1278668449803800576),
        (1278670365086584847, 2211231231, 1278670205073887232),
        (1278670655659577344, 1278670599241994240, 1278664659524521984),
        (1278670655659577345, 1278670599241994240, 1278668449803800576),
        (1278670655659577346, 1278670599241994240, 1278670205073887232),
        (1278670655659577347, 1278670599241994240, 1278669765661822976),
        (1278670655659577348, 1278670599241994240, 1278669471007772672),
        (1278670655659577349, 1278670599241994240, 1278669198487064576);


insert into sys_menu (id, menu_name, permission_code, router_path, parent_id, menu_type, is_menu, menu_status, component_path, is_external_link, icon_path, sort, create_by, create_time, update_by, update_time, is_deleted, revision)
values  (1111, '菜单管理', '', '/admin/sys-menu-manager', 1278666636891394048, 1, 1, 0, '', 0, 'Menu', 1, 2233, '2024-08-29 10:20:54', 2233, '2024-08-29 10:45:31', 0, null),
        (1278664659524521984, '个人中心', ':admin:sys-user-center', '/admin/sys-user-center', 0, 1, 1, 0, '', 0, 'AddLocation', 99, 2233, '2024-08-29 10:36:51', 2233, '2024-08-29 10:41:13', 0, null),
        (1278665169853878272, '角色管理', ':admin:sys-role-manager', '/admin/sys-role-manager', 1278666636891394048, 1, 1, 0, '', 0, 'UserFilled', 3, 2233, '2024-08-29 10:38:53', 2233, '2024-08-29 10:45:36', 0, null),
        (1278666070165426176, '系统首页', ':admin:home', '/admin/home', 0, 1, 1, 0, null, 0, 'HomeFilled', 100, 2233, '2024-08-29 10:42:28', 2233, '2024-08-29 10:42:28', 0, null),
        (1278666313250508800, '权限管理', ':admin:sys-permission-manager', '/admin/sys-permission-manager', 1278666636891394048, 1, 1, 0, '', 0, 'Operation', 1, 2233, '2024-08-29 10:43:26', 2233, '2024-08-29 10:45:27', 0, null),
        (1278666452165857280, '字典管理', ':admin:sys-dictionary-manager', '/admin/sys-dictionary-manager', 1278666636891394048, 1, 1, 0, '', 0, 'Notebook', 1, 2233, '2024-08-29 10:43:59', 2233, '2024-08-29 10:45:22', 0, null),
        (1278666636891394048, '系统管理', ':admin:system', '/admin/system', 0, 0, 1, 0, '', 0, 'Platform', 5, 2233, '2024-08-29 10:44:43', 2233, '2024-08-29 10:53:20', 0, null),
        (1278666780940570624, '操作日志', ':admin:sys-log-manager', '/admin/sys-log-manager', 1278666636891394048, 1, 1, 0, null, 0, 'Monitor', 1, 2233, '2024-08-29 10:45:17', 2233, '2024-08-29 10:45:17', 0, null),
        (1278667087892320256, '文件管理', ':admin:file', '/admin/file', 0, 0, 1, 0, '', 0, 'Folder', 1, 2233, '2024-08-29 10:46:30', 2233, '2024-08-29 10:49:36', 0, null),
        (1278667378054270976, '个人文件', ':admin:sys-file-manager', '/admin/sys-file-manager', 1278667087892320256, 1, 1, 0, '', 0, 'Files', 2, 2233, '2024-08-29 10:47:39', 2233, '2024-08-29 10:52:51', 0, null),
        (1278668202272755712, '文件回收站', ':admin:sys-recycle-manager', '/admin/sys-recycle-manager', 1278667087892320256, 1, 1, 0, null, 0, 'DeleteFilled', 1, 2233, '2024-08-29 10:50:56', 2233, '2024-08-29 10:50:56', 0, null),
        (1278668449803800576, '服务监控', ':admin:dashboard', '/admin/dashboard', 0, 1, 1, 0, '', 0, 'Monitor', 0, 2233, '2024-08-29 10:51:55', 2233, '2024-08-29 10:52:04', 0, null),
        (1278669198487064576, '番剧管理', ':admin:ani-opus-manager', '/admin/ani-opus-manager', 1278670205073887232, 1, 1, 0, '', 0, 'Apple', 97, 2233, '2024-08-29 10:54:53', 2233, '2024-08-29 10:59:03', 0, null),
        (1278669471007772672, '我的追番', ':admin:ani-user-opus-manager', '/admin/ani-user-opus-manager', 1278670205073887232, 1, 1, 0, '', 0, 'StarFilled', 98, 2233, '2024-08-29 10:55:58', 2233, '2024-08-29 10:59:09', 0, null),
        (1278669765661822976, '番剧首页', ':#:anime:home', '/#/anime/home', 1278670205073887232, 1, 1, 0, '', 0, 'House', 98, 2233, '2024-08-29 10:57:09', 2233, '2024-08-29 10:59:14', 0, null),
        (1278670205073887232, '番剧模块', ':system:ani', '/system/ani', 0, 0, 1, 0, null, 0, 'Apple', 7, 2233, '2024-08-29 10:58:53', 2233, '2024-08-29 10:58:53', 0, null);


