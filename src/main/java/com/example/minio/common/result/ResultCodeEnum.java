package com.example.minio.common.result;

/**
 * 返回code枚举类
 */
public enum ResultCodeEnum {

    /**
     * 成功
     */
    SUCCESS(200, "成功"),
    /**
     * 运行时错误
     */
    ERROR_SERVER(500, "服务器内部错误"),
    /**
     * 自定义错误
     */
    ERROR_BUSINESS(501, "业务错误"),
    /**
     * 数据库数据重复
     */
    ERROR_DATA_REPEAT(502, "数据重复"),
    /**
     * 接口调用权限不足
     */
    ERROR_PERMISSION(503, "权限不足"),
    /**
     * 时间校验错误
     */
    ERROR_CHECK_DATE(507, "时间校验错误"),
    /**
     * token错误
     */
    ERROR_TOKEN(504, "token错误"),
    /**
     * 参数错误
     */
    ERROR_PARAMS(505, "参数错误"),
    /**
     *
     */
    ERROR_RESULT(506, "结果错误");

    private final Integer code;

    private final String msg;

    ResultCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public static ResultCodeEnum getByCode(Integer code){
        ResultCodeEnum[] values = values();
        for (ResultCodeEnum value : values) {
            if (value.getCode().equals(code)){
                return value;
            }
        }
        return null;
    }
}
