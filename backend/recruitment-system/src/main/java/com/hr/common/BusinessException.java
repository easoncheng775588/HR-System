package com.hr.common;

/**
 * 自定义业务异常类
 * 用于规范业务逻辑异常处理
 */
public class BusinessException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    // 错误码
    private String errorCode;
    
    /**
     * 构造方法
     * @param message 错误信息
     */
    public BusinessException(String message) {
        super(message);
        this.errorCode = "ERR0000";
    }
    
    /**
     * 构造方法
     * @param errorCode 错误码
     * @param message 错误信息
     */
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * 构造方法
     * @param message 错误信息
     * @param cause 异常原因
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "ERR0000";
    }
    
    /**
     * 构造方法
     * @param errorCode 错误码
     * @param message 错误信息
     * @param cause 异常原因
     */
    public BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    // getter 方法
    public String getErrorCode() {
        return errorCode;
    }
    
    // setter 方法
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}
