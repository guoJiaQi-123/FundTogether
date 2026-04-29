package com.fundtogether.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis分布式锁工具类
 * <p>
 * 基于Redis实现分布式锁机制，用于在分布式环境下保证共享资源的互斥访问。
 * 提供加锁（tryLock）和解锁（unlock）两个核心操作：
 * <ul>
 *   <li>加锁：使用Redis的SETNX命令（setIfAbsent），确保只有一个客户端能成功获取锁</li>
 *   <li>解锁：使用Lua脚本保证"判断锁持有者+删除锁"的原子性，避免误删其他客户端的锁</li>
 * </ul>
 * </p>
 */
@Slf4j
@Component
public class RedisLockUtil {

    /** Redis字符串操作模板，用于执行Redis命令 */
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 尝试获取分布式锁
     * <p>
     * 使用Redis的SETNX命令实现：仅当key不存在时设置value，同时设置过期时间防止死锁。
     * 该操作是原子性的，保证在并发场景下只有一个客户端能成功加锁。
     * </p>
     *
     * @param key          锁的Redis键，通常以业务名称命名，如"lock:order:123"
     * @param value        锁的值，建议使用唯一标识（如UUID），用于解锁时验证锁的持有者
     * @param expireSeconds 锁的过期时间（秒），防止持有锁的客户端宕机导致死锁
     * @return true表示加锁成功，false表示锁已被其他客户端持有
     */
    public boolean tryLock(String key, String value, long expireSeconds) {
        // setIfAbsent等效于SETNX命令，同时设置过期时间，保证原子性
        Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(key, value, expireSeconds, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(result);
    }

    /**
     * 释放分布式锁
     * <p>
     * 使用Lua脚本保证"判断锁值+删除锁"两步操作的原子性。
     * 只有当Redis中key对应的value与传入的value一致时（即当前客户端持有该锁），
     * 才会删除该key释放锁，避免误删其他客户端持有的锁。
     * </p>
     * <p>
     * Lua脚本逻辑：GET key判断值是否匹配，匹配则DEL key返回1，否则返回0
     * </p>
     *
     * @param key   锁的Redis键
     * @param value 锁的值，用于验证当前客户端是否为锁的持有者
     * @return true表示解锁成功（锁被删除），false表示解锁失败（锁不属于当前客户端或锁已过期）
     */
    public boolean unlock(String key, String value) {
        // Lua脚本：先获取key的值，若与期望值一致则删除key，否则返回0
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long result = stringRedisTemplate.execute(
                (org.springframework.data.redis.core.RedisCallback<Long>) connection -> {
                    // 执行Lua脚本，传入1个KEY和1个ARGV参数
                    Object eval = connection.eval(
                            script.getBytes(),
                            org.springframework.data.redis.connection.ReturnType.INTEGER,
                            1, // KEYS参数数量
                            key.getBytes(), // KEYS[1]
                            value.getBytes() // ARGV[1]
                    );
                    return (Long) eval;
                }
        );
        // 返回值为1表示锁被成功删除
        return Long.valueOf(1L).equals(result);
    }
}
