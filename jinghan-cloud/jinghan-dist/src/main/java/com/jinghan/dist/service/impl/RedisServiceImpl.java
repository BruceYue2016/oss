package com.jinghan.dist.service.impl;

import com.jinghan.dist.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author Bruce
 * @date 2018/6/26
 */
@Service
public class RedisServiceImpl<K, V> implements RedisService<K, V> {

    public static final String PREFIX_USER = "user:";

    public static final String PREFIX_STORE = "store:";

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 通过id查询，如果查询到则进行缓存
     * @param id 实体类id
     * @return 查询到的实现类
     */
    @SuppressWarnings("unchecked")
    @Override
    public V getByKey(String prefix, Integer id) {
        // 查询缓存
        String key = prefix + id;
        if (redisTemplate.hasKey(key)) {
            return (V) redisTemplate.opsForValue().get(key);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setByKey(String prefix, Integer id, V value) {
        String key = prefix + id;
        redisTemplate.opsForValue().set(key, value, 600, TimeUnit.SECONDS);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void delByKey(String prefix, Integer id) {
        String key = prefix + id;
        if (redisTemplate.hasKey(key)) {
            redisTemplate.delete(key);
        }
    }
}
