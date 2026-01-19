ALTER TABLE elysiananime.ani_user_opus ADD total_time bigint(20) NULL COMMENT '视频总时长';
ALTER TABLE elysiananime.ani_user_opus CHANGE total_time total_time bigint(20) NULL COMMENT '视频总时长' AFTER reading_time;

ALTER TABLE elysiananime.sys_log ADD api_path varchar(256) NULL COMMENT '接口请求路径';
ALTER TABLE elysiananime.sys_log CHANGE api_path api_path varchar(256) NULL COMMENT '接口请求路径' AFTER log_type;

ALTER TABLE elysiananime.ani_opus ADD score FLOAT NULL COMMENT '评分';
ALTER TABLE elysiananime.ani_opus CHANGE score score FLOAT NULL COMMENT '评分' AFTER episodes;
