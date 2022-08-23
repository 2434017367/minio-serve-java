package com.example.minio.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhy
 * @email 2434017367@qq.com
 * @date 2022/6/11 14:00
 */
@Configuration
@ConfigurationProperties(prefix = "app-config")
public class AppConfig {

    /**
     * 临时文件目录
     */
    private String interimPath;
    /**
     * 服务器地址
     */
    private String serverUrl;

    public void setInterimPath(String interimPath) {
        if (this.interimPath == null) {
            this.interimPath = interimPath;
        }
    }

    public String getInterimPath() {
        return interimPath;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }
}
