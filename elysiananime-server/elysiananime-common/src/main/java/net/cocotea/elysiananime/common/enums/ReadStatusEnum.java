package net.cocotea.elysiananime.common.enums;

import lombok.Getter;

/**
 * 观看状态：0未看 1已看 2在看
 * @author cocotea
 * @since 2023-4-22 16:49:12
 */
@Getter
public enum ReadStatusEnum {
    NOT_READ(0, "未看", "未看"),
    IS_READ(1, "已看", "已看"),
    READING(2, "在看", "正在观看")
    ;

    final Integer code;
    final String desc;
    final String aliseName;

    ReadStatusEnum(Integer code, String desc, String aliseName) {
        this.code = code;
        this.desc = desc;
        this.aliseName = aliseName;
    }

}
