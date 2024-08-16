package net.cocotea.janime.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 0停用 1正常 2冻结 3封禁
 *
 * @author CoCoTea
 * @version 2.0.0
 */
@Getter
@AllArgsConstructor
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
}
