package com.example.minio.common.interceptor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ExecutorBuilder;
import com.example.minio.common.exception.MyExceptionInfo;
import com.example.minio.common.utils.IPUtil;
import com.example.minio.common.utils.RequestUtils;
import com.example.minio.common.xss.BodyReaderHttpServletRequestWrapper;
import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @email 2434017367@qq.com
 * @author: zhy
 * @date: 2021/1/29
 * @time: 9:59
 */
@Log4j2
public class ReqLogInterceptor implements HandlerInterceptor, DisposableBean {

    private static final Gson gson = new Gson();

    private static final ThreadLocal<Long> threadLocal = new ThreadLocal<Long>();

    /**
     * 保存请求日志线程池
     */
    private ExecutorService executor;

    /**
     * 构造函数 初始化保存请求日志线程池
     */
    public ReqLogInterceptor() {
        log.info(getClass().getName() + " 构建初始化线程池");
        executor = ExecutorBuilder.create()
                .setCorePoolSize(3)
                .setMaxPoolSize(10)
                .setWorkQueue(new LinkedBlockingQueue<>(10))
                .build();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 保存请求开始时的时间毫秒值
        long timeMillis = System.currentTimeMillis();
        threadLocal.set(timeMillis);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

        if ("OPTIONS".equals(request.getMethod())){
            return;
        }

        try {
            long startTime = threadLocal.get();
            threadLocal.remove();

            long endTime = System.currentTimeMillis();
            // 获取请求url
            String requestURI = request.getRequestURI();
            // 获取请求方法
            String method = request.getMethod();
            // 获取请求ip
            String ip = IPUtil.getIpAddr(request);

            // 获取请求params
            String params = null;
            Map<String, String[]> parameterMap = request.getParameterMap();
            if (CollUtil.isNotEmpty(parameterMap)) {
                params = gson.toJson(parameterMap);
            }

            // 获取请求body
            String body = null;
            boolean isJson = RequestUtils.isRequestContentTypeJson(request);
            if (isJson) {
                Class<? extends HttpServletRequest> aClass = request.getClass();
                if (BodyReaderHttpServletRequestWrapper.class == aClass) {
                    byte[] requestBody = ((BodyReaderHttpServletRequestWrapper) request).getRequestBody();
                    body = new String(requestBody, "UTF-8");
                }
            }

            // 计算请求执行耗时
            long duration = endTime - startTime;

            String finalParams = params;
            String finalBody = body;
            MyExceptionInfo myExceptionInfo = MyExceptionInfo.getExceptionInfo();
            Future<?> submit = executor.submit(() -> {
                int responseStatus = response.getStatus();
                // 日志封装
                String logs = String.format("uri：%s，method：%s，ip：%s，执行耗时：%dms，params：%s, body：%s", requestURI, method, ip, duration, finalParams, finalBody);
                // 查看是否有异常信息
                if (myExceptionInfo != null) {
                    logs = "errCode：" + myExceptionInfo.getCode() + "，errMsg：" + myExceptionInfo.getMsg() + "，" + logs;
                    log.error(logs, myExceptionInfo.getE());
                } else if (responseStatus != 200) {
                    logs = "httpStatus：" + responseStatus + "，" + logs;
                    log.error(logs);
                } else {
                    log.info(logs);
                }
            });

            submit.get();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("请求日志处理错误!", e);
        }
    }

    /**
     * 对象销毁 关闭线程池
     * @throws Exception
     */
    @Override
    public void destroy() throws Exception {
        executor.shutdown();
    }

}
