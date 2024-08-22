package net.cocotea.janime.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 操作类型枚举值
 *
 * @author CoCoTea
 * @version v1.0.0
 */
@Getter
@AllArgsConstructor
public enum LogTypeEnum {

    LOGIN(1, "登录日志"),
    OPERATION(2, "操作日志");

    final Integer code;
    final String desc;
}
