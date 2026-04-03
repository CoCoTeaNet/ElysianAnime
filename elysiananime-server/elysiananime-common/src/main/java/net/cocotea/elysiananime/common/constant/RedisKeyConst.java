package net.cocotea.elysiananime.common.constant;

/**
 * redis缓存键
 *
 * @author CoCoTea
 */
public class RedisKeyConst {

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

    /**
     * RSS内容缓存，例子：RSS_RESULT_CACHE_https://rssUrl.xyz
     */
    public final static String RSS_RESULT_CACHE = "RSS_RESULT_CACHE_{}";

    /**
     * 用户信息缓存
     */
    public final static String CACHE_USERINFO = "CACHE_USERINFO_%s";

}
