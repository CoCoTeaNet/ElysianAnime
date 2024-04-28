package net.cocotea.janime.common.enums;

/**
 * 是否启用;0未启用 1启用
 * @date 2022-9-4 00:09:45
 * @author CoCoTea
 */
public enum ActiveEnum {
    CLOSE(0, "未启用"),
    ACTIVE(1, "启用");

    final Integer code;
    final String desc;

    ActiveEnum(Integer code, String desc) {
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
