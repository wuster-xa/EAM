package com.eam.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果封装类
 * 
 * 设计理念：
 * 所有后端接口统一返回此结构体，便于前端统一处理响应。
 * 前端可根据 code 判断请求是否成功，msg 获取提示信息，data 获取业务数据。
 * 
 * 使用示例：
 * - 成功：Result.success(data)
 * - 失败：Result.error("操作失败")
 * 
 * @param <T> 响应数据类型
 * @author 毕业设计项目组
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应状态码
     * 200 - 成功
     * 400 - 请求参数错误
     * 401 - 未登录或Token失效
     * 403 - 无权限访问
     * 500 - 服务器内部错误
     */
    private Integer code;

    /**
     * 响应提示信息
     * 成功时返回 "操作成功"
     * 失败时返回具体错误原因
     */
    private String msg;

    /**
     * 响应业务数据
     * 泛型类型，可承载任意类型的数据
     */
    private T data;

    /**
     * 时间戳，用于前端请求追踪
     */
    private Long timestamp;

    /**
     * 默认构造函数，初始化时间戳
     */
    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功响应（无数据）
     * 
     * @param <T> 数据类型
     * @return 成功结果，code=200，msg="操作成功"
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("操作成功");
        return result;
    }

    /**
     * 成功响应（带数据）
     * 
     * @param data 返回的业务数据
     * @param <T> 数据类型
     * @return 成功结果，code=200，msg="操作成功"，data=传入的数据
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    /**
     * 成功响应（带自定义消息）
     * 
     * @param msg 自定义成功消息
     * @param data 返回的业务数据
     * @param <T> 数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success(String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    /**
     * 失败响应（默认错误）
     * 
     * @param <T> 数据类型
     * @return 失败结果，code=500，msg="操作失败"
     */
    public static <T> Result<T> error() {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMsg("操作失败");
        return result;
    }

    /**
     * 失败响应（带错误消息）
     * 
     * @param msg 错误提示信息
     * @param <T> 数据类型
     * @return 失败结果，code=500
     */
    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMsg(msg);
        return result;
    }

    /**
     * 失败响应（带状态码和错误消息）
     * 
     * @param code 错误状态码
     * @param msg 错误提示信息
     * @param <T> 数据类型
     * @return 失败结果
     */
    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    /**
     * 参数错误响应
     * 
     * @param msg 错误提示信息
     * @param <T> 数据类型
     * @return 失败结果，code=400
     */
    public static <T> Result<T> badRequest(String msg) {
        Result<T> result = new Result<>();
        result.setCode(400);
        result.setMsg(msg);
        return result;
    }

    /**
     * 未授权响应（未登录或Token失效）
     * 
     * @param <T> 数据类型
     * @return 失败结果，code=401
     */
    public static <T> Result<T> unauthorized() {
        Result<T> result = new Result<>();
        result.setCode(401);
        result.setMsg("未登录或Token已失效，请重新登录");
        return result;
    }

    /**
     * 无权限响应
     * 
     * @param <T> 数据类型
     * @return 失败结果，code=403
     */
    public static <T> Result<T> forbidden() {
        Result<T> result = new Result<>();
        result.setCode(403);
        result.setMsg("无权限访问该资源");
        return result;
    }
}
