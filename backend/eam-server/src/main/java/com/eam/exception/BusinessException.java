package com.eam.exception;

import lombok.Getter;

/**
 * 业务异常类
 * 
 * 设计理念：
 * 用于业务逻辑中抛出的可预期异常，由全局异常处理器统一捕获处理。
 * 与系统异常（如 NullPointerException）区分，便于前端展示友好的错误提示。
 * 
 * 使用示例：
 * throw new BusinessException("资产不存在");
 * throw new BusinessException(400, "参数不能为空");
 * 
 * @author 毕业设计项目组
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误状态码
     * 默认为 500（服务器内部错误）
     */
    private final Integer code;

    /**
     * 错误提示信息
     */
    private final String message;

    /**
     * 构造函数 - 默认错误码 500
     * 
     * @param message 错误提示信息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
        this.message = message;
    }

    /**
     * 构造函数 - 自定义错误码
     * 
     * @param code 错误状态码
     * @param message 错误提示信息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 快速创建业务异常（默认错误码 500）
     * 
     * @param message 错误提示信息
     * @return BusinessException 实例
     */
    public static BusinessException of(String message) {
        return new BusinessException(message);
    }

    /**
     * 快速创建业务异常（自定义错误码）
     * 
     * @param code 错误状态码
     * @param message 错误提示信息
     * @return BusinessException 实例
     */
    public static BusinessException of(Integer code, String message) {
        return new BusinessException(code, message);
    }
}
