package net.cocotea.janime.common.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import net.cocotea.janime.common.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author CoCoTea
 * @since v1
 */
@Service
public class RedisServiceImpl implements RedisService {

    private final static Logger logger = LoggerFactory.getLogger(RedisServiceImpl.class);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void save(String key, String value, Long seconds){
        String baseMsg = "service[save]";
        if (StrUtil.isBlank(key)) {
            logger.warn(baseMsg + "保存失败，原因是key值为空");
            return;
        }
        if (StrUtil.isBlank(value)) {
            logger.warn(baseMsg + "键[{}]保存失败，原因是值为空", key);
            return;
        }
        stringRedisTemplate.opsForValue().set(key, value, seconds, TimeUnit.SECONDS);
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
        String baseMsg = "service[save]";
        if (StrUtil.isBlank(key)) {
            logger.warn(baseMsg + "保存失败，原因是key值为空");
            return;
        }
        if (StrUtil.isBlank(value)) {
            logger.warn(baseMsg + "键[{}]保存失败，原因是值为空", key);
            return;
        }
        stringRedisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void delete(String key) {
        String baseMsg ="service[delete]";
        if (StrUtil.isBlank(key)) {
            logger.warn(baseMsg + "删除失败，原因是key值为空");
            return;
        }
        stringRedisTemplate.delete(key);
    }

    @Override
    public String get(String key) {
        if (key.trim().isEmpty()) {
            return key;
        } else {
            return stringRedisTemplate.opsForValue().get(key);
        }
    }

    @Override
    public void hashPut(String key, String hashKey, String value) {
        if ( StrUtil.isBlank(key) ) {
            logger.warn("service[hashPut]key is empty");
            return;
        }
        stringRedisTemplate.opsForHash().put(key, hashKey, value);
    }

    @Override
    public void hashPut(String key, String hashKey, Object value) {
        if ( StrUtil.isBlank(key) ) {
            logger.warn("service[hashPut]key is empty");
            return;
        }
        stringRedisTemplate.opsForHash().put(key, hashKey, JSONObject.toJSONString(value));
    }

    @Override
    public Object hashGet(String key, String hashKey) {
        if ( StrUtil.isBlank(key) ) {
            logger.warn("service[hashGet]key is empty");
            return null;
        }
        return stringRedisTemplate.opsForHash().get(key, hashKey);
    }

    @Override
    public Map<Object, Object> hashGetEntries(String key) {
        if ( StrUtil.isBlank(key) ) {
            logger.warn("service[hashGet]key is empty");
            return null;
        }
        return stringRedisTemplate.opsForHash().entries(key);
    }

    @Override
    public void hashRemove(String key, Object... hashKeys) {
        if ( StrUtil.isBlank(key) ) {
            logger.warn("service[hashRemove]key is empty");
        }
        stringRedisTemplate.opsForHash().delete(key, hashKeys);
    }

    @Override
    public Set<String> keys(String pattern) {
        return stringRedisTemplate.keys(pattern);
    }
}
