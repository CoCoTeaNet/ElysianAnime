package net.cocotea.janime.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 回复类型;0文章 1用户
 *
 * @author CoCoTea
 * @version 2.0.0
 */
@Getter
@AllArgsConstructor
public enum ReplyTypeEnum {
    ARTICLE(0, "文章"),

    USER(1, "用户");

    final Integer code;
    final String desc;
}
