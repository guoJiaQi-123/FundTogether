package com.fundtogether.common.exception;

import lombok.Getter;

/**
 * 业务异常类。
 * <p>
 * 用于封装业务逻辑中可预期的异常情况，携带错误码和错误信息，
 * 由全局异常处理器统一捕获并转换为标准响应格式返回给前端。
 * 与系统级异常（如空指针、IO异常）区分，便于分类处理。
 * </p>
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 业务错误码，用于前端根据不同错误码进行差异化处理 */
    private final int code;

    /**
     * 构造业务异常，使用默认错误码 400。
     *
     * @param message 错误描述信息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    /**
     * 构造业务异常，指定自定义错误码。
     *
     * @param code    业务错误码（如 429 表示限流、403 表示无权限等）
     * @param message 错误描述信息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
