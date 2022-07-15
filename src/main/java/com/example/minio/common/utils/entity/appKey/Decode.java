package com.example.minio.common.utils.entity.appKey;

import lombok.Data;

import java.util.Date;

@Data
public class Decode {

    private Date stamp;

    private String appKey;

    public Decode(String appKey, Date stamp) {
        this.appKey = appKey;
        this.stamp = stamp;
    }

    public Decode() {

    }
}
