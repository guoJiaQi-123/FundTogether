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

/**
 * 接口限流切面。
 * <p>
 * 拦截所有标注了 {@link RateLimit} 注解的方法，基于 Redis 的 INCR 命令实现计数器式限流。
 * 核心思路：在时间窗口内对请求进行计数，首次请求时设置过期时间，超过允许次数则抛出业务异常。
 * </p>
 *
 * @see RateLimit
 */
@Aspect
@Component
public class RateLimitAspect {

    /** Redis 操作模板，用于执行限流计数器的自增与过期设置 */
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 环绕通知：拦截标注了 {@link RateLimit} 注解的方法，执行限流逻辑。
     * <p>
     * 限流流程：
     * <ol>
     *   <li>从方法上获取 {@link RateLimit} 注解配置</li>
     *   <li>构建 Redis 限流键</li>
     *   <li>对计数器执行自增操作</li>
     *   <li>首次请求时设置键的过期时间（即限流窗口）</li>
     *   <li>若计数超过允许次数，抛出 429 异常</li>
     *   <li>未超限则放行，执行目标方法</li>
     * </ol>
     * </p>
     *
     * @param joinPoint 切点连接点，包含目标方法信息
     * @return 目标方法的执行结果
     * @throws Throwable 目标方法抛出的异常，或限流时抛出的 {@link BusinessException}
     */
    @Around("@annotation(com.fundtogether.common.annotation.RateLimit)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);

        // 构建 Redis 限流键
        String key = buildKey(rateLimit);

        // 对计数器执行自增操作，返回自增后的值
        long count = stringRedisTemplate.opsForValue().increment(key, 1);

        // 首次请求时设置过期时间，作为限流窗口
        if (count == 1) {
            stringRedisTemplate.expire(key, rateLimit.seconds(), TimeUnit.SECONDS);
        }

        // 超过允许次数，抛出限流异常
        if (count > rateLimit.permits()) {
            throw new BusinessException(429, "请求过于频繁，请稍后再试");
        }

        // 未超限，放行执行目标方法
        return joinPoint.proceed();
    }

    /**
     * 构建 Redis 限流键。
     * <p>
     * 构建规则：
     * <ul>
     *   <li>若注解中指定了自定义 key，则使用 {@code rate_limit:自定义key}</li>
     *   <li>若未指定自定义 key，则按以下逻辑生成：
     *     <ul>
     *       <li>已登录用户：{@code rate_limit:请求URI:用户ID}</li>
     *       <li>未登录用户：{@code rate_limit:请求URI:客户端IP}</li>
     *     </ul>
     *   </li>
     * </ul>
     * </p>
     *
     * @param rateLimit 限流注解实例
     * @return 构建好的 Redis 限流键
     */
    private String buildKey(RateLimit rateLimit) {
        StringBuilder sb = new StringBuilder("rate_limit:");

        if (!rateLimit.key().isEmpty()) {
            // 使用自定义限流键
            sb.append(rateLimit.key());
        } else {
            // 自动生成限流键：基于请求上下文
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                // 以请求 URI 作为基础
                sb.append(request.getRequestURI());

                // 尝试获取当前登录用户
                LoginUser loginUser = null;
                try {
                    loginUser = (LoginUser) request.getAttribute("loginUser");
                } catch (Exception ignored) {}

                if (loginUser != null) {
                    // 已登录：以用户 ID 区分
                    sb.append(":").append(loginUser.getId());
                } else {
                    // 未登录：以客户端 IP 地址区分
                    sb.append(":").append(request.getRemoteAddr());
                }
            }
        }
        return sb.toString();
    }
}
