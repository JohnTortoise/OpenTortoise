package io.github.johntortoise.core.enums;

import lombok.Getter;


@Getter
public enum ErrorCodeEnum {


    INIT_CLIENT_ERROR("INIT_CLIENT_ERROR", "客户端初始化失败"),


    PARAMS_EMPTY_ERROR("PARAMS_EMPTY_ERROR", "参数不能为空"),
    PARAMS_INVALID_ERROR("PARAMS_INVALID_ERROR", "参数格式无效"),
    PARAMS_VALIDATE_ERROR("PARAMS_VALIDATE_ERROR", "参数校验失败"),


    MODEL_NOT_FOUND("MODEL_NOT_FOUND", "模型不存在"),
    DATA_NOT_FOUND("DATA_NOT_FOUND", "数据不存在"),
    DATA_ACCESS_ERROR("DATA_ACCESS_ERROR", "数据访问异常"),


    INVOKE_LLM_ERROR("INVOKE_LLM_ERROR", "调用LLM失败"),
    LLM_SERVICE_UNAVAILABLE("LLM_SERVICE_UNAVAILABLE", "大语言模型服务不可用"),
    LLM_TIMEOUT_ERROR("LLM_TIMEOUT_ERROR", "大语言模型服务响应超时"),


    PARSE_RESULT_ERROR("PARSE_RESULT_ERROR", "解析结果失败"),
    RESULT_FORMAT_ERROR("RESULT_FORMAT_ERROR", "结果格式错误"),


    SERVICE_ERROR("SERVICE_ERROR", "服务内部错误"),
    BUSINESS_ERROR("BUSINESS_ERROR", "业务逻辑错误"),
    OPERATION_NOT_ALLOWED("OPERATION_NOT_ALLOWED", "操作不允许"),


    SYSTEM_ERROR("SYSTEM_ERROR", "系统内部错误"),
    UNKNOWN_ERROR("UNKNOWN_ERROR", "未知错误"),
    NETWORK_ERROR("NETWORK_ERROR", "网络连接错误"),


    UNAUTHORIZED_ERROR("UNAUTHORIZED_ERROR", "未授权访问"),
    FORBIDDEN_ERROR("FORBIDDEN_ERROR", "禁止访问"),
    AUTHENTICATION_FAILED("AUTHENTICATION_FAILED", "认证失败");


    private final String code;


    private final String message;

    ErrorCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }


    public static ErrorCodeEnum getByCode(String code) {
        for (ErrorCodeEnum errorCode : values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode;
            }
        }
        return UNKNOWN_ERROR;
    }


    public static ErrorCodeEnum getByCode(String code, ErrorCodeEnum defaultError) {
        for (ErrorCodeEnum errorCode : values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode;
            }
        }
        return defaultError;
    }


    public static boolean contains(String code) {
        for (ErrorCodeEnum errorCode : values()) {
            if (errorCode.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public String toString() {
        return String.format("ErrorCodeEnum{code='%s', message='%s'}", code, message);
    }


    public String getFullMessage() {
        return String.format("[%s] %s", code, message);
    }
}