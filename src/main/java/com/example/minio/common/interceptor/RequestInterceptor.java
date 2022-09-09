package com.example.minio.common.interceptor;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.minio.common.utils.entity.appKey.Decode;
import com.example.minio.common.exception.MyExceptionInfo;
import com.example.minio.common.result.Result;
import com.example.minio.common.result.ResultCodeEnum;
import com.example.minio.common.utils.AppKeyUtils;
import com.example.minio.common.utils.SpringContextHolder;
import com.example.minio.entity.apps.Apps;
import com.example.minio.service.AppsService;
import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Date;

/**
 * 请求拦截器
 */
@Log4j2
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

        if (appKey == null ) {
            appKey = request.getParameter("key1");
        }
        if (stamp == null) {
            stamp = request.getParameter("key2");
        }

        // 校验
        Decode decode = null;
        try {
            decode = AppKeyUtils.decode(appKey, stamp);
        } catch (Exception e) {
            log.error("解密失败", e);
        }
        if (decode != null) {
            String getAppKey = decode.getAppKey();
            Date stampDate = decode.getStamp();
            if (stampDate != null && getAppKey != null) {
                Date now = new Date();
                // 获取请求uri
                String requestURI = request.getRequestURI();

                // 校验时间
                boolean b = false;
                if (requestURI.lastIndexOf("/shareFile") > 0) {
                   if (stampDate.compareTo(now) >= 0) {
                       request.setAttribute("stamp", stamp);
                       b = true;
                   }
                } else {
                    // 时间校验
                    long between = DateUtil.between(stampDate, now, DateUnit.MINUTE, false);
                    // 时间允许误差前2分钟后5分钟
                    if (between > -5 && between < 2) {
                        b = true;
                    }
                }

                if (b) {
                    // 校验appKey
                    Apps one = appsService.getOne(new LambdaQueryWrapper<Apps>()
                            .eq(Apps::getAppKey, getAppKey));
                    if (one != null) {
                        request.setAttribute("app", one);
                        return true;
                    }
                }
            }
        }

        error(response, Result.error(ResultCodeEnum.ERROR_PERMISSION, "非法请求"), null);
        return false;
    }

    private void error(HttpServletResponse httpServletResponse, Result result, Exception exception){
        MyExceptionInfo.setExceptionInfo(result.getCode().getCode(), result.getMsg(), exception);
        try {
            httpServletResponse.setCharacterEncoding("utf-8");
            httpServletResponse.setContentType("application/json; charset=utf-8");
            httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");
            httpServletResponse.setHeader("Cache-Control", "no-cache");
            httpServletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
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
