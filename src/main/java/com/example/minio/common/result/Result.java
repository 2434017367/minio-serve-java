package com.example.minio.common.result;

import cn.hutool.core.util.StrUtil;

import java.util.HashMap;

/**
 * 返回数据
 */
public class Result extends HashMap<String, Object> {

    private static final String CODE = "code";

    private static final String MSG = "msg";

    private static final String DATA = "data";

    private Result(){

    }

    private Result(ResultCodeEnum codeEnum){
        int code = codeEnum.getCode();
        String msg = codeEnum.getMsg();
        this.put(CODE, code);
        this.put(MSG, msg);
    }

    private Result(int code, String msg){
        this.put(CODE, code);
        this.put(MSG, msg);
    }

    public static Result error(){
        return new Result(ResultCodeEnum.ERROR_BUSINESS);
    }

    public static Result error(ResultCodeEnum codeEnum, String msg){
        int code = codeEnum.getCode();
        String msg1 = codeEnum.getMsg();
        if (StrUtil.isEmpty(msg)){
            msg = msg1;
        }
        Result result = new Result(code, msg);
        return result;
    }

    public static Result error(String msg){
        return error(ResultCodeEnum.ERROR_BUSINESS, msg);
    }

    public static Result error(ResultCodeEnum resultCodeEnum){
        return new Result(resultCodeEnum);
    }

    public static Result ok(Object data){
        Result result = new Result(ResultCodeEnum.SUCCESS);
        result.setData(data);
        return result;
    }

    public static Result ok(String msg, Object data) {
        Result result = new Result(ResultCodeEnum.SUCCESS.getCode(), msg);
        result.setData(data);
        return result;
    }

    public static Result ok() {
        return new Result(ResultCodeEnum.SUCCESS);
    }

    public void setData(Object data){
        this.put(DATA, data);
    }

    @Override
    public Result put(String key, Object value) {
        super.put(key, value);
        return this;
    }

    public ResultCodeEnum getCode(){
        int code = Integer.parseInt(String.valueOf(this.get(CODE)));
        return ResultCodeEnum.getByCode(code);
    }

    public Result setMsg(String msg) {
        this.put(MSG, msg);
        return this;
    }

    public String getMsg(){
        return (String) this.get(MSG);
    }

    public Object getObj(){
        return this.get(DATA);
    }


}
