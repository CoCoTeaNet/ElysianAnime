package net.cocotea.elysiananime.common.service;

import java.util.Map;
import java.util.Set;

/**
 * redis包装服务
 *
 * @author CoCoTea
 * @version 2.0
 */
public interface RedisService {
    /**
     * 保存方法，时间为秒
     *
     * @param key     k
     * @param value   v
     * @param seconds 单位秒
     */
    void save(String key, String value, Long seconds);

    /**
     * 保存方法，时间为小时
     *
     * @param key   k
     * @param value v
     * @param hours 单位小时
     */
    void saveByHours(String key, String value, int hours);

    /**
     * 保存方法，时间为分钟
     *
     * @param key     k
     * @param value   v
     * @param minutes 单位分钟
     */
    void saveByMinutes(String key, String value, int minutes);

    /**
     * 保存方法，时效为永久
     *
     * @param key   k
     * @param value v
     */
    void save(String key, String value);

    /**
     * 删除
     *
     * @param key k
     */
    void delete(String key);

    /**
     * 获取值
     *
     * @param key k
     * @return 值
     */
    String get(String key);

    /**
     * 哈希
     *
     * @param key     集合key
     * @param hashKey 元素key
     * @param value   元素值
     */
    void hashPut(String key, String hashKey, String value);

    /**
     * 哈希
     *
     * @param key     集合key
     * @param hashKey 元素key
     * @param value   元素值
     */
    void hashPut(String key, String hashKey, Object value);

    /**
     * 哈希集合 根据hashKey取value
     *
     * @param key     集合key
     * @param hashKey 元素key
     * @return 储存值
     */
    Object hashGet(String key, String hashKey);

    /**
     * 哈希集合 根据hashKey取value
     *
     * @param key     集合key
     * @param hashKey 元素key
     * @return 储存值
     */
    Map<Object, Object> hashGetEntries(String key);

    /**
     * 哈希集合 删除某几个值
     *
     * @param key      集合key
     * @param hashKeys 元素key
     */
    void hashRemove(String key, Object... hashKeys);

    /**
     * 匹配键
     *
     * @param pattern 正则表达式
     * @return 键集合
     */
    Set<String> keys(String pattern);

    void set(String redisKey, String s);

}
