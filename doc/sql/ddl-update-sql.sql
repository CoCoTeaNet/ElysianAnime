ALTER TABLE elysiananime.ani_user_opus ADD total_time bigint(20) NULL COMMENT '视频总时长';
ALTER TABLE elysiananime.ani_user_opus CHANGE total_time total_time bigint(20) NULL COMMENT '视频总时长' AFTER reading_time;
