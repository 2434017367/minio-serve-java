package com.example.minio.common.interceptor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.minio.common.exception.MyExceptionInfo;
import com.example.minio.common.result.Result;
import com.example.minio.common.result.ResultCodeEnum;
import com.example.minio.common.utils.AppKeyUtils;
import com.example.minio.common.utils.SpringContextHolder;
import com.example.minio.entity.apps.Apps;
import com.example.minio.service.AppsService;
import com.google.gson.Gson;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.lang.reflect.Method;

/**
 * 请求拦截器
 */
public class RequestInterceptor implements HandlerInterceptor {

    private static final Gson gson = new Gson();

    private AppsService appsService = SpringContextHolder.getBean(AppsService.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object object) throws Exception {
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        if (!(object instanceof HandlerMethod)) {
            return true;
        }

        // 获取 加密appKey
        String appKey = request.getHeader("key1");
        // 获取 加密时间戳
        String stamp = request.getHeader("key2");
        // 校验
        String decode = AppKeyUtils.decode(appKey, stamp);

        // 校验
        if (decode != null) {
            boolean b = appsService.count(new LambdaQueryWrapper<Apps>()
                    .eq(Apps::getAppKey, decode)) > 0;
            if (b) {
                return true;
            }
        } else {
            error(response, Result.error(ResultCodeEnum.ERROR_PERMISSION, "非法请求"), null);
        }
        return false;

    }

    private void error( HttpServletResponse httpServletResponse, Result result, Exception exception){
        MyExceptionInfo.setExceptionInfo(result.getCode().getCode(), result.getMsg(), exception);
        try {
            httpServletResponse.setCharacterEncoding("utf-8");
            httpServletResponse.setContentType("application/json; charset=utf-8");
            httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");
            httpServletResponse.setHeader("Cache-Control", "no-cache");
            PrintWriter out = httpServletResponse.getWriter();
            String res = gson.toJson(result);
            out.write(res);
            out.flush();
            out.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
