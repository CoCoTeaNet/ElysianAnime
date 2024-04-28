package net.cocotea.janime.common.constant;

/**
 * redis缓存键
 *
 * @author CoCoTea
 */
public class RedisKeyConst {
    /**
     * 验证码键：第一个参数为验证码类型（必填），第二个选填
     */
    public final static String VERIFY_CODE = "%s_CODE__%s";

    /**
     * 在线用户，参数为用户id
     */
    public final static String ONLINE_USER = "ONLINE_USER_%s";

    /**
     * 用户缓存权限
     */
    public final static String USER_PERMISSION = "USER_PERMISSION_%s";

    /**
     * qb 认证缓存
     */
    public final static String QB_COOKIE = "QB_COOKIE";

    /**
     * 通知消息 NOTIFY_SET_类型_接收人
     */
    public final static String NOTIFY_SET = "NOTIFY_SET_%s_%s";

    /**
     * rss工作状态
     */
    public final static String RSS_WORKS_STATUS = "RSS_WORKS_STATUS";
}
