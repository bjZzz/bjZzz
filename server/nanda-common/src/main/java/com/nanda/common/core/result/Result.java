package com.nanda.common.core.result;

import com.nanda.common.core.exception.ErrorCode;
import lombok.Data;
import org.slf4j.MDC;

import static com.nanda.common.core.constant.CommonConstants.MDC_REQUEST_ID;

@Data
public class Result<T> {

    private int code;
    private String message;
    private T data;
    private String requestId;

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<T>();
        r.setCode(ErrorCode.SUCCESS);
        r.setMessage("success");
        r.setData(data);
        r.setRequestId(MDC.get(MDC_REQUEST_ID));
        return r;
    }

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> fail(int code, String message) {
        Result<T> r = new Result<T>();
        r.setCode(code);
        r.setMessage(message);
        r.setRequestId(MDC.get(MDC_REQUEST_ID));
        return r;
    }
}
