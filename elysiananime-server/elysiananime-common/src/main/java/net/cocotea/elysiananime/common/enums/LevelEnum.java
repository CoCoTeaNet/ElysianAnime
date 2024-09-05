package net.cocotea.elysiananime.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 等级信息枚举值
 *
 * @author CoCoTea
 * @version v2.0.0
 */
@Getter
@AllArgsConstructor
public enum LevelEnum {

    INFO(1, "信息"),
    WARN(2, "警告"),
    ERROR(3, "错误");

    final Integer code;
    final String desc;

}