package net.cocotea.janime.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 操作状态 枚举类
 *
 * @author CoCoTea
 * @version 2.0.0
 */
@Getter
@AllArgsConstructor
public enum LogStatusEnum {
    /**
     * 0异常 1成功
     */
    ERROR(0, "异常"),
    SUCCESS(1, "成功");

    final Integer code;
    final String desc;
}
