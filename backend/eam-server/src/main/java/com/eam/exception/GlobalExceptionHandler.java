package com.eam.exception;

import com.eam.common.Result;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 
 * 设计理念：
 * 统一捕获处理所有异常，避免在 Controller 层使用 try-catch。
 * 将异常转换为统一的 Result 格式返回给前端，便于前端统一处理。
 * 
 * 处理的异常类型：
 * 1. BusinessException - 业务异常（可预期的业务错误）
 * 2. BindException/MethodArgumentNotValidException - 参数校验异常
 * 3. ConstraintViolationException - 约束违规异常
 * 4. AccessDeniedException - 权限不足异常
 * 5. BadCredentialsException - 认证失败异常
 * 6. Exception - 其他未知异常
 * 
 * @author 毕业设计项目组
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     * 
     * 业务逻辑中主动抛出的异常，返回异常中携带的错误码和消息
     * 
     * @param e 业务异常
     * @return 统一响应结果
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常（@Valid 注解校验失败）
     * 
     * 当使用 @Valid 注解校验请求体参数时，校验失败会抛出此异常
     * 提取所有字段错误信息，拼接后返回
     * 
     * @param e 参数校验异常
     * @return 统一响应结果
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        // 提取所有字段错误信息
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", message);
        return Result.badRequest(message);
    }

    /**
     * 处理参数绑定异常
     * 
     * 当请求参数绑定到对象失败时抛出此异常
     * 例如：字符串无法转换为数字类型
     * 
     * @param e 绑定异常
     * @return 统一响应结果
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e) {
        // 提取所有字段错误信息
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数绑定失败: {}", message);
        return Result.badRequest(message);
    }

    /**
     * 处理约束违规异常
     * 
     * 当使用 @Validated 注解校验方法参数时，校验失败会抛出此异常
     * 
     * @param e 约束违规异常
     * @return 统一响应结果
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        // 提取所有约束违规信息
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.warn("约束违规: {}", message);
        return Result.badRequest(message);
    }

    /**
     * 处理参数类型不匹配异常
     * 
     * 当请求参数类型转换失败时抛出此异常
     * 例如：字符串 "abc" 无法转换为 Long 类型
     * 
     * @param e 参数类型不匹配异常
     * @return 统一响应结果
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String message = String.format("参数 '%s' 类型错误，期望类型: %s", 
                e.getName(), 
                e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知");
        log.warn("参数类型不匹配: {}", message);
        return Result.badRequest(message);
    }

    /**
     * 处理权限不足异常
     * 
     * 当用户访问无权限的资源时，Spring Security 会抛出此异常
     * 返回 403 状态码，提示无权限访问
     * 
     * @param e 权限不足异常
     * @return 统一响应结果
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        return Result.forbidden();
    }

    /**
     * 处理认证失败异常
     * 
     * 当用户名或密码错误时，Spring Security 会抛出此异常
     * 返回 401 状态码，提示认证失败
     * 
     * @param e 认证失败异常
     * @return 统一响应结果
     */
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleBadCredentialsException(BadCredentialsException e) {
        log.warn("认证失败: {}", e.getMessage());
        return Result.error(401, "用户名或密码错误");
    }

    /**
     * 处理 404 异常
     * 
     * 当请求的接口不存在时抛出此异常
     * 
     * @param e 404 异常
     * @return 统一响应结果
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("接口不存在: {}", e.getRequestURL());
        return Result.error(404, "请求的接口不存在");
    }

    /**
     * 处理其他未知异常
     * 
     * 捕获所有未被上述方法处理的异常
     * 记录详细的错误日志，返回通用的服务器错误提示
     * 
     * @param e 异常
     * @return 统一响应结果
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        // 记录详细的错误日志，包含堆栈信息
        log.error("系统异常: ", e);
        return Result.error("系统繁忙，请稍后重试");
    }
}
