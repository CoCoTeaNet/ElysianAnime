package net.cocotea.janime.common.enums;

/**
 * 0停用 1正常 2冻结 3封禁
 * @date 2022-1-12 16:30:04
 * @author jwss
 */
public enum AccountStatusEnum {
    /**
     * 停用
     */
    STOP_USING(0, "停用"),
    /**
     * 男
     */
    NORMAL(1, "正常"),
    /**
     * 冻结
     */
    FREEZE(2, "冻结"),
    /**
     * 封禁
     */
    BAN(3, "封禁");

    final Integer code;
    final String desc;

    AccountStatusEnum(Integer code, String desc) {
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
