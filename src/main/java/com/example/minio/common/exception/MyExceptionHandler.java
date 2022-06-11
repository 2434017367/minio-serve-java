package com.example.minio.common.exception;

import com.example.minio.common.result.Result;
import com.example.minio.common.result.ResultCodeEnum;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
    public Result handleRRException(RRException e){
        Result error = Result.error(ResultCodeEnum.ERROR_BUSINESS, e.getMsg());
        setExceptionInfo(error, e);
        return error;
    }

    /**
     * 数据库唯一索引错误
     * @param e
     * @return
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result handleDuplicateKeyException(DuplicateKeyException e) {
        Result error = Result.error(ResultCodeEnum.ERROR_BUSINESS, "sql重复键异常");
        setExceptionInfo(error, e);
        return error;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result handleHttpRequestMethodNotSupportedException(Exception e) {
        Result error = Result.error(ResultCodeEnum.ERROR_BUSINESS, "请求方法类型错误");
        setExceptionInfo(error, e);
        return error;
    }

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e){
        Result error = Result.error(ResultCodeEnum.ERROR_SERVER);
        setExceptionInfo(error, e);
        return error;
    }

    private void setExceptionInfo(Result result, Throwable e){
        log.error(e);
        MyExceptionInfo.setExceptionInfo(result.getCode().getCode(), result.getMsg(), e);
    }

}
