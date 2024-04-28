package net.cocotea.janime.common.enums;

import lombok.Getter;

/**
 * @author CoCoTea
 * @date 2022-1-12 16:30:04
 * @version v2.0.8
 */
@Getter
public enum ApiResultEnum {
    /**
     * 成功
     */
    SUCCESS(200, "请求成功"),
    /**
     * 方法不正确
     */
    METHOD_ERROR(403, "请求方法不正确"),
    /**
     * 内部错误
     */
    ERROR(500, "系统异常，请联系管理员"),
    /**
     * 未找到资源
     */
    NOT_FOUNT(404, "未找到资源"),
    /**
     * 拒绝请求
     */
    REFUSE(401, "拒绝请求"),
    /**
     * 未登录
     */
    NOT_LOGIN(4001, "未登录"),
    /**
     * 无权限访问
     */
    NOT_PERMISSION(4002, "无权限访问"),
    /**
     * token失效
     */
    TOKEN_INVALID(4003, "TOKEN失效")
    ;
    final Integer code;
    final String desc;

    ApiResultEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
