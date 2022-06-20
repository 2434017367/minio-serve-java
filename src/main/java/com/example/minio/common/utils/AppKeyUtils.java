package com.example.minio.common.utils;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.example.minio.common.interceptor.Decode;
import com.example.minio.common.result.ResultCodeEnum;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AppKeyUtils {

    public static Decode decode(String secretKey, String timeStamp ){
        Decode decode = new Decode();
        if (secretKey == null || timeStamp == null) {
            return null;
        }
        String[] cs = new String[]{"o", "8", "L", "A", "e", "5", "K", "i", "y", "1"};

        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < cs.length; i++) {
            map.put(cs[i], i);
        }
        // 计算偏移量
        int pyl = 0;
        StringBuffer time = new StringBuffer();
        for (int i = 0; i < timeStamp.length(); i++) {
            char c = timeStamp.charAt(i);
            Integer integer = map.get(Character.toString(c));
            if (i == 0) {
                pyl = integer;
            }
            time.append(integer);
        }
        DateFormat df = new SimpleDateFormat("ssyyHHmmMMdd");

//
        try {
            Date parse = df.parse(time.toString());
            decode.setStamp(parse);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // 获得原始密钥
        StringBuffer ysmy = new StringBuffer();
        for (int i = 0; i < secretKey.length(); i++) {
            char c = (char)((int)secretKey.charAt(i) - pyl);
            ysmy.append(c);
        }

        // 获得appkey
        String appkey = ysmy.toString().replace(timeStamp, "");
        decode.setAppKey(appkey);
        return decode;
    }

}
