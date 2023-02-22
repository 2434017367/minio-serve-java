package com.example.minio.entity;

import lombok.Data;

/**
 * @author zhy
 * @email 2434017367@qq.com
 * @date 2023/2/21 16:11
 *
 * 跳过认证上传文件类
 */
@Data
public class SkipAuthFileUploadEntity {

    private String appKey;

    private String filePath;

}
