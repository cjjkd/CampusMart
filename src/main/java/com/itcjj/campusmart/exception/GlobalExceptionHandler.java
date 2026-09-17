package com.itcjj.campusmart.exception;

import com.itcjj.campusmart.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 业务异常：属于预期内的分支（如「用户名已存在」），用 warn，不算错误
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBiz(BizException e, HttpServletRequest request) {
        log.warn("业务异常 [{}] -> code={}, msg={}",
                request.getRequestURI(), e.getCodeEnum().getCode(), e.getCodeEnum().getMsg());
        return Result.error(e.getCodeEnum().getCode(), e.getCodeEnum().getMsg());
    }

    // 参数校验失败：用户传错了东西，warn 足够
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String msg = e.getBindingResult().getFieldError().getDefaultMessage();
        log.warn("参数校验失败 [{}] -> {}", request.getRequestURI(), msg);
        return Result.error(400, msg);
    }

    // 兜底：真正的意外，用 error + 完整堆栈
    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception e, HttpServletRequest request) {
        log.error("系统异常 [{}]", request.getRequestURI(), e);
        return Result.error(500, "服务器开小差了");
    }
}
