package net.cocotea.elysiananime.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 菜单状态 枚举值
 *
 * @author CoCoTea
 * @version 2.0.0
 */
@Getter
@AllArgsConstructor
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
}
