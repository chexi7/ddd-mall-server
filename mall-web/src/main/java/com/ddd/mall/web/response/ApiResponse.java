package com.ddd.mall.web.response;

import lombok.Getter;

/**
 * 统一 API 响应结构
 *
 * @param <T> 响应数据类型
 */
@Getter
public class ApiResponse<T> {
    /**
     * 请求是否成功
     */
    private final boolean success;

    /**
     * 响应消息
     */
    private final String message;

    /**
     * 响应数据
     */
    private final T data;

    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "success", data);
    }

    public static <T> ApiResponse<T> ok() {
        return new ApiResponse<>(true, "success", null);
    }

    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
