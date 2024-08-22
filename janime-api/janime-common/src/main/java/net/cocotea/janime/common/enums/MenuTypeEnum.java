package net.cocotea.janime.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 按钮类型;0目录 1菜单 2按钮
 *
 * @author CoCoTea
 * @version 2.0.0
 */
@Getter
@AllArgsConstructor
public enum MenuTypeEnum {
    DIRECTORY(0, "目录"),
    MENU(1, "菜单"),
    BUTTON(2, "按钮");

    final Integer code;
    final String desc;
}
