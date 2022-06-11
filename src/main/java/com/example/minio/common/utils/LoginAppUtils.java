package com.example.minio.common.utils;

import com.example.minio.entity.apps.Apps;

/**
 * @author zhy
 * @email 2434017367@qq.com
 * @date 2022/6/4 18:43
 *
 * 登录应用工具类
 */
public class LoginAppUtils {

    public static Apps getAppInfo() {
        Apps apps = new Apps();
        apps.setId("8ba74b55b7f74cc9b788f1095d403f75");
        apps.setAppKey("920e166308e24bc8a5ba92a7497dcd69");
        apps.setMinioBucket("test");
        return apps;
    }

    public static String getMinioBucket(){
        Apps appInfo = getAppInfo();
        return appInfo.getMinioBucket();
    }

}
