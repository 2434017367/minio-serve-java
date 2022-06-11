package com.example.minio.common.enums;

/**
 * @author zhy
 * @email 2434017367@qq.com
 * @date 2022/6/4 18:28
 */
public enum FilePathEnum {

    /**
     * 文件存放地址
     */
    FILES("files", "文件存放地址"),
    /**
     * 临时文件存放地址
     */
    INTERIM("interim", "临时文件存放地址");

    /**
     * 路径
     */
    private final String path;
    /**
     * 描述
     */
    private final String desc;

    FilePathEnum(String path, String desc) {
        this.path = path;
        this.desc = desc;
    }

    public String getPath() {
        return path;
    }

    public String getDesc() {
        return desc;
    }
}
