package com.jinghan.dist.service;

/**
 * @author Bruce
 * @date 2018/6/26
 */
public interface RedisService<K, V> {

    V getByKey(String prefix, Integer id);

    void setByKey(String prefix, Integer id, V value);

    void delByKey(String prefix, Integer id);
}

