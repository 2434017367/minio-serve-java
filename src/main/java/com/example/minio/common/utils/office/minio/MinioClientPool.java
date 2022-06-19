package com.example.minio.common.utils.office.minio;

import com.example.minio.common.config.MinioConfig;
import io.minio.MinioClient;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @author zhy
 * @email 2434017367@qq.com
 * @date 2022/6/4 11:51
 *
 * minio客户端连接池
 */
@Log4j2
@Component
public class MinioClientPool {

    /**
     * minioClient队列
     */
    private static final Queue<MinioClient> minioClientQueue = new ConcurrentLinkedDeque<>();
    /**
     * 初始化大小
     */
    private static final Integer initNum = 5;
    /**
     * minio配置类
     */
    @Autowired
    private MinioConfig minioConfig;

    /**
     * 初始化
     */
    @PostConstruct
    public void init() {
        log.info("初始化MinioClientPool");
        for (Integer i = 0; i < initNum; i++) {
            createMinioClient();
        }
    }

    /**
     * 创建minioClient并加入队列中
     */
    private void createMinioClient() {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioConfig.getEndpoint())
                .credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey())
                .build();
        minioClientQueue.add(minioClient);
    }

    /**
     * 获取一个minio客户端
     * @return
     */
    public MinioClient getMinioClient() {
        // 判断是否还有minio客户端没有则创建一个
        if (minioClientQueue.size() == 0) {
           createMinioClient();
        }
        MinioClient minioClient = minioClientQueue.remove();
        return minioClient;
    }

    /**
     * 归还minio客户端
     * @param minioClient
     */
    public void closeMinioClient(MinioClient minioClient) {
        minioClientQueue.add(minioClient);
    }

}
