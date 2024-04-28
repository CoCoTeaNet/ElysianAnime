package net.cocotea.janime.common.enums;

/**
 * 按钮类型;0目录 1菜单 2按钮
 *
 * @date 2022-1-12 16:30:04
 * @author CoCoTea
 */
public enum MenuTypeEnum {
    DIRECTORY(0, "目录"),
    MENU(1, "菜单"),
    BUTTON(2, "按钮");

    final Integer code;
    final String desc;

    MenuTypeEnum(Integer code, String desc) {
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
