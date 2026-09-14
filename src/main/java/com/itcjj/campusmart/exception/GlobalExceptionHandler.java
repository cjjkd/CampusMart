
package com.itcjj.campusmart.exception;

import com.itcjj.campusmart.common.Result;
import com.itcjj.campusmart.exception.BizException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result<Void> handleBiz(BizException e) {
        return Result.error(e.getCodeEnum().getCode(), e.getCodeEnum().getMsg());
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception e) {
        e.printStackTrace();
        return Result.error(500, "服务器开小差了");
    }
}
