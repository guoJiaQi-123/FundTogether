package com.fundtogether.common.aspect;

import com.fundtogether.common.annotation.RateLimit;
import com.fundtogether.common.exception.BusinessException;
import com.fundtogether.security.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class RateLimitAspect {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Around("@annotation(com.fundtogether.common.annotation.RateLimit)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);

        String key = buildKey(rateLimit);
        long count = stringRedisTemplate.opsForValue().increment(key, 1);
        if (count == 1) {
            stringRedisTemplate.expire(key, rateLimit.seconds(), TimeUnit.SECONDS);
        }
        if (count > rateLimit.permits()) {
            throw new BusinessException(429, "请求过于频繁，请稍后再试");
        }

        return joinPoint.proceed();
    }

    private String buildKey(RateLimit rateLimit) {
        StringBuilder sb = new StringBuilder("rate_limit:");
        if (!rateLimit.key().isEmpty()) {
            sb.append(rateLimit.key());
        } else {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                sb.append(request.getRequestURI());
                LoginUser loginUser = null;
                try {
                    loginUser = (LoginUser) request.getAttribute("loginUser");
                } catch (Exception ignored) {}
                if (loginUser != null) {
                    sb.append(":").append(loginUser.getId());
                } else {
                    sb.append(":").append(request.getRemoteAddr());
                }
            }
        }
        return sb.toString();
    }
}
