package com.hr.common;

import java.io.Serializable;

/**
 * 统一响应格式类
 * 标准化 API 响应结构
 */
public class Response<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 响应码
    private String returnCode;
    
    // 错误信息
    private String errorMsg;
    
    // 响应数据
    private T body;
    
    /**
     * 构造方法
     * @param returnCode 响应码
     * @param errorMsg 错误信息
     * @param body 响应数据
     */
    public Response(String returnCode, String errorMsg, T body) {
        this.returnCode = returnCode;
        this.errorMsg = errorMsg;
        this.body = body;
    }
    
    /**
     * 成功响应
     * @param body 响应数据
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> Response<T> success(T body) {
        return new Response<>("SUC0000", "", body);
    }
    
    /**
     * 失败响应
     * @param errorMsg 错误信息
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> Response<T> fail(String errorMsg) {
        return new Response<>("ERR0000", errorMsg, null);
    }
    
    /**
     * 失败响应（带响应码）
     * @param returnCode 响应码
     * @param errorMsg 错误信息
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> Response<T> fail(String returnCode, String errorMsg) {
        return new Response<>(returnCode, errorMsg, null);
    }
    
    // getter 和 setter 方法
    public String getReturnCode() {
        return returnCode;
    }
    
    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }
    
    public String getErrorMsg() {
        return errorMsg;
    }
    
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
    
    public T getBody() {
        return body;
    }
    
    public void setBody(T body) {
        this.body = body;
    }
}
