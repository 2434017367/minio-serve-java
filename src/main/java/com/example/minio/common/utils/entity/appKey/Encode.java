package com.example.minio.common.utils.entity.appKey;

import lombok.Data;

/**
 * @author zhy
 * @email 2434017367@qq.com
 * @date 2022/7/15 11:20
 */
@Data
public class Encode {

    private String secretKey;

    private String timeStamp;

    public Encode(String secretKey, String timeStamp) {
        this.secretKey = secretKey;
        this.timeStamp = timeStamp;
    }

    public Encode() {
    }
}
