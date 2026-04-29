package com.fundtogether.config;

import com.fundtogether.common.Result;
import com.fundtogether.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

/**
 * 全局异常处理器
 * <p>
 * 基于 Spring 的 {@link RestControllerAdvice} 机制，统一拦截 Controller 层抛出的各类异常，
 * 将异常转换为标准化的 {@link Result} 响应格式返回给前端，避免将原始异常堆栈信息暴露给客户端。
 * 同时通过日志记录异常信息，便于问题排查和监控。
 * </p>
 * <p>
 * 异常处理优先级：按异常类型从具体到通用依次匹配，子类异常优先于父类异常。
 * </p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理请求参数校验异常（@Valid / @Validated 注解触发）
     * <p>
     * 当使用 JSR-303 校验注解（如 @NotBlank、@Size 等）校验请求体参数不通过时抛出，
     * 提取第一个校验错误信息返回给前端。
     * </p>
     *
     * @param e 方法参数校验异常
     * @return 包含校验错误信息的统一响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("参数校验异常: {}", message);
        return Result.error(400, message);
    }

    /**
     * 处理参数绑定异常
     * <p>
     * 当请求参数绑定到对象属性失败时抛出（如类型不匹配、格式错误等），
     * 提取第一个绑定错误信息返回给前端。
     * </p>
     *
     * @param e 参数绑定异常
     * @return 包含绑定错误信息的统一响应
     */
    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e) {
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("参数绑定异常: {}", message);
        return Result.error(400, message);
    }

    /**
     * 处理权限不足异常（Spring Security 抛出）
     * <p>
     * 当已认证用户访问其无权限的资源时，由 Spring Security 的访问控制机制抛出。
     * 返回 403 状态码，表示服务器理解请求但拒绝执行。
     * </p>
     *
     * @param e 访问拒绝异常
     * @return 包含权限不足提示的统一响应
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Result<?> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        return Result.error(403, "权限不足，拒绝访问");
    }

    /**
     * 处理认证失败异常（Spring Security 抛出）
     * <p>
     * 当用户未登录或登录凭证（JWT Token）无效/过期时，由 Spring Security 认证机制抛出。
     * 返回 401 状态码，提示前端需要重新登录。
     * </p>
     *
     * @param e 认证异常
     * @return 包含认证失败提示的统一响应
     */
    @ExceptionHandler(AuthenticationException.class)
    public Result<?> handleAuthenticationException(AuthenticationException e) {
        log.warn("认证失败: {}", e.getMessage());
        return Result.error(401, "未登录或登录已过期");
    }

    /**
     * 处理非法参数异常
     * <p>
     * 当业务代码中主动抛出 {@link IllegalArgumentException} 时触发，
     * 通常用于业务参数的合法性校验不通过的场景。
     * </p>
     *
     * @param e 非法参数异常
     * @return 包含参数错误信息的统一响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("业务参数异常: {}", e.getMessage());
        return Result.error(400, e.getMessage());
    }

    /**
     * 处理自定义业务异常
     * <p>
     * 当业务逻辑中主动抛出 {@link BusinessException} 时触发，
     * 使用异常对象中携带的错误码和错误信息构造响应，实现业务错误的精确返回。
     * </p>
     *
     * @param e 业务异常
     * @return 包含业务错误码和错误信息的统一响应
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理运行时异常（兜底）
     * <p>
     * 拦截所有未被上述处理器捕获的 {@link RuntimeException}，
     * 作为运行时异常的兜底处理，记录完整的异常堆栈以便排查问题。
     * </p>
     *
     * @param e 运行时异常
     * @return 包含通用错误提示的统一响应
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException e) {
        log.error("运行时业务异常:", e);
        return Result.error(500, e.getMessage() != null ? e.getMessage() : "系统业务异常");
    }

    /**
     * 处理所有未捕获的异常（最终兜底）
     * <p>
     * 拦截所有未被上述处理器捕获的异常（包括受检异常），
     * 作为全局异常处理的最后一道防线，返回通用的服务器错误提示，
     * 避免将原始异常信息暴露给前端。
     * </p>
     *
     * @param e 未捕获的异常
     * @return 包含通用服务器错误提示的统一响应
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("服务器内部错误:", e);
        return Result.error(500, "服务器内部错误，请联系管理员");
    }
}
