package com.fundtogether.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口限流注解。
 * <p>
 * 标注在 Controller 方法上，配合 {@link com.fundtogether.common.aspect.RateLimitAspect} 切面，
 * 基于 Redis 实现滑动窗口式接口限流，防止接口被恶意频繁调用。
 * </p>
 *
 * @see com.fundtogether.common.aspect.RateLimitAspect
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * 在指定时间窗口内允许的最大请求次数，默认 5 次。
     *
     * @return 允许的请求次数
     */
    int permits() default 5;

    /**
     * 限流时间窗口长度（秒），默认 1 秒。
     *
     * @return 时间窗口秒数
     */
    int seconds() default 1;

    /**
     * 限流键的自定义前缀。
     * <p>
     * 若为空字符串，则自动根据请求 URI + 用户ID/IP 地址生成限流键；
     * 若指定了值，则直接使用该值作为限流键，适用于需要按业务维度限流的场景。
     * </p>
     *
     * @return 限流键，默认为空
     */
    String key() default "";
}
