package com.example.minio.common.utils.office.minio;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zhy
 * @email 2434017367@qq.com
 * @date 2022/6/4 14:26
 */
@Log4j2
@Aspect
@Component
public class MinioAspect {

    @Autowired
    private MinioClientPool minioClientPool;

//    @Pointcut("@annotation(Minio)")
//    public void point(){}
//
//    @SneakyThrows
//    @Around("point()")
//    public void around(ProceedingJoinPoint jp){
//        // 获取参数列表,赋值minioClient
//        MinioClient minioClient = null;
//        Object[] arguments = jp.getArgs();//传入的参数
//        Signature signature = jp.getSignature();//此处joinPoint的实现类是MethodInvocationProceedingJoinPoint
//        MethodSignature methodSignature = (MethodSignature) signature;//获取参数名
//        String[] parameterNames = methodSignature.getParameterNames();
//        for (int i = 0; i < parameterNames.length; i++) {
//            if ("minioClient".equals(parameterNames[i])) {
//                minioClient = minioClientPool.getMinioClient();
//                arguments[i] = minioClient;
//                break;
//            }
//        }
//
//        // 执行方法
//        jp.proceed();
//
//        // 将minioClient归还回pool中
//        if (minioClient != null) {
//            minioClientPool.closeMinioClient(minioClient);
//        }
//    }

}
