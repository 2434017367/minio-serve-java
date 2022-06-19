package com.example.minio.common.exception;

import com.example.minio.common.result.Result;
import com.example.minio.common.result.ResultCodeEnum;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletResponse;

/**
 * @email 2434017367@qq.com
 * @author: zhy
 * @date: 2020/3/12
 * @time: 9:50
 */
@Log4j2
@RestControllerAdvice
public class MyExceptionHandler {

    /**
     * 处理自定义异常
     */
    @ExceptionHandler(RRException.class)
    public Result handleRRException(RRException e, HttpServletResponse response){
        setResponseStatus(response);
        Result error = Result.error(ResultCodeEnum.ERROR_BUSINESS, e.getMsg());
        setExceptionInfo(error, e);
        return error;
    }

    /**
     * 异常处理
     * @param e
     * @param response
     * @return
     */
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e, HttpServletResponse response){
        setResponseStatus(response);
        Result error = Result.error(ResultCodeEnum.ERROR_SERVER);
        setExceptionInfo(error, e);
        return error;
    }

    private void setResponseStatus(HttpServletResponse response) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    private void setExceptionInfo(Result result, Throwable e){
        MyExceptionInfo.setExceptionInfo(result.getCode().getCode(), result.getMsg(), e);
    }

}
