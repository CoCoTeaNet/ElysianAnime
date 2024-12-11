package net.cocotea.elysiananime.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 启用状态;0关闭 1启用
 *
 * @since 2.2.3
 */
@Getter
@AllArgsConstructor
public enum EnableStatusEnum {

    OFF(0, "关闭"),
    ON(1, "启用");

    final Integer code;
    final String desc;

}
