package com.fusemi.dashboard.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        e.printStackTrace();
        System.err.println("GLOBAL EXCEPTION: " + e.getClass().getName() + " - " + e.getMessage());
        return Result.fail(500, "服务器内部错误");
    }
}
