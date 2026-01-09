package io.github.johntortoise.exceptions;

import io.github.johntortoise.core.dto.sys.TortoiseBaseResult;
import io.github.johntortoise.core.exceptions.TortoiseBusinessException;
import io.github.johntortoise.core.utils.LogUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private TortoiseBaseResult<Void> buildErrorResponse(int code, String message) {
        return TortoiseBaseResult.fail(code,message);
    }

    
    @ExceptionHandler(Exception.class)
    public TortoiseBaseResult<Void> handleException(Exception e) {
        log.error("执行异常:",e);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),e.getMessage());
    }

    
    @ExceptionHandler(TortoiseBusinessException.class)
    public TortoiseBaseResult<Void> handleBusinessException(TortoiseBusinessException e) {
        log.error("执行异常:",e);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),e.getErrorDetail());
    }


    @ExceptionHandler({JwtException.class, ExpiredJwtException.class, MalformedJwtException.class, SignatureException.class})
    public TortoiseBaseResult<Void> handleJwtException(JwtException e) {
        LogUtil.warn("JWT exception: {}", e.getMessage());
        String message = "Token无效或已过期";
        if (e instanceof ExpiredJwtException) {
            message = "Token已过期";
        } else if (e instanceof MalformedJwtException) {
            message = "Token格式错误";
        } else if (e instanceof SignatureException) {
            message = "Token签名验证失败";
        }
        return buildErrorResponse(HttpStatus.UNAUTHORIZED.value(),message);

    }


    
    @ExceptionHandler(IllegalArgumentException.class)
    public TortoiseBaseResult<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST.value(),e.getMessage());
    }
}