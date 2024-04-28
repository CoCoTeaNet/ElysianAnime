package net.cocotea.janime.common.enums;

/**
 * 菜单状态 枚举值
 *
 * @author CoCoTea
 * @date 2022-4-2 14:16:05
 */
public enum MenuStatusEnum {
    /**
     * 显示&启用
     */
    SHOW(0, "显示"),
    /**
     * 隐藏&启用
     */
    HIDE(1, "隐藏");

    final Integer code;
    final String desc;

    MenuStatusEnum(Integer code, String desc) {
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
