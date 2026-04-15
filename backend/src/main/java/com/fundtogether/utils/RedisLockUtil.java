package com.fundtogether.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedisLockUtil {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public boolean tryLock(String key, String value, long expireSeconds) {
        Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(key, value, expireSeconds, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(result);
    }

    public boolean unlock(String key, String value) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long result = stringRedisTemplate.execute(
                (org.springframework.data.redis.core.RedisCallback<Long>) connection -> {
                    Object eval = connection.eval(
                            script.getBytes(),
                            org.springframework.data.redis.connection.ReturnType.INTEGER,
                            1,
                            key.getBytes(),
                            value.getBytes()
                    );
                    return (Long) eval;
                }
        );
        return Long.valueOf(1L).equals(result);
    }
}
