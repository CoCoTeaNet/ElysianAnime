/**
 * api状态返回值
 */
enum ApiResultEnum {
    /**
     * 成功
     */
    SUCCESS = 200,
    /**
     * 内部错误
     */
    ERROR = 500,
    /**
     * 未找到资源
     */
    NOT_FOUNT = 404,
    /**
     * 拒绝请求
     */
    REFUSE = 401,
    /**
     * 未登录
     */
    NOT_LOGIN = 4001,
    /**
     * 无权限访问
     */
    NOT_PERMISSION = 4002,
    /**
     * token失效
     */
    TOKEN_INVALID = 4003
}

export {ApiResultEnum};