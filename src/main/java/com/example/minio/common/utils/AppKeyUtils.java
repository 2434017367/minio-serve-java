package com.example.minio.common.utils;

import com.example.minio.common.interceptor.Decode;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AppKeyUtils {

    private static final String[] codebook = new String[]{"o", "8", "L", "A", "e", "5", "K", "i", "y", "1"};

    private static final String dateFormat = "ssyyHHmmMMdd";

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);

    public static Decode decode(String secretKey, String timeStamp ){
        Decode decode = new Decode();
        if (secretKey == null || timeStamp == null) {
            return null;
        }

        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < codebook.length; i++) {
            map.put(codebook[i], i);
        }
        // 计算偏移量
        int pyl = 0;
        StringBuffer time = new StringBuffer();
        for (int i = 0; i < timeStamp.length(); i++) {
            char c = timeStamp.charAt(i);
            Integer integer = map.get(Character.toString(c));
            if (i == 1) {
                pyl = integer % 5 + 1;
            }
            time.append(integer);
        }

        try {
            Date parse = simpleDateFormat.parse(time.toString());
            decode.setStamp(parse);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // 获得原始密钥
        StringBuffer ysmy = new StringBuffer();
        for (int i = 0; i < secretKey.length(); i++) {
            int c = (int)secretKey.charAt(i);
            int y = c - pyl;
            if (y < 48) {
                y = 123 - (48 - y);
            } else if (y > 57 && y < 65) {
                y -= 7;
            } else if (y > 90 && y < 97) {
                y -= 6;
            } else if (y > 122) {
                y = y - 122 + 47;
            }
            ysmy.append((char) y);
        }

        // 获得appkey
        String appkey = ysmy.toString().replace(timeStamp, "");
        decode.setAppKey(appkey);
        return decode;
    }

}
