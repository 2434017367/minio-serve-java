package com.example.minio.common.utils;

import cn.hutool.core.util.StrUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zhy
 * @email 2434017367@qq.com
 * @date 2022/4/18 14:43
 */
public class RequestUtils {

    /**
     * 判断请求内容是否为 application/json
     * @param request
     * @return
     */
    public static boolean isRequestContentTypeJson(HttpServletRequest request) {
        String contentType = request.getHeader("Content-Type");
        if (StrUtil.isNotEmpty(contentType) && contentType.indexOf("application/json") >= 0) {
            return true;
        } else {
            return false;
        }
    }

}
