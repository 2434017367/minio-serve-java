package com.example.minio.common.exception;

/**
 * @email 2434017367@qq.com
 * @author: zhy
 * @date: 2021/12/18
 * @time: 14:40
 */
public class MyExceptionInfo {

    private static final ThreadLocal<MyExceptionInfo> exceptionThreadLocal = new ThreadLocal<>();

    private Integer code;

    private String msg;

    private Throwable e;

    private MyExceptionInfo(Integer code, String msg, Throwable e){
        this.code = code;
        this.msg = msg;
        this.e = e;
    }

    public static void setExceptionInfo(Integer code, String msg, Throwable e) {
        MyExceptionInfo myExceptionInfo = new MyExceptionInfo(code, msg, e);
        exceptionThreadLocal.set(myExceptionInfo);
    }

    public static MyExceptionInfo getExceptionInfo(){
        MyExceptionInfo myExceptionInfo = exceptionThreadLocal.get();
        exceptionThreadLocal.remove();
        return myExceptionInfo;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public Throwable getE() {
        return e;
    }
}
