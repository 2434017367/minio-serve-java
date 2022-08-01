package com.example.minio.common.utils;

import com.example.minio.entity.apps.Apps;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zhy
 * @email 2434017367@qq.com
 * @date 2022/6/4 18:43
 *
 * 登录应用工具类
 */
public class LoginAppUtils {


    private static HttpServletRequest getRequest(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return request;
    }

    public static Apps getAppInfo() {
        HttpServletRequest request = getRequest();
        Apps apps = (Apps) request.getAttribute("app");
        return apps;
    }

    public static String getAppId() {
        Apps apps = getAppInfo();
        return apps.getId();
    }

    public static String getMinioBucket(){
        Apps appInfo = getAppInfo();
        return appInfo.getMinioBucket();
    }

}
