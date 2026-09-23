package com.trae.ams.common.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一API响应结果封装类
 * 
 * 功能说明：
 * 封装所有API接口的返回数据，提供统一的响应格式。
 * 前端可通过code判断请求是否成功，通过message获取提示信息，通过data获取业务数据。
 * 
 * 响应格式示例：
 * 成功：{"code": 200, "message": "success", "data": {...}}
 * 失败：{"code": 500, "message": "错误描述", "data": null}
 * 
 * @param <T> 响应数据的泛型类型
 */
@Data
public class Result<T> implements Serializable {

    /** 响应状态码（200成功，400参数错误，409冲突，500服务器错误等） */
    private Integer code;
    /** 响应提示信息 */
    private String message;
    /** 响应业务数据 */
    private T data;

    /**
     * 构造成功响应（无数据）
     * @return 状态码200的成功响应
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        return result;
    }

    /**
     * 构造成功响应（带数据）
     * @param data 响应业务数据
     * @return 状态码200的成功响应
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    /**
     * 构造错误响应（指定错误码和消息）
     * @param code 错误状态码
     * @param message 错误描述信息
     * @return 错误响应
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    /**
     * 构造错误响应（指定错误码、消息和数据）
     * 用于需要返回部分数据的错误场景，如技师冲突时返回冲突列表
     * @param code 错误状态码
     * @param message 错误描述信息
     * @param data 附加数据
     * @return 错误响应
     */
    public static <T> Result<T> error(Integer code, String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 构造错误响应（默认500错误码）
     * @param message 错误描述信息
     * @return 状态码500的错误响应
     */
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }
}
