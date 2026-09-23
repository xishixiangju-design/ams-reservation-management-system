package com.trae.ams.common.exception;

import lombok.Getter;

/**
 * 业务异常类
 * 
 * 功能说明：
 * 用于封装业务逻辑中发生的可预期异常，例如：
 * - 参数校验失败
 * - 业务规则不满足（如预约时间冲突、技师不可用等）
 * - 数据不存在或状态不允许操作
 * 
 * 该异常会被 GlobalExceptionHandler 统一捕获并转换为标准JSON响应
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 错误码，用于前端判断具体错误类型 */
    private final Integer code;

    /**
     * 构造方法：仅指定错误消息，默认错误码为500
     * 
     * @param message 错误描述信息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    /**
     * 构造方法：指定错误码和错误消息
     * 
     * @param code 错误码（如400表示参数错误，409表示冲突等）
     * @param message 错误描述信息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
