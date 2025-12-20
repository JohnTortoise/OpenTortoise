package io.github.johntortoise.core.exceptions;

import io.github.johntortoise.core.enums.ErrorCodeEnum;
import lombok.Getter;


@Getter
public class TortoiseBusinessException extends RuntimeException {


    private final String errorCode;


    private final String errorMessage;


    private final String errorDetail;


    public TortoiseBusinessException(ErrorCodeEnum errorCodeEnum) {
        super(errorCodeEnum.getMessage());
        this.errorCode = errorCodeEnum.getCode();
        this.errorMessage = errorCodeEnum.getMessage();
        this.errorDetail = null;
    }

    public TortoiseBusinessException(ErrorCodeEnum errorCodeEnum, String errorDetail) {
        super(errorCodeEnum.getMessage() + (errorDetail != null ? ": " + errorDetail : ""));
        this.errorCode = errorCodeEnum.getCode();
        this.errorMessage = errorCodeEnum.getMessage();
        this.errorDetail = errorDetail;
    }

    public TortoiseBusinessException(ErrorCodeEnum errorCodeEnum, Throwable cause) {
        super(errorCodeEnum.getMessage(), cause);
        this.errorCode = errorCodeEnum.getCode();
        this.errorMessage = errorCodeEnum.getMessage();
        this.errorDetail = null;
    }

    public TortoiseBusinessException(ErrorCodeEnum errorCodeEnum, String errorDetail, Throwable cause) {
        super(errorCodeEnum.getMessage() + (errorDetail != null ? ": " + errorDetail : ""), cause);
        this.errorCode = errorCodeEnum.getCode();
        this.errorMessage = errorCodeEnum.getMessage();
        this.errorDetail = errorDetail;
    }


    public TortoiseBusinessException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.errorDetail = null;
    }

    public TortoiseBusinessException(String errorCode, String errorMessage, String errorDetail) {
        super(errorMessage + (errorDetail != null ? ": " + errorDetail : ""));
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.errorDetail = errorDetail;
    }

    @Override
    public String toString() {
        return String.format("TortoiseBusinessException{errorCode='%s', errorMessage='%s', errorDetail='%s'}",
                errorCode, errorMessage, errorDetail);
    }
}