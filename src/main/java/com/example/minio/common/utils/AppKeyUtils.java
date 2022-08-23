package com.example.minio.common.utils;

import cn.hutool.core.util.ArrayUtil;
import com.example.minio.common.utils.entity.appKey.Decode;
import com.example.minio.common.utils.entity.appKey.Encode;

import java.util.Date;

public class AppKeyUtils {

    private static final String[] codebook = new String[]{"o", "8", "L", "A", "e", "5", "K", "i", "y", "1"};

    /**
     * 加密
     * @param appKey
     * @return
     */
    public static Encode encode(String appKey) throws Exception {
        long l = System.currentTimeMillis();
        return encode(appKey, l);
    }

    /**
     * 加密
     * @param appKey
     * @param timeMillis
     * @return
     * @throws Exception
     */
    public static Encode encode(String appKey, long timeMillis) throws Exception {
        String s = String.valueOf(timeMillis);

        String timeStamp = "";
        for (int i = 0; i < s.length(); i++) {
            Integer n = Integer.valueOf(s.substring(i, i + 1));
            timeStamp += codebook[n];
        }
        timeStamp += "798";

        String secretKey = AesUtil.encode(timeStamp, appKey);

        Encode encode = new Encode(secretKey, timeStamp);

        return encode;
    }

    /**
     * 解密
     * @param secretKey
     * @param timeStamp
     * @return
     * @throws Exception
     */
    public static Decode decode(String secretKey, String timeStamp) throws Exception {
        // 解密获取到appKey
        String appKey = AesUtil.decode(timeStamp, secretKey);

        // 根据时间戳计算时间
        timeStamp = timeStamp.substring(0, timeStamp.length() - 3);
        String ms = "";
        for (int i = 0; i < timeStamp.length(); i++) {
            String t = timeStamp.substring(i, i + 1);
            int index = ArrayUtil.indexOf(codebook, t);
            ms += index;
        }
        long timeMillis = Long.valueOf(ms);
        Date date = new Date(timeMillis);

        Decode decode = new Decode(appKey, date);

        return decode;
    }

}
