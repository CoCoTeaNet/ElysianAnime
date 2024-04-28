package net.cocotea.janime.common.enums;

/**
 * 回复类型;0文章 1用户
 *
 * @author CoCoTea
 * @date 2022-8-21 04:07:45
 */
public enum ReplyTypeEnum {
    ARTICLE(0, "文章"),

    USER(1, "用户");

    final Integer code;
    final String desc;

    ReplyTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
