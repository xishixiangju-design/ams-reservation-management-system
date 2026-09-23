package com.trae.ams.common.exception;

import com.trae.ams.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;
import com.trae.ams.dto.technician.TechnicianConflictDTO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 负责统一处理 Controller 层抛出的各类异常，并返回标准 JSON 格式
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理技师日程冲突异常
     */
    @ExceptionHandler(TechnicianScheduleConflictException.class)
    public Result<List<TechnicianConflictDTO>> handleTechnicianScheduleConflictException(TechnicianScheduleConflictException e) {
        log.warn("技师日程冲突: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage(), e.getConflictList());
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.error("Business Exception: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数绑定校验异常（@Valid 注解校验失败时触发）
     * 
     * @param e 校验异常
     * @return 包含校验错误信息的标准响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        // 获取第一个字段的校验错误信息
        String message = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        log.error("Validation Exception: {}", message);
        return Result.error(400, message);
    }

    /**
     * 处理参数绑定异常（表单提交时数据类型不匹配触发）
     * 
     * @param e 绑定异常
     * @return 包含绑定错误信息的标准响应
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        // 获取第一个字段的绑定错误信息
        String message = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        log.error("Bind Exception: {}", message);
        return Result.error(400, message);
    }

    /**
     * 处理所有未捕获的普通异常
     * 作为兜底异常处理器，捕获所有未被上述方法处理的Exception
     * 
     * @param e 未捕获的异常
     * @return 包含通用错误信息的标准响应
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("System Exception", e);
        e.printStackTrace(); // 将堆栈信息输出到标准错误流
        String msg = e.getMessage();
        if (msg == null) {
            msg = "未知错误"; // 异常无消息时使用默认提示
        }
        return Result.error(500, "系统内部错误: " + msg);
    }

    /**
     * 处理所有未捕获的严重错误（Throwable级别）
     * 捕获Error级别的严重问题，如OutOfMemoryError等
     * 
     * @param e 严重错误
     * @return 包含严重错误信息的标准响应
     */
    @ExceptionHandler(Throwable.class)
    public Result<Void> handleThrowable(Throwable e) {
        log.error("Fatal Error", e);
        e.printStackTrace();
        String msg = e.getMessage();
        if (msg == null) {
            msg = "未知错误";
        }
        return Result.error(500, "严重错误: " + msg);
    }
}
