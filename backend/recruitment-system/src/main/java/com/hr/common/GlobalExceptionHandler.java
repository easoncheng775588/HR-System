package com.hr.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 统一处理所有异常，确保 API 响应格式一致
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 处理自定义业务异常
     * @param e 业务异常
     * @return 统一响应格式
     */
    @ExceptionHandler(BusinessException.class)
    public Response<?> handleBusinessException(BusinessException e) {
        logger.error("Business Exception: {}", e.getMessage(), e);
        return Response.fail(e.getErrorCode(), e.getMessage());
    }
    
    /**
     * 处理其他所有异常
     * @param e 异常
     * @return 统一响应格式
     */
    @ExceptionHandler(Exception.class)
    public Response<?> handleException(Exception e) {
        logger.error("System Exception: {}", e.getMessage(), e);
        return Response.fail("ERR9999", "系统内部错误，请稍后重试");
    }
}
