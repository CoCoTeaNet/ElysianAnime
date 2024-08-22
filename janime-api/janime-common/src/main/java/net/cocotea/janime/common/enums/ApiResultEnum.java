package net.cocotea.janime.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 返回数据模型状态枚举值
 *
 * @author CoCoTea
 * @version v2.0.0
 */
@Getter
@AllArgsConstructor
public enum ApiResultEnum {
    /**
     * 成功
     */
    SUCCESS(200, "请求成功"),
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
     * 请求方式不正确
     */
    REQUEST_ERROR(403, "请求方式不正确"),
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
    TOKEN_INVALID(4003, "TOKEN失效"),
    /**
     * 参数缺失异常
     */
    MISSING_REQUEST_PARAMETER(3001, "参数缺失异常")
    ;
    final Integer code;
    final String desc;

}
