package com.example.minio.common.utils.office.minio;

import com.example.minio.common.config.MinioConfig;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zhy
 * @email 2434017367@qq.com
 * @date 2022/6/4 11:15
 */
//@Component
public class MinioClientFactory {

    /**
     * 为一个servlet线程创建一个minioClient进行服务
     */
    private static final ThreadLocal<MinioClient> threadLocal = new ThreadLocal<MinioClient>();
    /**
     * minio配置类
     */
    @Autowired
    private MinioConfig minioConfig;

    /**
     * 获取MinioClient
     * @return
     */
    public MinioClient getMinioClient() {
        MinioClient minioClient = threadLocal.get();
        if (minioClient == null) {
            minioClient = MinioClient.builder()
                    .endpoint(minioConfig.getEndpoint())
                    .credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey())
                    .build();
            threadLocal.set(minioClient);
        }
        return minioClient;
    }

    public static void main(String[] args) {
        MinioClientFactory minioClientPool = new MinioClientFactory();
        text(minioClientPool);
        text(minioClientPool);
    }

    private static void text(MinioClientFactory minioClientPool) {
        MinioClient minioClient = minioClientPool.getMinioClient();
        Thread thread = Thread.currentThread();
        System.out.println("Thread:" + thread.getName() + "，minioClient:" + minioClient);
    }

}
