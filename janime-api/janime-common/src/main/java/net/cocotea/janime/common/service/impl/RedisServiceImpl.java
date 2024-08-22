package net.cocotea.janime.common.service.impl;

import net.cocotea.janime.common.service.RedisService;
import org.noear.redisx.RedisClient;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author CoCoTea
 * @since v1
 */
@Component
public class RedisServiceImpl implements RedisService {

    private static final Logger log = LoggerFactory.getLogger(RedisServiceImpl.class);

    @Inject
    private RedisClient redisClient;

    @Override
    public void save(String key, String value, Long seconds){
        redisClient.open(session -> session.key(key).expire(Math.toIntExact(seconds)).set(value));
    }

    @Override
    public void saveByHours(String key, String value, int hours){
        save(key, value, hours * 3600L);
    }

    @Override
    public void saveByMinutes(String key, String value, int minutes){
        save(key, value, minutes * 60L);
    }

    @Override
    public void save(String key, String value) {
        redisClient.open(session -> session.key(key).set(value));
    }

    @Override
    public void delete(String key) {
        redisClient.open(session -> session.deleteKeys(Collections.singleton(key)));
    }

    @Override
    public String get(String key) {
        return redisClient.openAndGet(session -> session.key(key).get());
    }

    @Override
    public void hashPut(String key, String hashKey, String value) {
        redisClient.open(session -> session.key(key).hashSet(hashKey, value));
    }

    @Override
    public void hashPut(String key, String hashKey, Object value) {

    }

    @Override
    public Object hashGet(String key, String hashKey) {
        return null;
    }

    @Override
    public Map<Object, Object> hashGetEntries(String key) {
        return Map.of();
    }

    @Override
    public void hashRemove(String key, Object... hashKeys) {

    }

    @Override
    public Set<String> keys(String pattern) {
        return redisClient.openAndGet(session -> session.keys(pattern));
    }

    @Override
    public void set(String key, String value) {
        redisClient.open(session -> session.key(key).expire(0).set(value));
    }
}
