package com.example.minio.common.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Create by zhy on 2022/3/3 11:33
 *
 * 文件类型枚举
 */
public enum FileTypeEnum {

    /**
     * 图片
     */
    IMAGE("img", "png", "jpeg", "jpg"),

    /**
     * word
     */
    WORD("word", "doc", "docx", "wps"),

    /**
     * pdf
     */
    PDF("pdf", "pdf"),

    /**
     * xls
     */
    XLS("xls","xlsx"),

    /**
     * 视频
     */
    video("video", "mp4");


    /**
     * 类型
     */
    private final String type;

    /**
     * 类型对应文件后缀
     */
    private final String[] suffixs;

    FileTypeEnum(String type, String... suffixs){
        this.type = type;
        this.suffixs = suffixs;
    }

    private static final List<String> fileTypes = getFileTypes();
    private static final List<String> getFileTypes() {
        FileTypeEnum[] typeEnums = values();
        List<String> stringList = Arrays.stream(typeEnums)
                .flatMap(fileTypeEnum -> Arrays.stream(fileTypeEnum.suffixs))
                .collect(Collectors.toList());
        return stringList;
    }

    public static boolean isExistFileType(String suffix) {
        return fileTypes.contains(suffix);
    }

    public static boolean isNotExistFileType(String suffix) {
        return !isExistFileType(suffix);
    }

    public static boolean isType(FileTypeEnum fileTypeEnum, String suffix){
        return Arrays.stream(fileTypeEnum.suffixs).anyMatch(s -> s.equals(suffix));
    }

    public static boolean isImage(String suffix){
        return isType(IMAGE, suffix);
    }

    public static boolean isWord(String suffix){
        return isType(WORD, suffix);
    }

    public static boolean isPdf(String suffix){
        return isType(PDF, suffix);
    }

    public static FileTypeEnum getTypeBySuffix (String suffix) {
        FileTypeEnum[] enums = values();
        for (FileTypeEnum typeEnum : enums) {
            boolean b = Arrays.stream(typeEnum.suffixs).anyMatch(s -> s.equals(suffix));
            if (b) {
                return typeEnum;
            }
        }
        return null;
    }

}
