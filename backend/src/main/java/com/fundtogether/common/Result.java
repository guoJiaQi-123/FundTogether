package com.fundtogether.common;

import lombok.Data;

/**
 * 统一响应结果封装类。
 * <p>
 * 对所有接口的返回值进行统一格式封装，包含状态码、消息和数据三部分，
 * 便于前端统一处理响应逻辑。支持泛型，可承载任意类型的数据。
 * </p>
 *
 * @param <T> 响应数据的类型
 */
@Data
public class Result<T> {

    /** 响应状态码，200 表示成功，其他表示各类错误 */
    private Integer code;

    /** 响应消息，成功时为 "success"，失败时为错误描述 */
    private String message;

    /** 响应数据，成功时携带业务数据，失败时通常为 null */
    private T data;

    /**
     * 构造成功响应（无数据）。
     *
     * @param <T> 响应数据的类型
     * @return 状态码为 200、消息为 "success" 的空数据响应
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        return result;
    }

    /**
     * 构造成功响应（携带数据）。
     *
     * @param data 响应中携带的业务数据
     * @param <T>  响应数据的类型
     * @return 状态码为 200、消息为 "success"、包含业务数据的响应
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    /**
     * 构造失败响应（默认错误码 500）。
     *
     * @param message 错误描述信息
     * @param <T>     响应数据的类型
     * @return 状态码为 500、包含错误描述的响应
     */
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    /**
     * 构造失败响应（自定义错误码）。
     *
     * @param code    自定义错误码（如 429、403 等）
     * @param message 错误描述信息
     * @param <T>     响应数据的类型
     * @return 包含指定错误码和错误描述的响应
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
