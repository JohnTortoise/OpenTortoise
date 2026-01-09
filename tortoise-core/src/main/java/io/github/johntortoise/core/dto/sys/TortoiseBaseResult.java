package io.github.johntortoise.core.dto.sys;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 基础响应结果类
 *
 * @param <T> 数据泛型
 */
@Data
@Accessors(chain = true)
public class TortoiseBaseResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer code;
    private T data;
    private String msg;

    public static final int SUCCESS_CODE = 200;
    public static final int ERROR_CODE = 500;
    public static final String SUCCESS_MSG = "操作成功";
    public static final String ERROR_MSG = "操作失败";

    // 成功方法
    public static <T> TortoiseBaseResult<T> ok() {
        return ok(null);
    }

    public static <T> TortoiseBaseResult<T> ok(T data) {
        return new TortoiseBaseResult<T>()
                .setCode(SUCCESS_CODE)
                .setData(data)
                .setMsg(SUCCESS_MSG);
    }


    public static <T> TortoiseBaseResult<T> fail(String msg) {
        return new TortoiseBaseResult<T>()
                .setCode(ERROR_CODE)
                .setMsg(msg);
    }

    public static <T> TortoiseBaseResult<T> fail(Integer code, String msg) {
        return new TortoiseBaseResult<T>()
                .setCode(code)
                .setMsg(msg);
    }

}