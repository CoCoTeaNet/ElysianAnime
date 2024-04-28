package net.cocotea.janime.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 常用枚举之是否枚举
 *
 * @author CoCoTea
 * @version 2.0.0
 */
@Getter
@AllArgsConstructor
public enum IsEnum {

    N(0, "否"),
    Y(1, "是");

    final Integer code;
    final String desc;

}
