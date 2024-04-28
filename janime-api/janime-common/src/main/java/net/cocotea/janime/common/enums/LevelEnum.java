package net.cocotea.janime.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LevelEnum {

    INFO(1, "信息"),
    WARN(2, "警告"),
    ERROR(3, "错误");

    final Integer code;
    final String desc;

}