package net.cocotea.janime.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 是否启用;0未启用 1启用
 *
 * @author CoCoTea
 * @version v2.0.0
 */
@Getter
@AllArgsConstructor
public enum ActiveEnum {
    CLOSE(0, "未启用"),
    ACTIVE(1, "启用");

    final Integer code;
    final String desc;
}
